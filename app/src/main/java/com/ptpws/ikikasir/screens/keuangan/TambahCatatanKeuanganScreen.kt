package com.ptpws.ikikasir.screens.keuangan

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.commond.GlobalImagePickerDialog
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.ImageUploadType
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.commond.rememberGlobalImagePicker
import com.ptpws.ikikasir.core.responsive.presentation.components.AdaptiveFormList
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.TambahCatatanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.HiltTambahCatatanKeuanganUiProvider
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.TambahCatatanKeuanganViewModel
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahCatatanKeuanganScreen(
    navController: NavController,
    viewModel: TambahCatatanKeuanganUiProvider = HiltTambahCatatanKeuanganUiProvider(hiltViewModel<TambahCatatanKeuanganViewModel>())
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val handleBack: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.popBackStackSafely(expectedRoute = AppScreen.TambahCatatanKeuangan.baseRoute)
        }
    }
    BackHandler(onBack = handleBack)

    var showDatePicker by remember { mutableStateOf(false) }

    val imagePickerState = rememberGlobalImagePicker(
        onImageProcessed = { permanentUri ->
            viewModel.onBuktiUriChange(permanentUri.toString())
        },
        targetSubfolder = "financial_record_payment_proofs",
        uploadType = ImageUploadType.BUKTI_PEMBAYARAN,
        progressMessage = "Memeriksa foto bukti..."
    )

    // Date Picker Modal
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.tanggalMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onTanggalChange(it) }
                    showDatePicker = false
                }) {
                    Text("OK", fontFamily = interfamily, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", fontFamily = interfamily, color = Color(0xFF64748B))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Edit Catatan Keuangan" else "Tambah Catatan Keuangan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            val compact = maxWidth <= 360.dp
            AdaptiveFormList(
                contentPadding = PaddingValues(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 8.dp else 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
            ) {

            // ── Error Banner
            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠ ${state.errorMessage}",
                            color = Color(0xFFDC2626),
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // ── 1. JENIS TRANSAKSI CARD (Pemasukan vs Pengeluaran)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
                    ) {
                        Text(
                            text = "Jenis Transaksi",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
                        ) {
                            val isPemasukan = state.jenis.equals("PEMASUKAN", ignoreCase = true)

                            // Option 1: Pemasukan
                            JenisOptionCard(
                                modifier = Modifier.weight(1f),
                                title = "Pemasukan",
                                subtitle = "Uang Masuk",
                                isPositive = true,
                                isSelected = isPemasukan,
                                compact = compact,
                                onClick = { viewModel.onJenisChange("PEMASUKAN") }
                            )

                            // Option 2: Pengeluaran
                            JenisOptionCard(
                                modifier = Modifier.weight(1f),
                                title = "Pengeluaran",
                                subtitle = "Uang Keluar",
                                isPositive = false,
                                isSelected = !isPemasukan,
                                compact = compact,
                                onClick = { viewModel.onJenisChange("PENGELUARAN") }
                            )
                        }
                    }
                }
            }

            // ── 2. FORM INPUTS CARD (Judul & Keterangan)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
                    ) {
                        // Judul
                        GlobalTextField(
                            value = state.judul,
                            onValueChange = viewModel::onJudulChange,
                            label = "Judul *",
                            isRequired = true,
                            placeholder = "Misal: Pembelian Bahan Baku / Penjualan Catering",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Keterangan (Opsional)
                        GlobalTextField(
                            value = state.keterangan,
                            onValueChange = viewModel::onKeteranganChange,
                            label = "Keterangan (Opsional)",
                            placeholder = "Tambahkan rincian / catatan pendukung",
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 3. TANGGAL TRANSAKSI CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tanggal Transaksi",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(state.tanggalMillis))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dateFormatted,
                                    fontFamily = interfamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    // Responsif: tanggal dipotong agar ikon kalender tetap terlihat di 320dp
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. METODE PEMBAYARAN CARD (Grid 2x2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
                    ) {
                        Text(
                            text = "METODE PEMBAYARAN",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )

                        val methods = listOf(
                            MetodeOption("Tunai", "Cash Manual", Icons.Outlined.Payments, Color(0xFFDBEAFE), Color(0xFF2563EB)),
                            MetodeOption("QRIS", "BCA, Gopay, OVO", Icons.Outlined.QrCodeScanner, Color(0xFFF3E8FF), Color(0xFF9333EA)),
                            MetodeOption("Kartu Debit", "EDC Mesin", Icons.Outlined.CreditCard, Color(0xFFF1F5F9), Color(0xFF475569)),
                            MetodeOption("Transfer", "BCA / Mandiri", Icons.Outlined.AccountBalance, Color(0xFFE0F2FE), Color(0xFF0284C7))
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
                            ) {
                                MetodeCardItem(
                                    modifier = Modifier.weight(1f),
                                    option = methods[0],
                                    isSelected = state.metodePembayaran.equals(methods[0].title, ignoreCase = true),
                                    compact = compact,
                                    onSelect = { viewModel.onMetodePembayaranChange(methods[0].title) }
                                )
                                MetodeCardItem(
                                    modifier = Modifier.weight(1f),
                                    option = methods[1],
                                    isSelected = state.metodePembayaran.equals(methods[1].title, ignoreCase = true),
                                    compact = compact,
                                    onSelect = { viewModel.onMetodePembayaranChange(methods[1].title) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
                            ) {
                                MetodeCardItem(
                                    modifier = Modifier.weight(1f),
                                    option = methods[2],
                                    isSelected = state.metodePembayaran.equals(methods[2].title, ignoreCase = true),
                                    compact = compact,
                                    onSelect = { viewModel.onMetodePembayaranChange(methods[2].title) }
                                )
                                MetodeCardItem(
                                    modifier = Modifier.weight(1f),
                                    option = methods[3],
                                    isSelected = state.metodePembayaran.equals(methods[3].title, ignoreCase = true),
                                    compact = compact,
                                    onSelect = { viewModel.onMetodePembayaranChange(methods[3].title) }
                                )
                            }
                        }
                    }
                }
            }

            // ── 5. JUMLAH NOMINAL CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Jumlah Nominal (Rp) *",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        GlobalTextField(
                            value = state.nominalText,
                            onValueChange = viewModel::onNominalChange,
                            leadingIcon = {
                                Text(
                                    text = "Rp ",
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 6. BUKTI PEMBAYARAN CARD (Opsional - Tampil HANYA jika Metode Non-Tunai)
            if (!state.metodePembayaran.equals("Tunai", ignoreCase = true)) {
                item {
                    Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bukti Pembayaran (${state.metodePembayaran})",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Maks. 2MB • Kompresi Otomatis",
                                    fontFamily = interfamily,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (state.buktiUri != null) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = if (state.buktiUri != null) "Terlampir" else "Opsional",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.buktiUri != null) Color(0xFF059669) else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Upload Box Container / Preview Area
                        if (!state.buktiUri.isNullOrBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Image Box Preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = state.buktiUri,
                                        contentDescription = "Bukti Pembayaran",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Top-Right Badge Overlay ("Bukti terlampir")
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 10.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.75f),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "Bukti terlampir",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                // Action Buttons Row: Ubah Bukti & Trash Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { imagePickerState.openPicker() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = "Ubah Bukti",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.onBuktiUriChange(null) },
                                        modifier = Modifier
                                            .width(52.dp)
                                            .heightIn(min = 44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFFDC2626)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Bukti",
                                            tint = Color(0xFF334155),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                                    .clickable { imagePickerState.openPicker() },
                                color = Color(0xFFF8FAFC)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEEF2FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Upload / Pilih Foto Bukti Pembayaran",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Kamera atau galeri",
                                        fontFamily = interfamily,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            // ── 7. TOMBOL SIMPAN CATATAN (Item Terakhir di LazyColumn)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.simpanCatatan(
                            onSuccess = { isEdit ->
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "crud_result_success",
                                    if (isEdit) "Catatan keuangan berhasil diperbarui." else "Catatan keuangan berhasil disimpan."
                                )
                                handleBack()
                            },
                            onFailure = { errorMessage ->
                                GlobalCrudResultDialog.failure(errorMessage)
                            }
                        )
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isEditMode) "Simpan Perubahan" else "Simpan Catatan",
                                fontFamily = interfamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialog Pilihan Upload Gambar Bukti (Kamera vs Galeri)
        GlobalImagePickerDialog(
            show = imagePickerState.showDialog,
            onDismissRequest = imagePickerState.closePicker,
            onCameraSelect = imagePickerState.launchCamera,
            onGallerySelect = imagePickerState.launchGallery,
            title = "Pilih Bukti Pembayaran",
            description = "Pilih metode pengambilan foto bukti:"
        )
    }
}
}

@Composable
private fun JenisOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isPositive: Boolean,
    isSelected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val activeBorderColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
    val activeBgColor = if (isPositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val iconBg = if (isPositive) Color(0xFF10B981) else Color(0xFFF1F5F9)
    val iconTint = if (isPositive) Color.White else Color(0xFF64748B)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) activeBgColor else Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeBorderColor else Color(0xFFE2E8F0)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(if (compact) 30.dp else 38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 12.sp else 14.sp,
                    color = if (isSelected && isPositive) Color(0xFF047857) else if (isSelected) Color(0xFFB91C1C) else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = interfamily,
                    fontSize = if (compact) 9.sp else 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class MetodeOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

@Composable
private fun MetodeCardItem(
    modifier: Modifier = Modifier,
    option: MetodeOption,
    isSelected: Boolean,
    compact: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 70.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (compact) 8.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = option.iconBg,
                    modifier = Modifier.size(if (compact) 28.dp else 36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = option.iconTint,
                            modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.title,
                        fontFamily = interfamily,
                        fontSize = if (compact) 11.sp else 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = option.subtitle,
                        fontFamily = interfamily,
                        fontSize = if (compact) 9.sp else 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terpilih",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}


// ---- PREVIEW (aktifkan satu per satu untuk tes) ----
// import androidx.compose.ui.tooling.preview.Preview
//
// @Preview(name = "HP kecil 320x568", device = "spec:width=320dp,height=568dp", showBackground = true)
// @Preview(name = "HP standar 360x800", device = "spec:width=360dp,height=800dp", showBackground = true)
// @Preview(name = "HP besar 411x891", device = "spec:width=411dp,height=891dp", showBackground = true)
// @Preview(name = "Font besar", device = "spec:width=360dp,height=800dp", fontScale = 1.3f, showBackground = true)
// @Composable
// private fun TambahCatatanKeuanganScreenResponsivePreview() {
//     // Preview butuh ViewModel/Firebase + NavController, perlu composable stateless
// }

@com.ptpws.ikikasir.core.responsive.presentation.components.PhoneDevicePreviews
@androidx.compose.runtime.Composable
private fun TambahCatatanKeuanganScreenLocalPreview() {
    androidx.compose.material3.MaterialTheme {
        TambahCatatanKeuanganScreen(
            navController = androidx.navigation.compose.rememberNavController(),
            viewModel = com.ptpws.ikikasir.commond.PreviewTambahCatatanKeuanganProvider()
        )
    }
}
