package com.ptpws.ikikasir.commond

import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.presentation.state.AuditLogState
import com.ptpws.ikikasir.feature.auditlog.presentation.viewmodel.AuditLogUiProvider
import com.ptpws.ikikasir.feature.auth.presentation.state.SecurityState
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.SecurityUiProvider
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriFormState
import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriListState
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriUiProvider
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.TambahKategoriUiProvider
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.ProfilUiProvider
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.UserProfileState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.presentation.state.DetailProdukState
import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukFormState
import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukListState
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.DetailProdukUiProvider
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.ProdukUiProvider
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.TambahProdukUiProvider
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem
import com.ptpws.ikikasir.feature.promo.domain.model.isAvailableOn
import com.ptpws.ikikasir.feature.promo.domain.model.isExpiredOn
import com.ptpws.ikikasir.feature.promo.domain.model.isUpcomingOn
import com.ptpws.ikikasir.feature.promo.presentation.viewmodel.PromoListState
import com.ptpws.ikikasir.feature.promo.presentation.viewmodel.PromoUiProvider
import com.ptpws.ikikasir.feature.promo.presentation.viewmodel.TambahPromoFormState
import com.ptpws.ikikasir.feature.promo.presentation.viewmodel.TambahPromoUiProvider
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.antrean.presentation.state.QueueHistoryUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanUiProvider
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.QueueHistoryUiProvider
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.presentation.state.BluetoothPrinterState
import com.ptpws.ikikasir.feature.bluetooth.presentation.viewmodel.BluetoothPrinterUiProvider
import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.ManajemenStokState
import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.UpdateStokState
import com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel.ManajemenStokUiProvider
import com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel.UpdateStokUiProvider
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.presentation.state.DetailTransaksiState
import com.ptpws.ikikasir.feature.penjualan.presentation.state.GroupedTransaksi
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PembayaranState
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PrinterStatus
import com.ptpws.ikikasir.feature.penjualan.presentation.state.RiwayatTransaksiState
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.DetailTransaksiUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.PembayaranUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.RiwayatTransaksiUiProvider
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import com.ptpws.ikikasir.feature.referral.presentation.viewmodel.ReferralUiProvider
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.presentation.state.RoleFormState
import com.ptpws.ikikasir.feature.role.presentation.state.RoleListState
import com.ptpws.ikikasir.feature.role.presentation.viewmodel.RoleFormUiProvider
import com.ptpws.ikikasir.feature.role.presentation.viewmodel.RoleListUiProvider
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.presentation.state.LaporanKeuanganState
import com.ptpws.ikikasir.feature.keuangan.presentation.state.DailySalesEntry
import com.ptpws.ikikasir.feature.keuangan.presentation.state.MetodePembayaranReport
import com.ptpws.ikikasir.feature.keuangan.presentation.state.PeriodeFilter
import com.ptpws.ikikasir.feature.keuangan.presentation.state.TopProdukReport
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.CatatanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.CatatanKeuanganUiState
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.DetailCatatanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.DetailCatatanUiState
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.GroupedCatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.LaporanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.TambahCatatanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.TambahCatatanUiState
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.ProdukTerjualItem
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.SortByLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.state.LaporanPenjualanState
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.viewmodel.LaporanPenjualanUiProvider
import com.example.app.ui.screen.DashboardAnalytics
import com.example.app.ui.screen.DashboardUiProvider
import com.example.app.ui.screen.UserSessionState
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserFormState
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserListState
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel.UserFormUiProvider
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel.UserListUiProvider

/**
 * ViewModel global khusus testing Compose Preview.
 *
 * Semua stub di sini mengimplementasikan kontrak `*UiProvider` sehingga screen
 * bisa di-preview tanpa Hilt / Firebase / Room (layoutlib tidak punya Application).
 * Data contoh bersifat statis supaya hasil preview konsisten.
 */

// ── AuditLog ──
/**
 * Stub untuk Compose Preview: menggantikan AuditLogViewModel yang butuh Room.
 * Data contoh statis supaya layout preview selalu sama.
 */

/**
 * Data contoh yang dipakai bersama oleh seluruh stub preview.
 */
private val previewKategoriList = listOf(
    Kategori(
        id = "kat-minuman",
        name = "Minuman",
        description = "Minuman segar untuk menemani hari.",
        iconUrl = "LocalCafe|#4F46E5"
    ),
    Kategori(
        id = "kat-makanan",
        name = "Makanan",
        description = "Menu makanan utama toko.",
        iconUrl = "Restaurant|#F59E0B"
    ),
    Kategori(
        id = "kat-snack",
        name = "Snack",
        description = "Camilan ringan.",
        iconUrl = "LocalDining|#10B981"
    )
)

private val previewProdukList = listOf(
    Produk(
        id = "prd-001",
        name = "Kopi Susu 250ml",
        categoryId = "kat-minuman",
        barcode = "8992753210123",
        costPrice = 12000.0,
        sellingPrice = 18000.0,
        stock = 48,
        lowStockThreshold = 5
    ),
    Produk(
        id = "prd-002",
        name = "Teh Manis 350ml",
        categoryId = "kat-minuman",
        barcode = "8992753210124",
        costPrice = 5000.0,
        sellingPrice = 8000.0,
        stock = 3,
        lowStockThreshold = 5
    ),
    Produk(
        id = "prd-003",
        name = "Nasi Goreng Spesial",
        categoryId = "kat-makanan",
        barcode = "8992753210131",
        costPrice = 15000.0,
        sellingPrice = 25000.0,
        stock = 24,
        lowStockThreshold = 5
    ),
    Produk(
        id = "prd-004",
        name = "Keripik Kentang Chitato",
        categoryId = "kat-snack",
        barcode = "8992753210148",
        costPrice = 9000.0,
        sellingPrice = 12000.0,
        stock = 0,
        lowStockThreshold = 5
    )
)

private val previewTransaksiList = listOf(
    PenjualanTransaksi(
        transactionId = "#INV-2026-0891",
        transactionNumber = "#INV-2026-0891",
        items = listOf(
            CartItem(produk = previewProdukList[0], quantity = 2),
            CartItem(produk = previewProdukList[1], quantity = 1)
        ),
        subtotal = 44000.0,
        total = 44000.0,
        paymentMethod = "Tunai",
        paymentAmount = 50000.0,
        change = 6000.0,
        status = "COMPLETED",
        createdBy = "Kasir",
        customerName = "Budi Santoso",
        tableNumber = "3",
        queueSequence = 1
    ),
    PenjualanTransaksi(
        transactionId = "#INV-2026-0892",
        transactionNumber = "#INV-2026-0892",
        items = listOf(
            CartItem(produk = previewProdukList[2], quantity = 1)
        ),
        subtotal = 25000.0,
        total = 25000.0,
        paymentMethod = "QRIS",
        paymentAmount = 25000.0,
        change = 0.0,
        status = "COMPLETED",
        createdBy = "Kasir",
        customerName = "Siti Aminah",
        tableNumber = "1",
        queueSequence = 2
    ),
    PenjualanTransaksi(
        transactionId = "#INV-2026-0893",
        transactionNumber = "#INV-2026-0893",
        items = listOf(
            CartItem(produk = previewProdukList[3], quantity = 2)
        ),
        subtotal = 24000.0,
        total = 24000.0,
        paymentMethod = "Tunai",
        paymentAmount = 24000.0,
        change = 0.0,
        status = "REFUND",
        createdBy = "Kasir",
        customerName = "Andi Wijaya",
        queueSequence = 3,
        refundReason = "Barang tidak sesuai pesanan"
    )
)


class PreviewAuditLogProvider : AuditLogUiProvider {

    private val sampleLogs = listOf(
        AuditLog(
            id = "1",
            title = "Transaksi baru #INV-2026-001",
            description = "Total Rp 250.000 dibayar dengan Tunai",
            category = "TRANSACTION",
            action = "CREATE",
            actorName = "Budi Santoso",
            actorRole = "Kasir",
            timestamp = sampleTime(0, 9, 15),
            isSynced = true
        ),
        AuditLog(
            id = "2",
            title = "Stok berkurang 5 pcs",
            description = "Kopi Susu 250ml stok 48 -> 43",
            category = "STOCK",
            action = "UPDATE",
            actorName = "Siti Aminah",
            actorRole = "Admin",
            timestamp = sampleTime(0, 10, 42),
            isSynced = true
        ),
        AuditLog(
            id = "3",
            title = "Harga produk diubah",
            description = "Teh Manis 350ml: Rp 5.000 -> Rp 6.000",
            category = "PRICE",
            action = "UPDATE",
            actorName = "Andi Wijaya",
            actorRole = "Admin",
            timestamp = sampleTime(0, 11, 5),
            isSynced = true
        ),
        AuditLog(
            id = "4",
            title = "Promo berhasil diaktifkan",
            description = "Promo DISKON10 berlaku sampai 31 Des 2026",
            category = "PROMO",
            action = "CREATE",
            actorName = "Budi Santoso",
            actorRole = "Kasir",
            timestamp = sampleTime(0, 12, 30),
            isSynced = false
        ),
        AuditLog(
            id = "5",
            title = "Percobaan login gagal",
            description = "Password salah 3 kali berturut-turut",
            category = "AUTHENTICATION",
            action = "LOGIN",
            actorName = "Tidak Diketahui",
            actorRole = "-",
            isWarning = true,
            timestamp = sampleTime(1, 8, 20),
            isSynced = true
        ),
        AuditLog(
            id = "6",
            title = "Backup database berhasil",
            description = "Pencadangan otomatis terjadwal setiap hari",
            category = "SYSTEM",
            action = "LOGIN",
            actorName = "Sistem",
            actorRole = "System",
            timestamp = sampleTime(2, 2, 0),
            isSynced = true
        )
    )

    private val _state = MutableStateFlow(
        AuditLogState(
            auditLogs = sampleLogs,
            filteredLogs = sampleLogs,
            selectedDateFilter = "Semua Tanggal"
        )
    )
    override val state: StateFlow<AuditLogState> = _state.asStateFlow()

    override fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun onCategorySelected(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    override fun onDateFilterSelected(dateFilter: String) {
        _state.value = _state.value.copy(selectedDateFilter = dateFilter)
    }

    override fun onCustomDateRangeSelected(startDateMillis: Long, endDateMillis: Long) {
        _state.value = _state.value.copy(
            selectedDateFilter = "Filter Tanggal",
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis
        )
    }

    private fun sampleTime(daysAgo: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}

// ── Kategori ──
/**
 * Stub untuk Compose Preview: menggantikan KategoriViewModel yang butuh Room/Firebase.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewKategoriProvider : KategoriUiProvider {

    private val _state = MutableStateFlow(
        KategoriListState(
            kategoriList = previewKategoriList,
            filteredList = previewKategoriList,
            isOnline = true
        )
    )
    override val state: StateFlow<KategoriListState> = _state.asStateFlow()

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredList = if (query.isBlank()) {
                previewKategoriList
            } else {
                previewKategoriList.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                }
            }
        )
    }

    override fun requestDeleteKategori(kategori: Kategori) {
        _state.value = _state.value.copy(kategoriToDelete = kategori)
    }

    override fun dismissDeleteDialog() {
        _state.value = _state.value.copy(kategoriToDelete = null)
    }

    override fun deleteKategori(id: String) {
        _state.value = _state.value.copy(
            kategoriList = _state.value.kategoriList.filterNot { it.id == id },
            filteredList = _state.value.filteredList.filterNot { it.id == id },
            kategoriToDelete = null
        )
    }

    override fun toggleCashierVisibility(kategori: Kategori) {
        _state.value = _state.value.copy(
            kategoriList = _state.value.kategoriList.map {
                if (it.id == kategori.id) it.copy(isVisibleInCashier = !it.isVisibleInCashier) else it
            }
        )
    }
}

// ── TambahKategori ──
/**
 * Stub untuk Compose Preview: menggantikan TambahKategoriViewModel yang butuh Firebase/Room.
 * Form sudah diisi supaya preview menampilkan form yang realistis.
 */
class PreviewTambahKategoriProvider : TambahKategoriUiProvider {

    private val _formState = MutableStateFlow(
        KategoriFormState(
            nama = "Minuman",
            deskripsi = "Aneka minuman dingin dan hangat",
            iconName = "LocalCafe",
            colorHex = "#4F46E5",
            isVisibleInCashier = true
        )
    )
    override val formState: StateFlow<KategoriFormState> = _formState.asStateFlow()

    override fun onNamaChange(nama: String) {
        _formState.value = _formState.value.copy(nama = nama, errorMessage = null)
    }

    override fun onDeskripsiChange(deskripsi: String) {
        _formState.value = _formState.value.copy(deskripsi = deskripsi)
    }

    override fun onIconChange(iconName: String) {
        _formState.value = _formState.value.copy(iconName = iconName)
    }

    override fun onColorChange(colorHex: String) {
        _formState.value = _formState.value.copy(colorHex = colorHex)
    }

    override fun onVisibilityChange(isVisible: Boolean) {
        _formState.value = _formState.value.copy(isVisibleInCashier = isVisible)
    }

    override fun simpanKategori() {
        _formState.value = _formState.value.copy(isSuccess = true)
    }

    override fun resetSuccess() {
        _formState.value = _formState.value.copy(isSuccess = false)
    }
}

// ── Produk ──
/**
 * Stub untuk Compose Preview: menggantikan ProdukViewModel yang butuh Room/Firebase.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewProdukProvider : ProdukUiProvider {

    private val _state = MutableStateFlow(
        ProdukListState(
            produkList = previewProdukList,
            filteredList = previewProdukList,
            isOnline = true
        )
    )
    override val state: StateFlow<ProdukListState> = _state.asStateFlow()

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredList = filterList(previewProdukList, query, _state.value.selectedCategoryId)
        )
    }

    override fun onCategoryFilterChange(categoryId: String?) {
        _state.value = _state.value.copy(
            selectedCategoryId = categoryId,
            filteredList = filterList(previewProdukList, _state.value.searchQuery, categoryId)
        )
    }

    override fun requestDeleteProduk(produk: Produk) {
        _state.value = _state.value.copy(produkToDelete = produk)
    }

    override fun dismissDeleteDialog() {
        _state.value = _state.value.copy(produkToDelete = null)
    }

    override fun deleteProduk(id: String) {
        _state.value = _state.value.copy(
            produkList = _state.value.produkList.filterNot { it.id == id },
            filteredList = _state.value.filteredList.filterNot { it.id == id },
            produkToDelete = null
        )
    }

    override fun updateProdukCategory(produk: Produk, newCategoryId: String) {
        _state.value = _state.value.copy(
            produkList = _state.value.produkList.map {
                if (it.id == produk.id) it.copy(categoryId = newCategoryId) else it
            },
            userMessage = "Kategori berhasil diperbarui"
        )
    }

    private fun filterList(list: List<Produk>, query: String, categoryId: String?): List<Produk> =
        list.filter { produk ->
            val matchesQuery = query.isBlank() ||
                produk.name.contains(query, ignoreCase = true) ||
                produk.barcode.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || produk.categoryId == categoryId
            matchesQuery && matchesCategory
        }
}

// ── DetailProduk ──
/**
 * Stub untuk Compose Preview: menggantikan DetailProdukViewModel yang butuh Room/Firebase.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewDetailProdukProvider : DetailProdukUiProvider {

    private val sampleProduk = Produk(
        id = "prd-001",
        name = "Kopi Susu 250ml",
        categoryId = "kat-minuman",
        barcode = "8992753210123",
        costPrice = 12000.0,
        sellingPrice = 18000.0,
        stock = 48,
        lowStockThreshold = 5
    )

    private val samplePromo = Promo(
        id = "promo-001",
        name = "Diskon Minuman 10%",
        promoType = "DISKON",
        items = listOf(
            PromoProductItem(
                productId = "prd-001",
                productName = "Kopi Susu 250ml",
                price = 18000.0
            )
        ),
        discountType = "%",
        discountValue = 10.0,
        startDate = "1 Nov 2026",
        endDate = "30 Nov 2026",
        isActive = true
    )

    private val _state = MutableStateFlow(
        DetailProdukState(
            produk = sampleProduk,
            categoryName = "Minuman",
            terjualHariIni = 12,
            total30HariTerakhir = 148,
            activePromos = listOf(samplePromo),
            activePromo = samplePromo
        )
    )
    override val state: StateFlow<DetailProdukState> = _state.asStateFlow()

    override fun loadProduk(produkId: String) {
        // Data contoh sudah terisi, tidak perlu memuat ulang.
    }

    override fun onVisibilityToggle(isVisible: Boolean) {
        _state.value = _state.value.copy(
            produk = _state.value.produk?.copy(isVisibleInCashier = isVisible)
        )
    }

    override fun requestDeleteDialog(show: Boolean) {
        _state.value = _state.value.copy(showDeleteDialog = show)
    }

    override fun deleteProduk() {
        _state.value = _state.value.copy(isDeleted = true, showDeleteDialog = false)
    }
}

// ── TambahProduk ──
/**
 * Stub untuk Compose Preview: menggantikan TambahProdukViewModel yang butuh Firebase/Room.
 * Form sudah diisi supaya preview menampilkan form yang realistis.
 */
class PreviewTambahProdukProvider : TambahProdukUiProvider {

    private val _formState = MutableStateFlow(
        ProdukFormState(
            name = "Kopi Susu 250ml",
            costPrice = "12000",
            sellingPrice = "18000",
            stock = "48",
            lowStockThreshold = "5",
            categoryId = "kat-minuman",
            barcode = "8992753210123",
            isVisibleInCashier = true
        )
    )
    override val formState: StateFlow<ProdukFormState> = _formState.asStateFlow()

    override fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name, errorMessage = null)
    }

    override fun onCostPriceChange(costPrice: String) {
        _formState.value = _formState.value.copy(costPrice = costPrice, errorMessage = null)
    }

    override fun onSellingPriceChange(sellingPrice: String) {
        _formState.value = _formState.value.copy(sellingPrice = sellingPrice, errorMessage = null)
    }

    override fun onStockChange(stock: String) {
        _formState.value = _formState.value.copy(stock = stock)
    }

    override fun onLowStockThresholdChange(lowStockThreshold: String) {
        _formState.value = _formState.value.copy(lowStockThreshold = lowStockThreshold)
    }

    override fun onCategoryIdChange(categoryId: String) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }

    override fun onImageUrlChange(imageUrl: String) {
        _formState.value = _formState.value.copy(imageUrl = imageUrl)
    }

    override fun onBarcodeChange(barcode: String) {
        _formState.value = _formState.value.copy(barcode = barcode)
    }

    override fun onVisibilityChange(isVisible: Boolean) {
        _formState.value = _formState.value.copy(isVisibleInCashier = isVisible)
    }

    override fun simpanProduk() {
        _formState.value = _formState.value.copy(isSuccess = true)
    }

    override fun resetSuccess() {
        _formState.value = _formState.value.copy(isSuccess = false)
    }

    override fun cetakBarcode(productName: String, barcode: String, onSuccess: () -> Unit) {
        onSuccess()
    }
}

// ── Promo ──
/**
 * Stub untuk Compose Preview: menggantikan PromoViewModel yang butuh Room/Firebase.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewPromoProvider : PromoUiProvider {

    private val samplePromos = listOf(
        Promo(
            id = "promo-1",
            name = "DISKON10",
            promoType = "Diskon Persentase",
            items = listOf(
                PromoProductItem(productId = "produk-1", productName = "Kopi Susu 250ml", price = 15000.0),
                PromoProductItem(productId = "produk-2", productName = "Teh Manis 350ml", price = 6000.0)
            ),
            discountType = "%",
            discountValue = 10.0,
            startDate = "1 Nov 2026",
            endDate = "31 Des 2026",
            isActive = true
        ),
        Promo(
            id = "promo-2",
            name = "HEMAT5000",
            promoType = "Diskon Nominal",
            items = listOf(
                PromoProductItem(productId = "produk-3", productName = "Nasi Goreng Spesial", price = 25000.0)
            ),
            discountType = "Rp",
            discountValue = 5000.0,
            startDate = "10 Nov 2026",
            endDate = "30 Nov 2026",
            isActive = true
        ),
        Promo(
            id = "promo-3",
            name = "GRATISKOPI",
            promoType = "Buy 1 Get 1",
            items = listOf(
                PromoProductItem(productId = "produk-1", productName = "Kopi Susu 250ml", price = 15000.0)
            ),
            discountType = "%",
            discountValue = 100.0,
            startDate = "20 Nov 2026",
            endDate = "20 Des 2026",
            isActive = true
        ),
        Promo(
            id = "promo-4",
            name = "PROMOLAMA",
            promoType = "Diskon Persentase",
            items = listOf(
                PromoProductItem(productId = "produk-4", productName = "Roti Bakar Cokelat", price = 12000.0)
            ),
            discountType = "%",
            discountValue = 15.0,
            startDate = "1 Okt 2026",
            endDate = "31 Okt 2026",
            isActive = false
        )
    )

    private val _state = MutableStateFlow(
        PromoListState(
            promoList = samplePromos,
            filteredList = samplePromos,
            searchQuery = "",
            selectedFilterTab = "Semua",
            isLoading = false
        )
    )
    override val state: StateFlow<PromoListState> = _state.asStateFlow()

    override fun refreshPromoList() {
        _state.value = _state.value.copy(isLoading = false, errorMessage = null)
    }

    override fun onSearchQueryChange(query: String) {
        val current = _state.value
        _state.value = current.copy(
            searchQuery = query,
            filteredList = applyFilter(current.promoList, query, current.selectedFilterTab)
        )
    }

    override fun onFilterTabSelected(tab: String) {
        val current = _state.value
        _state.value = current.copy(
            selectedFilterTab = tab,
            filteredList = applyFilter(current.promoList, current.searchQuery, tab)
        )
    }

    override fun toggleStatus(promoId: String, currentStatus: Boolean) {
        val current = _state.value
        val updated = current.promoList.map { promo ->
            if (promo.id == promoId) promo.copy(isActive = !currentStatus) else promo
        }
        _state.value = current.copy(
            promoList = updated,
            filteredList = applyFilter(updated, current.searchQuery, current.selectedFilterTab)
        )
    }

    override fun deletePromo(promoId: String) {
        val current = _state.value
        val updated = current.promoList.filterNot { it.id == promoId }
        _state.value = current.copy(
            promoList = updated,
            filteredList = applyFilter(updated, current.searchQuery, current.selectedFilterTab)
        )
    }

    private fun applyFilter(list: List<Promo>, query: String, tab: String): List<Promo> =
        list.filter { promo ->
            val matchesQuery = query.isBlank() ||
                    promo.name.contains(query, ignoreCase = true) ||
                    promo.promoType.contains(query, ignoreCase = true)
            val matchesTab = when (tab) {
                "Aktif" -> promo.isAvailableOn()
                "Akan Datang" -> promo.isUpcomingOn()
                "Kedaluwarsa" -> promo.isExpiredOn()
                else -> true
            }
            matchesQuery && matchesTab
        }
}

// ── TambahPromo ──
/**
 * Stub untuk Compose Preview: menggantikan TambahPromoViewModel yang butuh Room/Firebase.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewTambahPromoProvider : TambahPromoUiProvider {

    private val _state = MutableStateFlow(
        TambahPromoFormState(
            namaPromo = "DISKON10",
            tipePromo = "Diskon Persentase",
            selectedProducts = listOf(previewProdukList[0], previewProdukList[1]),
            availableProducts = previewProdukList,
            kategoriList = previewKategoriList,
            isRefreshingProducts = false,
            diskonType = "%",
            nilaiDiskon = "10",
            tanggalMulai = "1 Nov 2026",
            tanggalBerakhir = "31 Des 2026",
            isLoading = false
        )
    )
    override val state: StateFlow<TambahPromoFormState> = _state.asStateFlow()

    override fun loadPromoForEdit(promoId: String) {
        // Data contoh sudah dalam bentuk form terisi, tidak perlu memuat ulang.
    }

    override fun refreshProdukCatalog() {
        _state.value = _state.value.copy(
            availableProducts = previewProdukList,
            kategoriList = previewKategoriList,
            isRefreshingProducts = false
        )
    }

    override fun onNamaPromoChange(nama: String) {
        _state.value = _state.value.copy(namaPromo = nama)
    }

    override fun onTipePromoChange(tipe: String) {
        _state.value = _state.value.copy(tipePromo = tipe)
    }

    override fun onDiskonTypeChange(type: String) {
        _state.value = _state.value.copy(diskonType = type)
    }

    override fun onNilaiDiskonChange(nilai: String) {
        _state.value = _state.value.copy(nilaiDiskon = nilai)
    }

    override fun onTanggalMulaiChange(tanggal: String) {
        _state.value = _state.value.copy(tanggalMulai = tanggal)
    }

    override fun onTanggalBerakhirChange(tanggal: String) {
        _state.value = _state.value.copy(tanggalBerakhir = tanggal)
    }

    override fun setSelectedProducts(products: List<Produk>) {
        _state.value = _state.value.copy(selectedProducts = products)
    }

    override fun removeProduct(productId: String) {
        _state.value = _state.value.copy(
            selectedProducts = _state.value.selectedProducts.filterNot { it.id == productId }
        )
    }

    override fun simpanPromo() {
        _state.value = _state.value.copy(
            isSavedSuccess = true,
            userMessage = "Promo DISKON10 berhasil disimpan!"
        )
    }

    override fun clearMessage() {
        _state.value = _state.value.copy(userMessage = null, errorMessage = null)
    }
}

// ── Profil ──
/**
 * Stub untuk Compose Preview: menggantikan ProfilViewModel yang butuh Firebase/Room.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewProfilProvider : ProfilUiProvider {

    private val sampleUser = User(
        id = "user-1",
        fullName = "Budi Santoso",
        email = "budi.santoso@ikikasir.com",
        roleId = "Admin",
        isActive = true,
        photoUrl = ""
    )

    private val _userProfileState = MutableStateFlow(
        UserProfileState(user = sampleUser, roleName = "Admin", isLoading = false)
    )
    override val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _notaSetting = MutableStateFlow(
        NotaSetting(
            storeName = "Toko Kopi Nusantara",
            storeAddress = "Jl. Merdeka No. 12, Bandung",
            storePhone = "0812-3456-7890",
            wifiName = "Kopi Nusantara",
            wifiPassword = "kopinusantara123",
            paperWidth = "58mm"
        )
    )
    override val notaSetting: StateFlow<NotaSetting> = _notaSetting.asStateFlow()

    private val _taxSetting = MutableStateFlow(
        TaxSetting(
            id = "default",
            isActive = true,
            percentage = 11.0,
            type = TaxSetting.TAX_TYPE_EXCLUSIVE
        )
    )
    override val taxSetting: StateFlow<TaxSetting> = _taxSetting.asStateFlow()

    private val _paymentMethodSetting = MutableStateFlow(
        PaymentMethodSetting(
            id = "default",
            isTunaiEnabled = true,
            isQrisEnabled = true,
            isTransferEnabled = true,
            isKartuKreditEnabled = false
        )
    )
    override val paymentMethodSetting: StateFlow<PaymentMethodSetting> = _paymentMethodSetting.asStateFlow()

    private val _tableSetting = MutableStateFlow(
        TableSetting(id = "default", isTableEnabled = true)
    )
    override val tableSetting: StateFlow<TableSetting> = _tableSetting.asStateFlow()

    override fun updateUserProfile(newFullName: String, newPhotoUrl: String, onComplete: (Boolean) -> Unit) {
        _userProfileState.value = _userProfileState.value.copy(
            user = sampleUser.copy(
                fullName = newFullName,
                photoUrl = newPhotoUrl
            )
        )
        onComplete(true)
    }

    override fun saveNotaSetting(setting: NotaSetting, onComplete: (Boolean) -> Unit) {
        _notaSetting.value = setting
        onComplete(true)
    }

    override fun saveTaxSetting(setting: TaxSetting, onComplete: (Boolean) -> Unit) {
        _taxSetting.value = setting
        onComplete(true)
    }

    override fun savePaymentMethodSetting(setting: PaymentMethodSetting, onComplete: (Boolean) -> Unit) {
        _paymentMethodSetting.value = setting
        onComplete(true)
    }

    override fun saveTableSetting(setting: TableSetting, onComplete: (Boolean) -> Unit) {
        _tableSetting.value = setting
        onComplete(true)
    }
}

// ── Security ──
/**
 * Stub untuk Compose Preview: menggantikan SecurityViewModel yang butuh Firebase Auth.
 * Data contoh statis supaya layout preview selalu sama.
 */
class PreviewSecurityProvider : SecurityUiProvider {

    private val _state = MutableStateFlow(SecurityState())
    override val state: StateFlow<SecurityState> = _state.asStateFlow()

    override fun updateCurrentPassword(value: String) {
        _state.value = _state.value.copy(currentPassword = value, errorMessage = null)
    }

    override fun updateNewPassword(value: String) {
        _state.value = _state.value.copy(newPassword = value, errorMessage = null)
    }

    override fun updateConfirmPassword(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, errorMessage = null)
    }

    override fun clearForm() {
        _state.value = SecurityState()
    }

    override fun changePassword() {
        _state.value = _state.value.copy(
            isLoading = false,
            isSuccess = true,
            errorMessage = null
        )
    }
}

// ── Bluetooth Printer ──

class PreviewBluetoothPrinterProvider : BluetoothPrinterUiProvider {

    private val _state = MutableStateFlow(
        BluetoothPrinterState(
            setting = BluetoothPrinterSetting(
                savedAddress = "00:11:22:33:44:55",
                savedName = "Thermal Printer X1",
                isAutoConnect = true,
                paperWidth = BluetoothPrinterSetting.PAPER_58MM
            ),
            pairedDevices = listOf(
                BluetoothPrinterDevice("00:11:22:33:44:55", "Thermal Printer X1", isPaired = true, isConnected = true),
                BluetoothPrinterDevice("66:77:88:99:AA:BB", "Mini Printer Z2", isPaired = true)
            ),
            isBluetoothEnabled = true,
            hasBluetoothPermission = true,
            selectedDevice = BluetoothPrinterDevice("00:11:22:33:44:55", "Thermal Printer X1", isPaired = true, isConnected = true),
            connectedAddress = "00:11:22:33:44:55"
        )
    )
    override val state: StateFlow<BluetoothPrinterState> = _state.asStateFlow()

    override fun refreshPairedDevices() = Unit

    override fun selectDevice(device: BluetoothPrinterDevice) {
        _state.value = _state.value.copy(selectedDevice = device)
    }

    override fun saveSetting(setting: BluetoothPrinterSetting, onComplete: (Boolean) -> Unit) {
        _state.value = _state.value.copy(setting = setting)
        onComplete(true)
    }

    override fun updatePermissionStatus(hasPermission: Boolean) {
        _state.value = _state.value.copy(hasBluetoothPermission = hasPermission)
    }
}

// ── Manajemen Stok ──

class PreviewManajemenStokProvider : ManajemenStokUiProvider {

    private val _state = MutableStateFlow(
        ManajemenStokState(
            produkList = previewProdukList,
            kategoriList = previewKategoriList,
            totalUnitKeseluruhan = 214,
            jumlahRestockDibutuhkan = 3
        )
    )
    override val state: StateFlow<ManajemenStokState> = _state.asStateFlow()

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun toggleFilterKritisOnly() {
        _state.value = _state.value.copy(isFilterKritisOnly = !_state.value.isFilterKritisOnly)
    }

    override fun closeRestockDialog() {
        _state.value = _state.value.copy(selectedRestockProduk = null, restockTambahJumlahText = "")
    }

    override fun onRestockJumlahChange(jumlah: String) {
        _state.value = _state.value.copy(restockTambahJumlahText = jumlah)
    }

    override fun simpanRestock() {
        _state.value = _state.value.copy(isSuccessRestock = true)
    }

    override fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    override fun clearSuccessRestock() {
        _state.value = _state.value.copy(isSuccessRestock = false)
    }
}

// ── Update Stok ──

class PreviewUpdateStokProvider : UpdateStokUiProvider {

    private val _state = MutableStateFlow(
        UpdateStokState(
            produkList = previewProdukList,
            selectedProduk = previewProdukList.first(),
            newStock = 48
        )
    )
    override val state: StateFlow<UpdateStokState> = _state.asStateFlow()

    override fun selectProduk(produk: Produk) {
        _state.value = _state.value.copy(selectedProduk = produk, newStock = produk.stock)
    }

    override fun toggleProductDropdown(show: Boolean?) {
        _state.value = _state.value.copy(
            showProductDropdown = show ?: !_state.value.showProductDropdown
        )
    }

    override fun incrementStok() {
        _state.value = _state.value.copy(newStock = _state.value.newStock + 1)
    }

    override fun decrementStok() {
        _state.value = _state.value.copy(newStock = (_state.value.newStock - 1).coerceAtLeast(0))
    }

    override fun setNewStock(stock: Int) {
        _state.value = _state.value.copy(newStock = stock)
    }

    override fun simpanPerubahan() {
        _state.value = _state.value.copy(isSuccess = true, successMessage = "Stok berhasil diperbarui.")
    }

    override fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    override fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false, successMessage = null)
    }
}

// ── Kasir ──

val previewKasirState = KasirState(
    kategoriList = previewKategoriList,
    produkKatalog = previewProdukList,
    cartItems = listOf(
        CartItem(produk = previewProdukList[0], quantity = 2),
        CartItem(produk = previewProdukList[1], quantity = 1, note = "Less sugar")
    ),
    activePromos = listOf(
        Promo(
            id = "promo-1",
            name = "Diskon 10% Minuman",
            promoType = "DISKON",
            discountType = "%",
            discountValue = 10.0,
            startDate = "1 Jan 2026",
            endDate = "31 Des 2026",
            isActive = true
        )
    )
)

class PreviewKasirProvider : KasirUiProvider {

    private val _state = MutableStateFlow(previewKasirState)
    override val state: StateFlow<KasirState> = _state.asStateFlow()

    override fun onBarcodeScanned(scannedCode: String) = Unit

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun onCatalogSearchQueryChange(query: String) {
        _state.value = _state.value.copy(catalogSearchQuery = query)
    }

    override fun onCategoryFilterChange(categoryId: String?) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId)
    }

    override fun incrementCartItem(produk: Produk) {
        val items = _state.value.cartItems
        val existing = items.firstOrNull { it.produk.id == produk.id }
        _state.value = _state.value.copy(
            cartItems = if (existing == null) {
                items + CartItem(produk = produk, quantity = 1)
            } else {
                items.map { if (it.produk.id == produk.id) it.copy(quantity = it.quantity + 1) else it }
            }
        )
    }

    override fun decrementCartItem(produk: Produk) {
        _state.value = _state.value.copy(
            cartItems = _state.value.cartItems.mapNotNull {
                if (it.produk.id != produk.id) it
                else if (it.quantity <= 1) null
                else it.copy(quantity = it.quantity - 1)
            }
        )
    }

    override fun updateQuantity(produkId: String, quantity: Int) {
        _state.value = _state.value.copy(
            cartItems = _state.value.cartItems.map {
                if (it.produk.id == produkId) it.copy(quantity = quantity) else it
            }
        )
    }

    override fun setCartItemQuantity(produk: Produk, quantity: Int) {
        updateQuantity(produk.id, quantity)
    }

    override fun removeFromCart(produkId: String) {
        _state.value = _state.value.copy(
            cartItems = _state.value.cartItems.filterNot { it.produk.id == produkId }
        )
    }

    override fun clearCart() {
        _state.value = _state.value.copy(cartItems = emptyList())
    }

    override fun openProductCatalogDialog(show: Boolean) {
        _state.value = _state.value.copy(showProductCatalogDialog = show)
    }

    override fun refreshKatalog() = Unit

    override fun clearUserMessage() {
        _state.value = _state.value.copy(userMessage = null)
    }

    override fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

// ── Pembayaran ──

class PreviewPembayaranProvider : PembayaranUiProvider {

    private val _state = MutableStateFlow(
        PembayaranState(
            cartItems = listOf(
                CartItem(produk = previewProdukList[0], quantity = 2),
                CartItem(produk = previewProdukList[1], quantity = 1)
            ),
            subtotal = 54000.0,
            totalItemCount = 3,
            totalPcsCount = 3,
            grandTotal = 54000.0,
            metodePembayaran = "Tunai",
            uangDiterimaText = "100000",
            uangDiterima = 100000.0,
            kembalian = 46000.0,
            customerName = "Budi Santoso",
            tableNumber = "3",
            printerStatus = PrinterStatus.SIAP,
            printerName = "Thermal Printer X1",
            isPrinterSiap = true
        )
    )
    override val state: StateFlow<PembayaranState> = _state.asStateFlow()

    override fun onSelectPromo(promo: Promo?) {
        _state.value = _state.value.copy(selectedPromo = promo)
    }

    override fun onMetodePembayaranSelect(metode: String) {
        _state.value = _state.value.copy(metodePembayaran = metode)
    }

    override fun onUangDiterimaChange(rawText: String) {
        val nominal = rawText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        _state.value = _state.value.copy(
            uangDiterimaText = rawText,
            uangDiterima = nominal,
            kembalian = (nominal - _state.value.grandTotal).coerceAtLeast(0.0)
        )
    }

    override fun onBuktiPembayaranChange(uri: String?) {
        _state.value = _state.value.copy(buktiPembayaranUri = uri)
    }

    override fun toggleRincianExpanded() {
        _state.value = _state.value.copy(isRincianExpanded = !_state.value.isRincianExpanded)
    }

    override fun toggleCetakStrukOtomatis() {
        _state.value = _state.value.copy(isCetakStrukOtomatis = !_state.value.isCetakStrukOtomatis)
    }

    override fun checkPrinterStatus(context: Context) {
        _state.value = _state.value.copy(printerStatus = PrinterStatus.SIAP, isPrinterSiap = true)
    }

    override fun prosesPembayaran() {
        _state.value = _state.value.copy(isLoading = true)
    }

    override fun dismissSuccessDialog() {
        _state.value = _state.value.copy(showSuccessDialog = false)
    }

    override fun dismissFailedDialog() {
        _state.value = _state.value.copy(showFailedDialog = false)
    }
}

// ── Riwayat Transaksi ──

class PreviewRiwayatTransaksiProvider : RiwayatTransaksiUiProvider {

    private val _state = MutableStateFlow(
        RiwayatTransaksiState(
            transaksiList = previewTransaksiList,
            filteredList = previewTransaksiList,
            groupedTransactions = listOf(
                GroupedTransaksi("Hari Ini, 12 Nov 2026", "3 transaksi", previewTransaksiList)
            )
        )
    )
    override val state: StateFlow<RiwayatTransaksiState> = _state.asStateFlow()

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun onFilterSelect(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    override fun onCustomDateRangeSelect(startMillis: Long, endMillis: Long) {
        _state.value = _state.value.copy(startDateMillis = startMillis, endDateMillis = endMillis)
    }

    override fun applyInitialFilter(filter: String?, startMillis: Long?, endMillis: Long?) {
        _state.value = _state.value.copy(
            selectedFilter = filter ?: _state.value.selectedFilter,
            startDateMillis = startMillis,
            endDateMillis = endMillis
        )
    }

    override fun resetToDefault() {
        _state.value = _state.value.copy(
            selectedFilter = "Hari Ini",
            startDateMillis = null,
            endDateMillis = null,
            customDateLabel = null
        )
    }

    override fun exportToExcel(context: Context) {
        _state.value = _state.value.copy(isExporting = true)
    }

    override fun clearExportState() {
        _state.value = _state.value.copy(isExporting = false, exportedFileUri = null, exportError = null)
    }
}

// ── Detail Transaksi ──

class PreviewDetailTransaksiProvider : DetailTransaksiUiProvider {

    private val _state = MutableStateFlow(
        DetailTransaksiState(transaksi = previewTransaksiList.first())
    )
    override val state: StateFlow<DetailTransaksiState> = _state.asStateFlow()

    override fun loadTransaksi(transactionId: String) = Unit

    override fun refundTransaksi(reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _state.value = _state.value.copy(refundSuccessMessage = "Transaksi berhasil di-refund.")
        onSuccess()
    }
}

// ── Antrean (Waiting List) ──

class PreviewAntreanProvider : AntreanUiProvider {

    private val _uiState = MutableStateFlow<AntreanUiState>(
        AntreanUiState.Success(
            data = listOf(
                Antrean(
                    id = "ant-1",
                    transactionId = "#INV-2026-0891",
                    queueSequence = 1,
                    status = AntreanStatus.WAITING,
                    customerName = "Budi Santoso",
                    tableNumber = "3"
                ),
                Antrean(
                    id = "ant-2",
                    transactionId = "#INV-2026-0892",
                    queueSequence = 2,
                    status = AntreanStatus.WAITING,
                    customerName = "Siti Aminah",
                    tableNumber = "1"
                )
            )
        )
    )
    override val uiState: StateFlow<AntreanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _transaksiMap = MutableStateFlow<Map<String, PenjualanTransaksi>>(
        previewTransaksiList.associateBy { it.transactionId }
    )
    override val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> = _transaksiMap.asStateFlow()

    private val _isBulkProcessing = MutableStateFlow(false)
    override val isBulkProcessing: StateFlow<Boolean> = _isBulkProcessing.asStateFlow()

    override fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    override fun selesaikan(id: String) = Unit

    override fun batalkan(id: String) = Unit

    override fun selesaikanBanyak(ids: List<String>) = Unit

    override fun batalkanBanyak(ids: List<String>) = Unit
}

// ── Riwayat Antrean ──

class PreviewQueueHistoryProvider : QueueHistoryUiProvider {

    private val _uiState = MutableStateFlow<QueueHistoryUiState>(
        QueueHistoryUiState.Success(
            data = listOf(
                QueueHistory(
                    transactionId = "#INV-2026-0891",
                    status = "DONE",
                    customerName = "Budi Santoso",
                    tableNumber = "3",
                    queueSequence = 1
                ),
                QueueHistory(
                    transactionId = "#INV-2026-0892",
                    status = "CANCELLED",
                    customerName = "Siti Aminah",
                    tableNumber = "1",
                    queueSequence = 2
                )
            )
        )
    )
    override val uiState: StateFlow<QueueHistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    override val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _startDateMillis = MutableStateFlow<Long?>(null)
    override val startDateMillis: StateFlow<Long?> = _startDateMillis.asStateFlow()

    private val _endDateMillis = MutableStateFlow<Long?>(null)
    override val endDateMillis: StateFlow<Long?> = _endDateMillis.asStateFlow()

    private val _customDateLabel = MutableStateFlow<String?>(null)
    override val customDateLabel: StateFlow<String?> = _customDateLabel.asStateFlow()

    private val _transaksiMap = MutableStateFlow<Map<String, PenjualanTransaksi>>(
        previewTransaksiList.associateBy { it.transactionId }
    )
    override val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> = _transaksiMap.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    override val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportedFileUri = MutableStateFlow<Uri?>(null)
    override val exportedFileUri: StateFlow<Uri?> = _exportedFileUri.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    override val exportError: StateFlow<String?> = _exportError.asStateFlow()

    override fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    override fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    override fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _startDateMillis.value = startMillis
        _endDateMillis.value = endMillis
    }

    override fun resetToDefault() {
        _selectedFilter.value = "Semua"
        _startDateMillis.value = null
        _endDateMillis.value = null
        _customDateLabel.value = null
    }

    override fun exportToExcel(context: Context, filteredItems: List<QueueHistory>) {
        _isExporting.value = true
    }

    override fun clearExportState() {
        _isExporting.value = false
        _exportedFileUri.value = null
        _exportError.value = null
    }

    override fun groupHistoryByDate(items: List<QueueHistory>) = Unit
}

// ── Role ──

class PreviewRoleListProvider : RoleListUiProvider {

    private val _listState = MutableStateFlow(
        RoleListState(
            roles = listOf(
                Role(
                    id = "role-1",
                    name = "Admin",
                    description = "Akses penuh seluruh menu",
                    userCount = 2,
                    createdAt = 1_700_000_000_000L
                ),
                Role(
                    id = "role-2",
                    name = "Kasir",
                    description = "Operasional kasir & transaksi",
                    userCount = 5,
                    createdAt = 1_700_000_100_000L
                )
            )
        )
    )
    override val listState: StateFlow<RoleListState> = _listState.asStateFlow()

    override fun deleteRole(id: String) {
        _listState.value = _listState.value.copy(
            roles = _listState.value.roles.filterNot { it.id == id },
            message = "Role berhasil dihapus"
        )
    }
}

class PreviewRoleFormProvider : RoleFormUiProvider {

    private val _formState = MutableStateFlow(
        RoleFormState(
            id = "role-2",
            name = "Kasir",
            description = "Operasional kasir & transaksi",
            createdAt = 1_700_000_100_000L,
            menuAccess = mapOf(
                "Dashboard" to true,
                "Profil" to true,
                "Kasir" to true,
                "Transaksi" to true,
                "Antrean" to true
            )
        )
    )
    override val formState: StateFlow<RoleFormState> = _formState.asStateFlow()

    override fun loadRoleById(roleId: String) = Unit

    override fun onNameChange(value: String) {
        _formState.value = _formState.value.copy(name = value)
    }

    override fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    override fun toggleMenuAccess(menuKey: String, isEnabled: Boolean) {
        _formState.value = _formState.value.copy(
            menuAccess = _formState.value.menuAccess.toMutableMap().apply { put(menuKey, isEnabled) }
        )
    }

    override fun saveRole(onSuccess: () -> Unit) {
        _formState.value = _formState.value.copy(isSuccess = true)
        onSuccess()
    }
}

// ── Referral ──

class PreviewReferralProvider : ReferralUiProvider {

    private val _referralInfo = MutableStateFlow(
        ReferralInfo(
            referralCode = "IKIKASIR-8F3K2M",
            uplineName = "Budi Santoso",
            uplineCode = "IKIKASIR-1A2B3C",
            uplineBoundDate = "12 Okt 2026",
            isBound = true,
            availableBalance = 450000.0,
            totalEarnings = 750000.0,
            totalWithdrawn = 300000.0
        )
    )
    override val referralInfo: StateFlow<ReferralInfo> = _referralInfo.asStateFlow()

    private val _userReferrals = MutableStateFlow(
        listOf(
            UserReferralItem(
                id = "ref-1",
                storeName = "Kedai Senja",
                ownerName = "Siti Aminah",
                phoneNumber = "081234567890",
                registeredDate = "12 Nov 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 15000.0
            ),
            UserReferralItem(
                id = "ref-2",
                storeName = "Warung Berkah",
                ownerName = "Andi Wijaya",
                phoneNumber = "082298765432",
                registeredDate = "10 Nov 2026",
                isActive = false,
                status = "Menunggu",
                commissionEarned = 0.0
            )
        )
    )
    override val userReferrals: StateFlow<List<UserReferralItem>> = _userReferrals.asStateFlow()

    override val filteredUserReferrals: StateFlow<List<UserReferralItem>> = _userReferrals

    private val _withdrawals = MutableStateFlow(
        listOf(
            WithdrawalTransaction(
                id = "WD-20261112-123",
                amount = 250000.0,
                bankOrWallet = "BCA",
                accountNumber = "1234567890",
                accountHolder = "Budi Santoso",
                requestDate = "11 Nov 2026, 14:20 WIB",
                status = WithdrawalStatus.MENUNGGU_VERIFIKASI
            )
        )
    )
    override val withdrawals: StateFlow<List<WithdrawalTransaction>> = _withdrawals.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    override val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override fun refreshData() = Unit

    override fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    override fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    override fun copyReferralCode(ctx: Context) = Unit

    override fun shareReferral(ctx: Context) = Unit

    override fun submitClient(
        name: String,
        businessName: String,
        phone: String,
        email: String,
        notes: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccess("Calon client berhasil diajukan.")
    }

    override fun submitWithdrawal(
        amount: Double,
        bankOrWallet: String,
        accountNumber: String,
        accountHolder: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccess()
    }

    override fun simulateApproveWithdrawal(transactionId: String, onDone: () -> Unit) {
        onDone()
    }

    override fun updateUpline(uplineName: String, uplineCode: String) {
        _referralInfo.value = _referralInfo.value.copy(uplineName = uplineName, uplineCode = uplineCode)
    }
}

// ── Catatan Keuangan ──

class PreviewCatatanKeuanganProvider : CatatanKeuanganUiProvider {

    private val sampleCatatan = listOf(
        CatatanKeuangan(
            id = "cat-001",
            jenis = "PEMASUKAN",
            judul = "Penjualan harian",
            keterangan = "Setoran kasir drawer",
            metodePembayaran = "Tunai",
            nominal = 1500000.0
        ),
        CatatanKeuangan(
            id = "cat-002",
            jenis = "PENGELUARAN",
            judul = "Beli stok susceptibility",
            keterangan = "Restock minuman & makanan",
            metodePembayaran = "Transfer",
            nominal = 850000.0
        ),
        CatatanKeuangan(
            id = "cat-003",
            jenis = "PEMASUKAN",
            judul = "Sewa kios bulan ini",
            keterangan = "Sewa kedai November 2026",
            metodePembayaran = "Transfer",
            nominal = 2000000.0
        )
    )

    private val _state = MutableStateFlow(
        CatatanKeuanganUiState(
            rawList = sampleCatatan,
            filteredList = sampleCatatan,
            groupedList = listOf(
                GroupedCatatanKeuangan("12 Nov 2026", sampleCatatan)
            ),
            totalPemasukan = 3500000.0,
            totalPengeluaran = 850000.0,
            saldoBersih = 2650000.0,
            totalTransaksiCount = sampleCatatan.size,
            isLoading = false
        )
    )
    override val state: StateFlow<CatatanKeuanganUiState> = _state.asStateFlow()

    override fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun onFilterSelected(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    override fun onCustomDateRangeSelected(startMillis: Long, endMillis: Long) {
        _state.value = _state.value.copy(
            customStartDateMillis = startMillis,
            customEndDateMillis = endMillis
        )
    }

    override fun deleteCatatan(id: String) {
        _state.value = _state.value.copy(
            rawList = _state.value.rawList.filterNot { it.id == id },
            filteredList = _state.value.filteredList.filterNot { it.id == id }
        )
    }

    override fun exportToExcel(context: Context) {
        _state.value = _state.value.copy(isExporting = true)
    }

    override fun clearExportState() {
        _state.value = _state.value.copy(isExporting = false, exportedFileUri = null, exportError = null)
    }
}

class PreviewDetailCatatanKeuanganProvider : DetailCatatanKeuanganUiProvider {

    private val _state = MutableStateFlow(
        DetailCatatanUiState(
            catatan = CatatanKeuangan(
                id = "cat-001",
                jenis = "PEMASUKAN",
                judul = "Penjualan harian",
                keterangan = "Setoran kasir drawer",
                metodePembayaran = "Tunai",
                nominal = 1500000.0
            ),
            isLoading = false
        )
    )
    override val state: StateFlow<DetailCatatanUiState> = _state.asStateFlow()

    override fun deleteCatatan(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isDeleted = true)
        onSuccess()
    }
}

class PreviewTambahCatatanKeuanganProvider : TambahCatatanKeuanganUiProvider {

    private val _state = MutableStateFlow(
        TambahCatatanUiState(
            isEditMode = false,
            jenis = "PEMASUKAN",
            judul = "Penjualan harian",
            keterangan = "Setoran kasir drawer",
            metodePembayaran = "Tunai",
            nominalText = "1500000"
        )
    )
    override val state: StateFlow<TambahCatatanUiState> = _state.asStateFlow()

    override fun onJenisChange(jenis: String) {
        _state.value = _state.value.copy(jenis = jenis)
    }

    override fun onJudulChange(judul: String) {
        _state.value = _state.value.copy(judul = judul)
    }

    override fun onKeteranganChange(keterangan: String) {
        _state.value = _state.value.copy(keterangan = keterangan)
    }

    override fun onTanggalChange(selectedDateMillis: Long) {
        _state.value = _state.value.copy(tanggalMillis = selectedDateMillis)
    }

    override fun onMetodePembayaranChange(metode: String) {
        _state.value = _state.value.copy(metodePembayaran = metode)
    }

    override fun onNominalChange(text: String) {
        _state.value = _state.value.copy(nominalText = text)
    }

    override fun onBuktiUriChange(uri: String?) {
        _state.value = _state.value.copy(buktiUri = uri)
    }

    override fun simpanCatatan(onSuccess: (isEditMode: Boolean) -> Unit, onFailure: (errorMessage: String) -> Unit) {
        _state.value = _state.value.copy(isSuccess = true)
        onSuccess(_state.value.isEditMode)
    }
}

class PreviewLaporanKeuanganProvider : LaporanKeuanganUiProvider {

    private val _state = MutableStateFlow(
        LaporanKeuanganState(
            isLoading = false,
            selectedPeriode = PeriodeFilter.SEMUA,
            dateRangeLabel = "Semua Transaksi",
            totalOmzet = 24500000.0,
            totalLabaKotor = 11250000.0,
            marginKotorPersen = 45.9f,
            totalLabaBersih = 8900000.0,
            profitMarginPersen = 36.3f,
            totalTransaksiCount = 312,
            totalProdukTerjual = 845,
            rataRataTransaksi = 78525.0,
            totalDiskon = 1250000.0,
            totalPajak = 1225000.0,
            totalHpp = 13250000.0,
            totalPemasukanLainnya = 1500000.0,
            totalPengeluaranBeban = 3850000.0,
            totalRefundNominal = 250000.0,
            totalRefundCount = 4,
            dailySales = listOf(
                DailySalesEntry("Sen", "09 Nov", 1_700_000_000_000L, 3200000.0, 1500000.0, 42, 1800000.0, 120000.0),
                DailySalesEntry("Sel", "10 Nov", 1_700_086_400_000L, 4100000.0, 1950000.0, 55, 2300000.0, 150000.0),
                DailySalesEntry("Rab", "11 Nov", 1_700_172_800_000L, 3750000.0, 1700000.0, 48, 2150000.0, 140000.0)
            ),
            topProdukList = listOf(
                TopProdukReport(1, "prd-001", "Kopi Susu 250ml", "", "kat-minuman", 320, 5760000.0, 23.5f),
                TopProdukReport(2, "prd-003", "Nasi Goreng Spesial", "", "kat-makanan", 180, 4500000.0, 18.4f),
                TopProdukReport(3, "prd-002", "Teh Manis 350ml", "", "kat-minuman", 145, 1160000.0, 4.7f)
            ),
            metodePembayaranList = listOf(
                MetodePembayaranReport("Tunai", 14500000.0, 190, 59.2f),
                MetodePembayaranReport("QRIS", 6800000.0, 88, 27.8f),
                MetodePembayaranReport("Kartu Debit", 3200000.0, 34, 13.0f)
            )
        )
    )
    override val state: StateFlow<LaporanKeuanganState> = _state.asStateFlow()

    override fun setPeriode(periode: PeriodeFilter) {
        _state.value = _state.value.copy(selectedPeriode = periode)
    }

    override fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _state.value = _state.value.copy(
            customStartDateMillis = startMillis,
            customEndDateMillis = endMillis,
            selectedPeriode = PeriodeFilter.KUSTOM
        )
    }

    override fun exportToExcel(context: Context) {
        _state.value = _state.value.copy(isExporting = true)
    }

    override fun clearExportState() {
        _state.value = _state.value.copy(isExporting = false, exportedFileUri = null, exportError = null)
    }
}

// ── Laporan Penjualan ──

class PreviewLaporanPenjualanProvider : LaporanPenjualanUiProvider {

    private val sampleProdukTerjual = listOf(
        ProdukTerjualItem(
            productId = "prd-001",
            namaProduk = "Kopi Susu 250ml",
            kategori = "Minuman",
            hargaSatuan = 18000.0,
            unitTerjual = 320,
            totalOmzet = 5760000.0,
            kontribusiPersen = 23.5,
            rank = 1
        ),
        ProdukTerjualItem(
            productId = "prd-003",
            namaProduk = "Nasi Goreng Spesial",
            kategori = "Makanan",
            hargaSatuan = 25000.0,
            unitTerjual = 180,
            totalOmzet = 4500000.0,
            kontribusiPersen = 18.4,
            rank = 2
        ),
        ProdukTerjualItem(
            productId = "prd-002",
            namaProduk = "Teh Manis 350ml",
            kategori = "Minuman",
            hargaSatuan = 8000.0,
            unitTerjual = 145,
            totalOmzet = 1160000.0,
            kontribusiPersen = 4.7,
            rank = 3
        )
    )

    private val _state = MutableStateFlow(
        LaporanPenjualanState(
            summary = LaporanPenjualanSummary(
                totalUnitTerjual = 645,
                totalOmzet = 11420000.0,
                totalProdukUnik = 3,
                produkTerlarisNama = "Kopi Susu 250ml",
                produkTerlarisQty = 320,
                filterLabel = "Hari Ini",
                items = sampleProdukTerjual
            ),
            displayedItems = sampleProdukTerjual,
            selectedPeriode = PeriodeLaporanPenjualan.HARI_INI,
            selectedSortBy = SortByLaporanPenjualan.TERBANYAK_QTY
        )
    )
    override val state: StateFlow<LaporanPenjualanState> = _state.asStateFlow()

    override fun setPeriode(periode: PeriodeLaporanPenjualan) {
        _state.value = _state.value.copy(selectedPeriode = periode)
    }

    override fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _state.value = _state.value.copy(
            customStartDateMillis = startMillis,
            customEndDateMillis = endMillis,
            selectedPeriode = PeriodeLaporanPenjualan.KUSTOM
        )
    }

    override fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun exportToExcel(context: Context) {
        _state.value = _state.value.copy(isExporting = true)
    }

    override fun clearExportState() {
        _state.value = _state.value.copy(isExporting = false, exportedFileUri = null, exportError = null)
    }
}

// ── Dashboard ──

class PreviewDashboardProvider : DashboardUiProvider {

    private val _sessionState = MutableStateFlow(
        UserSessionState(
            user = User(
                id = "usr-1",
                fullName = "Budi Santoso",
                email = "budi@ikikasir.com",
                roleId = "Admin"
            ),
            roleName = "Admin",
            isLoading = false
        )
    )
    override val sessionState: StateFlow<UserSessionState> = _sessionState.asStateFlow()

    private val _analyticsState = MutableStateFlow(
        DashboardAnalytics(
            penjualanHariIni = 4750000.0,
            pertumbuhanPenjualanPersen = 12.4,
            isPertumbuhanPenjualanPositif = true,
            totalTransaksiHariIni = 68,
            pertumbuhanTransaksiPersen = 8.1,
            isPertumbuhanTransaksiPositif = true,
            totalPelangganHariIni = 52,
            pertumbuhanPelangganPersen = 4.6,
            isPertumbuhanPelangganPositif = true,
            totalProdukTerjualHariIni = 186,
            pertumbuhanProdukTerjualPersen = 15.2,
            isPertumbuhanProdukTerjualPositif = true,
            isLoading = false
        )
    )
    override val analyticsState: StateFlow<DashboardAnalytics> = _analyticsState.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(false)
    override val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedFilter = MutableStateFlow(PeriodeLaporanPenjualan.HARI_INI)
    override val selectedFilter: StateFlow<PeriodeLaporanPenjualan> = _selectedFilter.asStateFlow()

    private val _customStartDateMillis = MutableStateFlow<Long?>(null)
    override val customStartDateMillis: StateFlow<Long?> = _customStartDateMillis.asStateFlow()

    private val _customEndDateMillis = MutableStateFlow<Long?>(null)
    override val customEndDateMillis: StateFlow<Long?> = _customEndDateMillis.asStateFlow()

    private val _customDateLabel = MutableStateFlow("")
    override val customDateLabel: StateFlow<String> = _customDateLabel.asStateFlow()

    private val _isBalanceVisible = MutableStateFlow(true)
    override val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    override fun isAllowed(menuKey: String): Boolean = true

    override fun setFilter(filter: PeriodeLaporanPenjualan) {
        _selectedFilter.value = filter
    }

    override fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customStartDateMillis.value = startMillis
        _customEndDateMillis.value = endMillis
    }

    override fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    override fun refreshDashboard() {
        _isRefreshing.value = true
    }
}

// ── Manajemen Pengguna ──

class PreviewUserListProvider : UserListUiProvider {

    private val sampleUsers = listOf(
        User(
            id = "usr-1",
            fullName = "Budi Santoso",
            email = "budi@ikikasir.com",
            roleId = "Admin",
            isActive = true
        ),
        User(
            id = "usr-2",
            fullName = "Siti Aminah",
            email = "siti@ikikasir.com",
            roleId = "Kasir",
            isActive = true
        ),
        User(
            id = "usr-3",
            fullName = "Andi Wijaya",
            email = "andi@ikikasir.com",
            roleId = "Kasir",
            isActive = false
        )
    )

    private val _listState = MutableStateFlow(
        UserListState(
            users = sampleUsers,
            filteredUsers = sampleUsers
        )
    )
    override val listState: StateFlow<UserListState> = _listState.asStateFlow()

    override fun onSearchQueryChange(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
    }

    override fun onFilterRoleChange(role: String) {
        _listState.value = _listState.value.copy(filterRole = role)
    }

    override fun onFilterStatusChange(status: String) {
        _listState.value = _listState.value.copy(filterStatus = status)
    }

    override fun clearFilters() {
        _listState.value = _listState.value.copy(filterRole = "", filterStatus = "")
    }

    override fun deleteUser(id: String) {
        _listState.value = _listState.value.copy(
            users = _listState.value.users.filterNot { it.id == id },
            filteredUsers = _listState.value.filteredUsers.filterNot { it.id == id },
            message = "Pengguna berhasil dihapus"
        )
    }

    override fun syncData() {
        _listState.value = _listState.value.copy(isSyncing = true)
    }
}

class PreviewUserFormProvider : UserFormUiProvider {

    private val _formState = MutableStateFlow(
        UserFormState(
            id = "usr-2",
            fullName = "Siti Aminah",
            email = "siti@ikikasir.com",
            initialEmail = "siti@ikikasir.com",
            roleId = "Kasir",
            isActive = true
        )
    )
    override val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    override fun loadUserById(userId: String) = Unit

    override fun onFullNameChange(value: String) {
        _formState.value = _formState.value.copy(fullName = value)
    }

    override fun onEmailChange(value: String) {
        _formState.value = _formState.value.copy(email = value)
    }

    override fun onCurrentPasswordChange(value: String) {
        _formState.value = _formState.value.copy(currentPassword = value)
    }

    override fun onPasswordChange(value: String) {
        _formState.value = _formState.value.copy(password = value)
    }

    override fun onConfirmPasswordChange(value: String) {
        _formState.value = _formState.value.copy(confirmPassword = value)
    }

    override fun onRoleChange(value: String) {
        _formState.value = _formState.value.copy(roleId = value)
    }

    override fun onActiveChange(value: Boolean) {
        _formState.value = _formState.value.copy(isActive = value)
    }

    override fun onPhotoUrlChange(value: String) {
        _formState.value = _formState.value.copy(photoUrl = value)
    }

    override fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        onResult(true, "Tautan reset password telah dikirim ke $email")
    }

    override fun resetForm() {
        _formState.value = UserFormState()
    }

    override fun saveUser(onSuccess: () -> Unit) {
        _formState.value = _formState.value.copy(isSuccess = true)
        onSuccess()
    }
}
