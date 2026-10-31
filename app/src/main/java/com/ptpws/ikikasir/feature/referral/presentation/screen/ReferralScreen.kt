package com.ptpws.ikikasir.feature.referral.presentation.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import com.ptpws.ikikasir.feature.referral.presentation.viewmodel.ReferralViewModel
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.util.Locale

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return formatter.format(amount.toLong())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    navController: NavController,
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val handleBack = {
        navController.popBackStackSafely(AppScreen.Referral.route)
    }
    BackHandler(onBack = { handleBack() })

    val context = LocalContext.current
    val referralInfo by viewModel.referralInfo.collectAsState()
    val userReferrals by viewModel.filteredUserReferrals.collectAsState()
    val allUserReferrals by viewModel.userReferrals.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: User Referal, 1: Riwayat Penarikan
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedWithdrawalDetail by remember { mutableStateOf<WithdrawalTransaction?>(null) }
    var showUplineDialog by remember { mutableStateOf(false) }
    var showAjukanClientDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Program Referral",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF4F46E5),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Segarkan",
                                tint = Color(0xFF4F46E5)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. KODE REFERAL SAYA
            item {
                KodeReferralCard(
                    referralCode = referralInfo.referralCode,
                    onCopy = { viewModel.copyReferralCode(context) },
                    onShare = { viewModel.shareReferral(context) }
                )
            }

            // ── 2. AKU TERIKAT KE SIAPA ? (MUhib)
            item {
                AkuTerikatKeSiapaCard(
                    referralInfo = referralInfo,
                    onClickDetail = { showUplineDialog = true }
                )
            }

            // ── 3. JUMLAH PENGHASILAN & TARIK SALDO
            item {
                JumlahPenghasilanCard(
                    referralInfo = referralInfo,
                    onTarikSaldo = { showWithdrawDialog = true }
                )
            }

            // ── 4. TAB NAVIGATION
            item {
                ReferralTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userReferralCount = allUserReferrals.size,
                    withdrawalCount = withdrawals.size
                )
            }

            // ── 5. TAB CONTENT
            when (selectedTab) {
                0 -> {
                    // TAB: User Referal & List data User Referal
                    item {
                        UserReferralHeader(
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            selectedFilter = selectedFilter,
                            onFilterSelect = { viewModel.setSelectedFilter(it) },
                            totalActive = allUserReferrals.count { it.isActive },
                            onAjukanClient = { showAjukanClientDialog = true }
                        )
                    }

                    if (userReferrals.isEmpty()) {
                        item {
                            EmptyUserReferralCard()
                        }
                    } else {
                        items(userReferrals, key = { it.id }) { item ->
                            UserReferralListItem(item = item)
                        }
                    }
                }
                1 -> {
                    // TAB: Riwayat Penarikan Saldo (Menunggu Verifikasi finance -> Selesai)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RIWAYAT PENARIKAN SALDO",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "Klik item untuk lacak alur",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }

                    if (withdrawals.isEmpty()) {
                        item {
                            EmptyWithdrawalCard(onTarikSekarang = { showWithdrawDialog = true })
                        }
                    } else {
                        items(withdrawals, key = { it.id }) { tx ->
                            WithdrawalListItem(
                                transaction = tx,
                                onClick = { selectedWithdrawalDetail = tx }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── DIALOG: Tarik Saldo
    if (showWithdrawDialog) {
        TarikSaldoDialog(
            availableBalance = referralInfo.availableBalance,
            onDismiss = { showWithdrawDialog = false },
            onSubmit = { amount, bank, noRek, namaRek, notes ->
                viewModel.submitWithdrawal(
                    amount = amount,
                    bankOrWallet = bank,
                    accountNumber = noRek,
                    accountHolder = namaRek,
                    notes = notes,
                    onSuccess = {
                        showWithdrawDialog = false
                        selectedTab = 1 // Pindah ke tab Riwayat Penarikan
                        Toast.makeText(
                            context,
                            "Penarikan Rp ${formatRupiah(amount)} berhasil diajukan! Menunggu verifikasi tim finance.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // ── DIALOG: Detail Penarikan & Step Tracker
    selectedWithdrawalDetail?.let { tx ->
        DetailPenarikanDialog(
            transaction = tx,
            onDismiss = { selectedWithdrawalDetail = null },
            onSimulateApproval = {
                viewModel.simulateApproveWithdrawal(tx.id) {
                    selectedWithdrawalDetail = null
                    Toast.makeText(
                        context,
                        "Status penarikan ${tx.id} berhasil disetujui (Selesai)!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // ── DIALOG: Detail Upline (Aku Terikat ke Siapa ? MUhib)
    if (showUplineDialog) {
        AkuTerikatDialog(
            referralInfo = referralInfo,
            onDismiss = { showUplineDialog = false },
            onUpdateUpline = { newName, newCode ->
                viewModel.updateUpline(newName, newCode)
                showUplineDialog = false
                Toast.makeText(context, "Data upline berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── DIALOG: Ajukan Calon Client
    if (showAjukanClientDialog) {
        AjukanCalonClientDialog(
            referralCode = referralInfo.referralCode,
            onDismiss = { showAjukanClientDialog = false },
            onSubmit = { name, businessName, phone, email, notes ->
                viewModel.submitClient(
                    name = name,
                    businessName = businessName,
                    phone = phone,
                    email = email,
                    notes = notes,
                    onSuccess = {
                        showAjukanClientDialog = false
                        selectedTab = 0
                        Toast.makeText(
                            context,
                            "Calon client '$name' berhasil diajukan! Menunggu verifikasi admin.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: KODE REFERAL SAYA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KodeReferralCard(
    referralCode: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3730A3), Color(0xFF4F46E5), Color(0xFF0284C7))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Kode Referal Anda",
                            fontFamily = interfamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Reward Rp 15.000",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Ajak rekan pemilik usaha bergabung ke IKIKASIR. Dapatkan komisi saldo setiap toko aktif bertransaksi!",
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                // Kotak Kode Referal
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KODE REFERAL",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = referralCode,
                                fontFamily = interfamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3730A3),
                                letterSpacing = 1.5.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Tombol Salin
                            OutlinedButton(
                                onClick = onCopy,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE0E7FF)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFEEF2FF)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Salin",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Salin",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4F46E5)
                                )
                            }

                            // Tombol Bagikan
                            Button(
                                onClick = onShare,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F46E5)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Bagikan",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bagikan",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: AKU TERIKAT KE SIAPA ? (MUhib)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AkuTerikatKeSiapaCard(
    referralInfo: ReferralInfo,
    onClickDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickDetail),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Aku terikat ke siapa ?",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "Terhubung",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Info Upline: MUhib
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar Pengundang
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = referralInfo.uplineName.take(2).uppercase(),
                            fontFamily = interfamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = referralInfo.uplineName,
                                fontFamily = interfamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Upline",
                                    fontFamily = interfamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Kode: ${referralInfo.uplineCode} • Terhubung sejak ${referralInfo.uplineBoundDate}",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Detail",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: JUMLAH PENGHASILAN & TARIK SALDO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JumlahPenghasilanCard(
    referralInfo: ReferralInfo,
    onTarikSaldo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Paid,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Jumlah Penghasilan",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Saldo Komisi",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Nominal Saldo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo Tersedia",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rp ${formatRupiah(referralInfo.availableBalance)}",
                        fontFamily = interfamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }

                // Tombol Tarik Saldo
                Button(
                    onClick = onTarikSaldo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Tarik Saldo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tarik Saldo",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Sub Stats (Total Penghasilan & Sudah Ditarik)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Penghasilan",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Rp ${formatRupiah(referralInfo.totalEarnings)}",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Sudah Ditarik",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Rp ${formatRupiah(referralInfo.totalWithdrawn)}",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: TAB NAVIGATION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReferralTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userReferralCount: Int,
    withdrawalCount: Int
) {
    Surface(
        color = Color(0xFFE2E8F0),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabButton(
                title = "User Referal ($userReferralCount)",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "Riwayat Tarik ($withdrawalCount)",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = interfamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF3B32D1) else Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: USER REFERAL & LIST
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UserReferralHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    totalActive: Int,
    onAjukanClient: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIST DATA USER REFERAL",
                fontFamily = interfamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
            FilledTonalButton(
                onClick = onAjukanClient,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Ajukan Calon Client",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Cari toko atau pemilik...",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        // Filter Chips & Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Aktif", "Menunggu").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelect(filter) },
                        label = {
                            Text(
                                text = filter,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEEF2FF),
                            selectedLabelColor = Color(0xFF4F46E5),
                            containerColor = Color.White,
                            labelColor = Color(0xFF64748B)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Text(
                text = "$totalActive Toko Aktif",
                fontFamily = interfamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF059669)
            )
        }
    }
}

@Composable
fun UserReferralListItem(item: UserReferralItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon Toko
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (item.isActive) Color(0xFFECFDF5) else Color(0xFFFFFBEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Store,
                        contentDescription = null,
                        tint = if (item.isActive) Color(0xFF059669) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.storeName,
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.ownerName} • ${item.phoneNumber}",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Bergabung: ${item.registeredDate}",
                        fontFamily = interfamily,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = if (item.isActive) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.status,
                        fontFamily = interfamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isActive) Color(0xFF059669) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (item.commissionEarned > 0) {
                    Text(
                        text = "+Rp ${formatRupiah(item.commissionEarned)}",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyUserReferralCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Belum Ada Pengguna Referal",
                fontFamily = interfamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Text(
                text = "Bagikan kode referal Anda ke teman pemilik usaha untuk mulai mendapatkan komisi saldo!",
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KOMPONEN: RIWAYAT PENARIKAN (Menunggu Verifikasi finance -> Selesai)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WithdrawalListItem(
    transaction: WithdrawalTransaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${transaction.bankOrWallet} • ${transaction.accountNumber}",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = transaction.accountHolder,
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Text(
                    text = "Rp ${formatRupiah(transaction.amount)}",
                    fontFamily = interfamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }

            HorizontalDivider(color = Color(0xFFF8FAFC))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.requestDate,
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                // Status Badge: "Menunggu Verifikasi finance" atau "Selesai"
                WithdrawalStatusBadge(status = transaction.status)
            }
        }
    }
}

@Composable
fun WithdrawalStatusBadge(status: WithdrawalStatus) {
    val bgColor = when (status) {
        WithdrawalStatus.MENUNGGU_VERIFIKASI -> Color(0xFFFEF3C7) // Kuning Amber
        WithdrawalStatus.SELESAI -> Color(0xFFECFDF5) // Hijau Emerald
        WithdrawalStatus.DITOLAK -> Color(0xFFFEE2E2) // Merah
    }
    val textColor = when (status) {
        WithdrawalStatus.MENUNGGU_VERIFIKASI -> Color(0xFFD97706)
        WithdrawalStatus.SELESAI -> Color(0xFF059669)
        WithdrawalStatus.DITOLAK -> Color(0xFFDC2626)
    }
    val icon = when (status) {
        WithdrawalStatus.MENUNGGU_VERIFIKASI -> Icons.Outlined.HourglassEmpty
        WithdrawalStatus.SELESAI -> Icons.Outlined.CheckCircle
        WithdrawalStatus.DITOLAK -> Icons.Outlined.Close
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = status.label,
                fontFamily = interfamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyWithdrawalCard(onTarikSekarang: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Belum Ada Riwayat Penarikan",
                fontFamily = interfamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Text(
                text = "Saldo komisi Anda dapat ditarik kapan saja ke rekening bank atau e-wallet pilihan Anda.",
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onTarikSekarang,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text(
                    text = "Tarik Saldo Sekarang",
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG: AJUKAN CALON CLIENT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AjukanCalonClientDialog(
    referralCode: String,
    onDismiss: () -> Unit,
    onSubmit: (name: String, businessName: String, phone: String, email: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ajukan Calon Client",
                                fontFamily = interfamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Daftarkan calon client mitra",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF1F5F9))
                )

                // Info Banner Komisi Rp 15.000
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Kode Referral: $referralCode",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "Calon client akan otomatis terikat ke akun Anda. Setelah disetujui & diverifikasi di sistem IKIKASIR, Anda otomatis mendapatkan komisi Rp 15.000!",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Field: Nama Pemilik / Calon Client
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nama Pemilik / Calon Client *",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Contoh: Budi Santoso", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Field: Nama Toko / Bisnis
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nama Toko / Usaha *",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        placeholder = { Text("Contoh: Toko Berkah Abadi", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Field: Nomor WhatsApp
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nomor WhatsApp *",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text("Contoh: 081234567890", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Field: Email
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Email Calon Client *",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Contoh: budi@gmail.com", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Field: Catatan
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Catatan Tambahan (Opsional)",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Contoh: Minat paket 1 tahun", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            onSubmit(name, businessName, phone, email, notes)
                        },
                        enabled = !isSubmitting && name.isNotBlank() && businessName.isNotBlank() && phone.isNotBlank() && email.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kirim",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG: TARIK SALDO
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarikSaldoDialog(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double, String, String, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("BCA") }
    var accountNumber by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val bankOptions = listOf("BCA", "Mandiri", "BRI", "BNI", "BSI", "DANA", "GoPay", "OVO", "ShopeePay")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tarik Saldo Komisi",
                        fontFamily = interfamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Info Saldo Tersedia
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo Tersedia",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF4F46E5)
                        )
                        Text(
                            text = "Rp ${formatRupiah(availableBalance)}",
                            fontFamily = interfamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3730A3)
                        )
                    }
                }

                // Input Nominal (Tanpa batasan minimal)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nominal Penarikan",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) amountText = input
                        },
                        prefix = {
                            Text(
                                text = "Rp ",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        },
                        placeholder = { Text("Contoh: 100000", fontFamily = interfamily, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Pilih Bank / E-Wallet
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Bank / E-Wallet Tujuan",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            bankOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontFamily = interfamily) },
                                    onClick = {
                                        selectedBank = opt
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Nomor Rekening & Atas Nama
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nomor Rekening / E-Wallet",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        placeholder = { Text("Contoh: 8230192841", fontFamily = interfamily, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nama Pemilik Rekening",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        placeholder = { Text("Nama sesuai buku tabungan", fontFamily = interfamily, fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Alur status info
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Setelah diajukan, status akan masuk: Menunggu Verifikasi finance ➔ Selesai",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 14.sp
                        )
                    }
                }

                // Tombol Batal & Ajukan Penarikan (diperbaiki agar tidak terpotong)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            onSubmit(amount, selectedBank, accountNumber, accountHolder, notes)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1)),
                        modifier = Modifier
                            .weight(1.8f)
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Ajukan Penarikan",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG: DETAIL PENARIKAN (STEP TRACKER: Tarik Saldo -> Menunggu Verifikasi -> Selesai)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetailPenarikanDialog(
    transaction: WithdrawalTransaction,
    onDismiss: () -> Unit,
    onSimulateApproval: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Status Penarikan",
                        fontFamily = interfamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // ID & Nominal Card
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ID: ${transaction.id}",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Rp ${formatRupiah(transaction.amount)}",
                            fontFamily = interfamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tujuan: ${transaction.bankOrWallet} (${transaction.accountNumber}) a/n ${transaction.accountHolder}",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                // Visual Step Tracker: Diajukan -> Menunggu Verifikasi finance -> Selesai
                Text(
                    text = "STATUS ALUR PENARIKAN",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )

                StepProgressItem(
                    stepNumber = 1,
                    title = "Tarik Saldo Diajukan",
                    subtitle = transaction.requestDate,
                    isDone = true,
                    isActive = false
                )

                StepProgressItem(
                    stepNumber = 2,
                    title = "Menunggu Verifikasi finance",
                    subtitle = if (transaction.status == WithdrawalStatus.MENUNGGU_VERIFIKASI)
                        "Sedang diperiksa & divalidasi oleh tim Finance Iki Kasir"
                    else "Telah diverifikasi & disetujui Finance",
                    isDone = transaction.status == WithdrawalStatus.SELESAI,
                    isActive = transaction.status == WithdrawalStatus.MENUNGGU_VERIFIKASI
                )

                StepProgressItem(
                    stepNumber = 3,
                    title = "Selesai",
                    subtitle = if (transaction.status == WithdrawalStatus.SELESAI)
                        "Dana telah berhasil ditransfer (${transaction.completedDate ?: "Selesai"})"
                    else "Dana akan ditransfer otomatis setelah verifikasi finance disetujui",
                    isDone = transaction.status == WithdrawalStatus.SELESAI,
                    isActive = false,
                    isLast = true
                )

                // Tombol Simulasi Approve (Jika status masih "Menunggu Verifikasi finance")
                if (transaction.status == WithdrawalStatus.MENUNGGU_VERIFIKASI) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Opsi Demo / Simulasi Finance",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Button(
                                onClick = onSimulateApproval,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Simulasikan Disetujui Finance (Selesai)",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Tutup",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressItem(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> Color(0xFF10B981) // Green
                            isActive -> Color(0xFFF59E0B) // Amber
                            else -> Color(0xFFE2E8F0) // Gray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color(0xFF94A3B8)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (isDone) Color(0xFF10B981) else Color(0xFFE2E8F0))
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color(0xFFD97706) else if (isDone) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )
            Text(
                text = subtitle,
                fontFamily = interfamily,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG: AKU TERIKAT KE SIAPA (UPLINE DETAILS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AkuTerikatDialog(
    referralInfo: ReferralInfo,
    onDismiss: () -> Unit,
    onUpdateUpline: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aku terikat ke siapa ?",
                        fontFamily = interfamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Profil Upline
                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = referralInfo.uplineName.take(2).uppercase(),
                                fontFamily = interfamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = referralInfo.uplineName,
                                fontFamily = interfamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0C4A6E)
                            )
                            Text(
                                text = "Kode Referal: ${referralInfo.uplineCode}",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = "Terikat sejak: ${referralInfo.uplineBoundDate}",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                Text(
                    text = "Akun Anda telah terikat resmi ke ${referralInfo.uplineName}. Upline Anda berhak memberikan dukungan panduan implementasi aplikasi dan memastikan bisnis Anda berjalan optimal.",
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Mengerti",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
