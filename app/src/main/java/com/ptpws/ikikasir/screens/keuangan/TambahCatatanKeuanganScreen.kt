package com.ptpws.ikikasir.screens.keuangan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.ImageSourcePickerDialog
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel.TambahCatatanKeuanganViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryRoyalBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahCatatanKeuanganScreen(
    catatanId: String? = null,
    onBack: () -> Unit = {},
    onSimpanSuccess: () -> Unit = {},
    viewModel: TambahCatatanKeuanganViewModel = hiltViewModel()
) {
    var isBackProcessing by remember { mutableStateOf(false) }
    val handleBack = {
        if (!isBackProcessing) {
            isBackProcessing = true
            onBack()
        }
    }
    BackHandler(enabled = !isBackProcessing, onBack = handleBack)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(catatanId) {
        if (!catatanId.isNullOrBlank()) {
            viewModel.loadCatatanForEdit(catatanId)
        }
    }

    LaunchedEffect(state.isSavedSuccess) {
        if (state.isSavedSuccess) {
            onSimpanSuccess()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val amountBringIntoViewRequester = remember { BringIntoViewRequester() }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { epoch ->
                        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        viewModel.onDateChange(formatter.format(Date(epoch)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", fontFamily = interfamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", fontFamily = interfamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showImagePickerDialog) {
        ImageSourcePickerDialog(
            onImageSelected = { uri ->
                viewModel.onProofImageSelected(uri)
                showImagePickerDialog = false
            },
            onDismiss = { showImagePickerDialog = false },
            targetSubfolder = "financial_record_payment_proofs",
            uploadType = com.ptpws.ikikasir.commond.ImageUploadType.BUKTI_PEMBAYARAN
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { handleBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (state.isEditMode) "Edit Catatan Keuangan" else "Tambah Catatan Keuangan",
                    fontFamily = interfamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── 1. JENIS TRANSAKSI (Matches Image 1)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Card Pemasukan (Uang Masuk)
                            val isPemasukan = state.type == "PEMASUKAN"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clickable { viewModel.onTypeChange("PEMASUKAN") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPemasukan) Color(0xFFECFDF5) else Color.White
                                ),
                                border = BorderStroke(
                                    width = if (isPemasukan) 2.dp else 1.dp,
                                    color = if (isPemasukan) Color(0xFF10B981) else Color(0xFFE2E8F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPemasukan) Color(0xFF10B981) else Color(0xFFF1F5F9),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.TrendingUp,
                                                contentDescription = "Pemasukan",
                                                tint = if (isPemasukan) Color.White else Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Pemasukan",
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPemasukan) Color(0xFF047857) else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Uang Masuk",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            // Card Pengeluaran (Uang Keluar)
                            val isPengeluaran = state.type == "PENGELUARAN"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clickable { viewModel.onTypeChange("PENGELUARAN") },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPengeluaran) Color(0xFFFFF1F2) else Color.White
                                ),
                                border = BorderStroke(
                                    width = if (isPengeluaran) 2.dp else 1.dp,
                                    color = if (isPengeluaran) Color(0xFFEF4444) else Color(0xFFE2E8F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPengeluaran) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.TrendingDown,
                                                contentDescription = "Pengeluaran",
                                                tint = if (isPengeluaran) Color.White else Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Pengeluaran",
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPengeluaran) Color(0xFFBE123C) else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Uang Keluar",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. JUDUL / KETERANGAN CATATAN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlobalTextField(
                            value = state.title,
                            onValueChange = { viewModel.onTitleChange(it) },
                            label = "Judul",
                            isRequired = true,
                            placeholder = "Misal: Pembelian Bahan Baku / Penjualan Catering",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlobalTextField(
                            value = state.note,
                            onValueChange = { viewModel.onNoteChange(it) },
                            label = "Keterangan (Opsional)",
                            placeholder = "Tambahkan rincian / catatan pendukung",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 3. TANGGAL TRANSAKSI
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tanggal Transaksi",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
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
                                    text = state.date.ifEmpty { "Pilih tanggal" },
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }



            // ── 5. METODE PEMBAYARAN (Matches Image 2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "METODE PEMBAYARAN",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )

                        val enabledMethods = remember(state.paymentMethodSetting) {
                            val list = mutableListOf<Triple<String, String, Triple<ImageVector, Color, Color>>>()
                            if (state.paymentMethodSetting.isTunaiEnabled) {
                                list.add(Triple("Tunai", "Cash Manual", Triple(Icons.Outlined.Payments, Color(0xFFE0E7FF), PrimaryRoyalBlue)))
                            }
                            if (state.paymentMethodSetting.isQrisEnabled) {
                                list.add(Triple("QRIS", "BCA, Gopay, OVO", Triple(Icons.Default.QrCodeScanner, Color(0xFFF3E8FF), Color(0xFF9333EA))))
                            }
                            if (state.paymentMethodSetting.isKartuKreditEnabled) {
                                list.add(Triple("Kartu Debit", "EDC Mesin", Triple(Icons.Default.CreditCard, Color(0xFFF1F5F9), Color(0xFF475569))))
                            }
                            if (state.paymentMethodSetting.isTransferEnabled) {
                                list.add(Triple("Transfer", "BCA / Mandiri", Triple(Icons.Default.AccountBalance, Color(0xFFE0F2FE), Color(0xFF0284C7))))
                            }
                            if (list.isEmpty()) {
                                list.add(Triple("Tunai", "Cash Manual", Triple(Icons.Outlined.Payments, Color(0xFFE0E7FF), PrimaryRoyalBlue)))
                            }
                            list
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            enabledMethods.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { (title, subtitle, style) ->
                                        CatatanPaymentMethodCard(
                                            title = title,
                                            subtitle = subtitle,
                                            icon = style.first,
                                            iconBgColor = style.second,
                                            iconTint = style.third,
                                            isSelected = state.paymentMethod.equals(title, ignoreCase = true),
                                            onSelect = { viewModel.onPaymentMethodChange(title) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "cash-amount") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlobalTextField(
                            value = state.amountText,
                            onValueChange = { viewModel.onAmountTextChange(it) },
                            label = "Jumlah Nominal (Rp)",
                            isRequired = true,
                            leadingIcon = {
                                Text(
                                    text = "Rp",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            },
                            placeholder = "0",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(amountBringIntoViewRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(250)
                                            amountBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            if (!state.paymentMethod.equals("Tunai", ignoreCase = true)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Bukti Pembayaran (${state.paymentMethod})",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Maks. 2MB • Kompresi Otomatis",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "Opsional",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            val hasImage = state.proofImageUri != null || state.proofImageUrl.isNotBlank()
                            if (hasImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF1F5F9))
                                ) {
                                    AsyncImage(
                                        model = state.proofImageUri ?: state.proofImageUrl,
                                        contentDescription = "Bukti Pembayaran",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
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
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { showImagePickerDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Ubah Bukti", fontFamily = interfamily)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.removeProofImage() },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus bukti pembayaran"
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 140.dp)
                                        .clickable { showImagePickerDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = PrimaryRoyalBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Upload / Pilih Foto Bukti Pembayaran",
                                            fontFamily = interfamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryRoyalBlue
                                        )
                                        Text(
                                            text = "Kamera atau galeri",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }

                item(key = "save-action") {
                Button(
                    onClick = { viewModel.simpanCatatan() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue)
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
            }
        }
    }
}

// ── PAYMENT METHOD CARD (Exact match Image 2)
@Composable
fun CatatanPaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryRoyalBlue else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBgColor,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = interfamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontFamily = interfamily,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Selected Checkmark Badge (Persis Gambar 2)
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryRoyalBlue,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terpilih",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahCatatanKeuanganScreenPreview() {
    MaterialTheme {
        TambahCatatanKeuanganScreen()
    }
}
