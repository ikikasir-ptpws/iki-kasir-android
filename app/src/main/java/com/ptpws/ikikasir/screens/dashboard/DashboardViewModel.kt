package com.example.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.usecase.GetRolesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import kotlinx.coroutines.tasks.await

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DEFAULT_UNASSIGNED_ROLE_ACCESS = listOf(
    "Dashboard",
    "Profil",
    "Produk",
    "Kategori Produk",
    "Manajemen Stok",
    "Kasir",
    "Transaksi",
    "Promo",
    "Laporan Penjualan",
    "Antrean",
    "Riwayat Antrean",
    "Laporan Keuangan",
    "Manajemen Pengguna",
    "Audit Log"
).associateWith { true }

data class UserSessionState(
    val user: User? = null,
    val roleName: String = "",
    val menuAccess: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true
)

data class DashboardAnalytics(
    val penjualanHariIni: Double = 0.0,
    val pertumbuhanPenjualanPersen: Double = 0.0,
    val isPertumbuhanPenjualanPositif: Boolean = true,
    val totalTransaksiHariIni: Int = 0,
    val pertumbuhanTransaksiPersen: Double = 0.0,
    val isPertumbuhanTransaksiPositif: Boolean = true,
    val totalPelangganHariIni: Int = 0,
    val pertumbuhanPelangganPersen: Double = 0.0,
    val isPertumbuhanPelangganPositif: Boolean = true,
    val totalProdukTerjualHariIni: Int = 0,
    val pertumbuhanProdukTerjualPersen: Double = 0.0,
    val isPertumbuhanProdukTerjualPositif: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val getRolesUseCase: GetRolesUseCase,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase
) : ViewModel() {

    private val _sessionState = MutableStateFlow(UserSessionState())
    val sessionState: StateFlow<UserSessionState> = _sessionState.asStateFlow()

    private val _analyticsState = MutableStateFlow(DashboardAnalytics())
    val analyticsState: StateFlow<DashboardAnalytics> = _analyticsState.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var allTransactions: List<PenjualanTransaksi> = emptyList()

    private val _selectedFilter = MutableStateFlow(PeriodeLaporanPenjualan.HARI_INI)
    val selectedFilter: StateFlow<PeriodeLaporanPenjualan> = _selectedFilter.asStateFlow()

    private val _customStartDateMillis = MutableStateFlow<Long?>(null)
    val customStartDateMillis: StateFlow<Long?> = _customStartDateMillis.asStateFlow()

    private val _customEndDateMillis = MutableStateFlow<Long?>(null)
    val customEndDateMillis: StateFlow<Long?> = _customEndDateMillis.asStateFlow()

    private val _customDateLabel = MutableStateFlow("")
    val customDateLabel: StateFlow<String> = _customDateLabel.asStateFlow()

    fun setFilter(filter: PeriodeLaporanPenjualan) {
        _selectedFilter.value = filter
        if (filter != PeriodeLaporanPenjualan.KUSTOM) {
            _customStartDateMillis.value = null
            _customEndDateMillis.value = null
            _customDateLabel.value = ""
        }
        if (allTransactions.isNotEmpty()) {
            calculateAnalytics(allTransactions, filter, _customStartDateMillis.value, _customEndDateMillis.value)
        }
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        val indonesianLocale = Locale.forLanguageTag("id-ID")
        val sDate = SimpleDateFormat("dd MMM yyyy", indonesianLocale).format(Date(startMillis))
        val eDate = SimpleDateFormat("dd MMM yyyy", indonesianLocale).format(Date(endMillis))
        val chipLabel = if (sDate == eDate) {
            SimpleDateFormat("dd MMM", indonesianLocale).format(Date(startMillis))
        } else {
            "${SimpleDateFormat("dd MMM", indonesianLocale).format(Date(startMillis))} - ${SimpleDateFormat("dd MMM", indonesianLocale).format(Date(endMillis))}"
        }

        _customStartDateMillis.value = startMillis
        _customEndDateMillis.value = endMillis
        _customDateLabel.value = chipLabel
        _selectedFilter.value = PeriodeLaporanPenjualan.KUSTOM

        if (allTransactions.isNotEmpty()) {
            calculateAnalytics(allTransactions, PeriodeLaporanPenjualan.KUSTOM, startMillis, endMillis)
        }
    }

    init {
        loadCurrentUserAndPermissions()
        observeTransactions()
        viewModelScope.launch {
            // Ketika aplikasi benar-benar ditutup (di-swipe ke atas dari recent apps) lalu dibuka kembali,
            // beri durasi shimmer yang halus (900ms) agar skeleton shimmer terlihat jelas dan tidak berkedip instan
            delay(900L)
            _isInitialLoading.value = false
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadCurrentUserAndPermissions()
            try {
                // Trigger re-observasi / fetch transaksi
                observeTransactions()
            } catch (_: Exception) {}
            // Berikan jeda shimmer agar animasi refresh terasa halus dan responsif
            delay(800L)
            _isRefreshing.value = false
        }
    }

    fun loadCurrentUserAndPermissions() {
        val firebaseUser = firebaseAuth.currentUser
        val email = firebaseUser?.email.orEmpty().trim()
        if (firebaseUser == null) {
            _sessionState.value = UserSessionState(isLoading = false)
            return
        }

        // 1. Instantly load from local Room DB cache on IO thread (zero UI delay, fast startup)
        viewModelScope.launch(Dispatchers.IO) {
            val localUser = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
            if (localUser != null) {
                val activeUser = localUser.toDomain()
                val roleName = activeUser.roleId.ifBlank { "Admin" }
                val isAdminRole = roleName.equals("Admin", ignoreCase = true) || 
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                val allLocalRoles = roleDao.getAllRoles()
                val matchedRole = allLocalRoles.find {
                    it.name.equals(roleName, ignoreCase = true) || it.id.equals(roleName, ignoreCase = true)
                }
                val menuAccess = when {
                    isAdminRole -> DEFAULT_UNASSIGNED_ROLE_ACCESS
                    matchedRole != null -> matchedRole.menuAccess
                    else -> DEFAULT_UNASSIGNED_ROLE_ACCESS
                }

                _sessionState.value = UserSessionState(
                    user = activeUser,
                    roleName = if (isAdminRole) "Admin" else roleName,
                    menuAccess = menuAccess,
                    isLoading = false
                )
            } else {
                // Buat fallback dari FirebaseUser agar UI langsung aktif tanpa menunggu network
                val fallbackUser = User(
                    id = firebaseUser.uid,
                    fullName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").ifBlank { "Pengguna" },
                    email = email,
                    roleId = "Admin",
                    isActive = true,
                    photoUrl = firebaseUser.photoUrl?.toString().orEmpty()
                )
                _sessionState.value = UserSessionState(
                    user = fallbackUser,
                    roleName = "Admin",
                    menuAccess = DEFAULT_UNASSIGNED_ROLE_ACCESS,
                    isLoading = false
                )
                userDao.insertOrUpdate(fallbackUser.toEntity(isSynced = true))
            }
        }

        // 2. Observasi reaktif dari Room DB: setiap perubahan profil langsung ter-update di Beranda secara realtime
        viewModelScope.launch {
            if (email.isNotBlank()) {
                userDao.getUserByEmailFlow(email).filterNotNull().collect { entity ->
                    val domainUser = entity.toDomain()
                    _sessionState.update { current ->
                        current.copy(
                            user = domainUser,
                            isLoading = false
                        )
                    }
                }
            }
        }

        // 3. Background network validation & sync (berjalan asinkron tanpa menahan UI lokal)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseUser.reload().await()
            } catch (e: Exception) {
                if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                    e.message?.contains("no user record", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true) {
                    try {
                        val local = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
                        if (local != null) {
                            userDao.deletePermanently(local.id)
                            userRemoteDataSource.deleteUser(local.id)
                        }
                    } catch (_: Exception) {}
                    firebaseAuth.signOut()
                    _sessionState.value = UserSessionState(user = null, isLoading = false)
                    return@launch
                }
            }

            // Cek apakah akun terdaftar di deleted_accounts
            val deletedDoc = try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("deleted_accounts")
                    .document(email.lowercase())
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }

            val isDeletedAccount = if (deletedDoc != null && deletedDoc.exists()) {
                val deletedAtMillis = deletedDoc.getTimestamp("deletedAt")?.toDate()?.time
                    ?: deletedDoc.getLong("deletedAt")
                    ?: 0L
                val creationMillis = firebaseUser.metadata?.creationTimestamp ?: 0L

                if (creationMillis > deletedAtMillis) {
                    try { deletedDoc.reference.delete().await() } catch (_: Exception) {}
                    false
                } else {
                    true
                }
            } else {
                false
            }

            if (isDeletedAccount) {
                try { firebaseUser.delete().await() } catch (_: Exception) {}
                try { deletedDoc?.reference?.delete()?.await() } catch (_: Exception) {}
                firebaseAuth.signOut()
                _sessionState.value = UserSessionState(user = null, isLoading = false)
                return@launch
            }

            // Sync update data user dari remote jika ada
            try {
                val remoteUser = userRemoteDataSource.getAllUsers().find { it.email.equals(email, ignoreCase = true) }
                if (remoteUser != null) {
                    userDao.insertOrUpdate(remoteUser.toEntity())
                }
            } catch (_: Exception) {}
        }

        // 4. Realtime sync updates from GetRolesUseCase
        viewModelScope.launch {
            getRolesUseCase().collect { roles ->
                val currentState = _sessionState.value
                val roleName = currentState.roleName
                val isAdminRole = roleName.equals("Admin", ignoreCase = true) || 
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                if (isAdminRole) {
                    _sessionState.value = currentState.copy(
                        menuAccess = DEFAULT_UNASSIGNED_ROLE_ACCESS,
                        isLoading = false
                    )
                    return@collect
                }

                val matchedRole = roles.find { role ->
                    roleName.isNotBlank() && (
                        role.name.equals(roleName, ignoreCase = true) ||
                            role.id.equals(roleName, ignoreCase = true)
                        )
                }

                if (matchedRole != null) {
                    _sessionState.value = currentState.copy(
                        menuAccess = matchedRole.menuAccess,
                        isLoading = false
                    )
                } else if (roleName.isNotBlank() && currentState.menuAccess.isNotEmpty()) {
                    _sessionState.value = currentState.copy(
                        menuAccess = emptyMap(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun isAllowed(menuKey: String): Boolean {
        val state = _sessionState.value
        val currentRole = state.roleName
        val isAdminRole = currentRole.isBlank() || 
                currentRole.equals("Admin", ignoreCase = true) || 
                currentRole.equals("System Administrator", ignoreCase = true)

        // Admin, System Administrator, dan akun Firebase default memiliki akses penuh ke seluruh fitur
        if (isAdminRole) {
            return true
        }

        // Dashboard dan Profil selalu diizinkan
        if (menuKey.equals("Dashboard", ignoreCase = true) || menuKey.equals("Profil", ignoreCase = true)) {
            return true
        }

        val direct = state.menuAccess[menuKey]
        if (direct != null) return direct

        val matched = state.menuAccess.entries.find { it.key.equals(menuKey, ignoreCase = true) }
        return matched?.value ?: false
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            getAllTransaksiUseCase().collect { transactions ->
                allTransactions = transactions
                calculateAnalytics(
                    transactions,
                    _selectedFilter.value,
                    _customStartDateMillis.value,
                    _customEndDateMillis.value
                )
            }
        }
    }

    private fun calculateAnalytics(
        transactions: List<PenjualanTransaksi>,
        filter: PeriodeLaporanPenjualan = _selectedFilter.value,
        customStart: Long? = _customStartDateMillis.value,
        customEnd: Long? = _customEndDateMillis.value
    ) {
        val now = System.currentTimeMillis()
        val nowSec = now / 1000

        // Hanya hitung transaksi yang valid / sukses dan bukan refund
        val validTransactions = transactions.filter { tx ->
            !tx.status.equals("REFUND", ignoreCase = true) &&
            !tx.status.equals("REFUNDED", ignoreCase = true) &&
            !tx.status.equals("BATAL", ignoreCase = true)
        }

        val (currentTxs, previousTxs) = when (filter) {
            PeriodeLaporanPenjualan.HARI_INI -> {
                val todayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfTodaySec = todayCal.timeInMillis / 1000
                val endOfTodaySec = startOfTodaySec + 86400

                val yesterdayCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfYesterdaySec = yesterdayCal.timeInMillis / 1000
                val endOfYesterdaySec = startOfTodaySec

                val cur = validTransactions.filter { it.createdAt.seconds in startOfTodaySec until endOfTodaySec }
                val prev = validTransactions.filter { it.createdAt.seconds in startOfYesterdaySec until endOfYesterdaySec }
                Pair(cur, prev)
            }
            PeriodeLaporanPenjualan.TUJUH_HARI -> {
                val sevenDaysAgoCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val fourteenDaysAgoCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -14)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOf7Sec = sevenDaysAgoCal.timeInMillis / 1000
                val startOf14Sec = fourteenDaysAgoCal.timeInMillis / 1000

                val cur = validTransactions.filter { it.createdAt.seconds in startOf7Sec..nowSec }
                val prev = validTransactions.filter { it.createdAt.seconds in startOf14Sec until startOf7Sec }
                Pair(cur, prev)
            }
            PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> {
                val thirtyDaysAgoCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val sixtyDaysAgoCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -60)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOf30Sec = thirtyDaysAgoCal.timeInMillis / 1000
                val startOf60Sec = sixtyDaysAgoCal.timeInMillis / 1000

                val cur = validTransactions.filter { it.createdAt.seconds in startOf30Sec..nowSec }
                val prev = validTransactions.filter { it.createdAt.seconds in startOf60Sec until startOf30Sec }
                Pair(cur, prev)
            }
            PeriodeLaporanPenjualan.BULAN_INI -> {
                val startOfMonthCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfPrevMonthCal = (startOfMonthCal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                }
                val startOfMonthSec = startOfMonthCal.timeInMillis / 1000
                val startOfPrevMonthSec = startOfPrevMonthCal.timeInMillis / 1000

                val cur = validTransactions.filter { it.createdAt.seconds in startOfMonthSec..nowSec }
                val prev = validTransactions.filter { it.createdAt.seconds in startOfPrevMonthSec until startOfMonthSec }
                Pair(cur, prev)
            }
            PeriodeLaporanPenjualan.KUSTOM -> {
                if (customStart != null && customEnd != null) {
                    val startSec = customStart / 1000
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = customEnd
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val endSec = endCal.timeInMillis / 1000
                    val durationSec = (endSec - startSec).coerceAtLeast(86400)
                    val prevStartSec = startSec - durationSec
                    val prevEndSec = startSec

                    val cur = validTransactions.filter { it.createdAt.seconds in startSec..endSec }
                    val prev = validTransactions.filter { it.createdAt.seconds in prevStartSec until prevEndSec }
                    Pair(cur, prev)
                } else {
                    Pair(validTransactions, emptyList())
                }
            }
            else -> {
                Pair(validTransactions, emptyList())
            }
        }

        // 1. Penjualan (Net Sales)
        val salesCurrent = currentTxs.sumOf { tx ->
            if (tx.total > 0) tx.total else (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
        }
        val salesPrevious = previousTxs.sumOf { tx ->
            if (tx.total > 0) tx.total else (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
        }
        val (growthSales, isPositiveSales) = computeGrowth(salesCurrent, salesPrevious)

        // 2. Total Transaksi
        val countCurrent = currentTxs.size
        val countPrevious = previousTxs.size
        val (growthCount, isPositiveCount) = computeGrowth(countCurrent.toDouble(), countPrevious.toDouble())

        // 3. Pelanggan
        val custCurrent = countUniqueCustomers(currentTxs)
        val custPrevious = countUniqueCustomers(previousTxs)
        val (growthCust, isPositiveCust) = computeGrowth(custCurrent.toDouble(), custPrevious.toDouble())

        // 4. Produk Terjual
        val itemsSoldCurrent = currentTxs.sumOf { tx ->
            if (tx.items.isNotEmpty()) tx.items.sumOf { it.quantity } else 1
        }
        val itemsSoldPrevious = previousTxs.sumOf { tx ->
            if (tx.items.isNotEmpty()) tx.items.sumOf { it.quantity } else 1
        }
        val (growthSold, isPositiveSold) = computeGrowth(itemsSoldCurrent.toDouble(), itemsSoldPrevious.toDouble())

        _analyticsState.value = DashboardAnalytics(
            penjualanHariIni = salesCurrent,
            pertumbuhanPenjualanPersen = growthSales,
            isPertumbuhanPenjualanPositif = isPositiveSales,

            totalTransaksiHariIni = countCurrent,
            pertumbuhanTransaksiPersen = growthCount,
            isPertumbuhanTransaksiPositif = isPositiveCount,

            totalPelangganHariIni = custCurrent,
            pertumbuhanPelangganPersen = growthCust,
            isPertumbuhanPelangganPositif = isPositiveCust,

            totalProdukTerjualHariIni = itemsSoldCurrent,
            pertumbuhanProdukTerjualPersen = growthSold,
            isPertumbuhanProdukTerjualPositif = isPositiveSold,

            isLoading = false
        )
    }

    private fun computeGrowth(current: Double, previous: Double): Pair<Double, Boolean> {
        if (previous <= 0.0) {
            return if (current > 0.0) Pair(100.0, true) else Pair(0.0, true)
        }
        val diff = current - previous
        val percent = (diff / previous) * 100.0
        return Pair(kotlin.math.abs(percent), current >= previous)
    }

    private fun countUniqueCustomers(list: List<PenjualanTransaksi>): Int {
        val namedCustomers = list.map { it.customerName.trim() }
            .filter { it.isNotBlank() && it != "-" }
            .distinct()
            .size
        val anonymousCustomers = list.count { it.customerName.isBlank() || it.customerName.trim() == "-" }
        return namedCustomers + anonymousCustomers
    }
}
