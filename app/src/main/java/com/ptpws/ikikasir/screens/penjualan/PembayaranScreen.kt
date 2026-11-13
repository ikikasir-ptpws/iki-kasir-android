package com.ptpws.ikikasir.screens.penjualan

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.GlobalImagePickerDialog
import com.ptpws.ikikasir.commond.GlobalImageUploadOverlay
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.commond.ImageUploadType
import com.ptpws.ikikasir.commond.rememberGlobalImagePicker
import com.ptpws.ikikasir.core.responsive.presentation.components.AdaptiveFormList
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import kotlinx.coroutines.delay
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectivePrice
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectiveSubtotal
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.PrintStrukUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.PembayaranUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.HiltPembayaranUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.PembayaranViewModel
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PrinterStatus
import com.ptpws.ikikasir.screens.penjualan.component.PembayaranFailedDialog
import com.ptpws.ikikasir.screens.penjualan.component.PembayaranSuccessDialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.PlatformTextStyle
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranScreen(
    navController: NavController,
    onTransaksiSelesai: () -> Unit = {
        navController.popBackStackSafely(expectedRoute = AppScreen.Pembayaran.route)
    },
    viewModel: PembayaranUiProvider = HiltPembayaranUiProvider(hiltViewModel<PembayaranViewModel>())
) {
    BackHandler {
        navController.popBackStackSafely(expectedRoute = AppScreen.Pembayaran.route)
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val printScope = rememberCoroutineScope()
    val printStrukUseCase = remember { PrintStrukUseCase() }

    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
        }
    }

    val printReceipt = {
        val transaksi = state.transaksiSukses
        if (transaksi != null) {
            printScope.launch {
                try {
                    printStrukUseCase(context, transaksi)
                    Toast.makeText(context, "Struk berhasil dikirim ke printer.", Toast.LENGTH_SHORT).show()
                } catch (error: Exception) {
                    Toast.makeText(
                        context,
                        error.message ?: "Gagal mencetak struk.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            printReceipt()
        } else {
            Toast.makeText(context, "Izin Bluetooth diperlukan untuk mencetak struk.", Toast.LENGTH_LONG).show()
        }
    }
    val onPrintReceipt = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            printReceipt()
        }
    }

    // ─── Bukti Pembayaran (Kamera & Galeri Global Picker) ─────────────────────
    val buktiPickerState = rememberGlobalImagePicker(
        onImageProcessed = { permanentUri ->
            viewModel.onBuktiPembayaranChange(permanentUri.toString())
        },
        targetSubfolder = "payment_proofs",
        uploadType = ImageUploadType.BUKTI_PEMBAYARAN,
        progressMessage = "Memeriksa bukti pembayaran..."
    )

    // Periksa status printer Bluetooth saat membuka halaman pembayaran
    LaunchedEffect(Unit) {
        viewModel.checkPrinterStatus(context)
    }

    // Cetak struk otomatis saat pembayaran sukses jika fitur diaktifkan/dicentang
    var lastAutoPrintedTxId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.showSuccessDialog, state.transaksiSukses) {
        val tx = state.transaksiSukses
        if (state.showSuccessDialog && tx != null && state.isCetakStrukOtomatis) {
            val txId = tx.transactionNumber.ifBlank { tx.transactionId }
            if (lastAutoPrintedTxId != txId) {
                lastAutoPrintedTxId = txId
                onPrintReceipt()
            }
        }
    }

    // Success Pop-Up Dialog
    if (state.showSuccessDialog) {
        PembayaranSuccessDialog(
            transaksi = state.transaksiSukses,
            onCetakStruk = onPrintReceipt,
            onTransaksiBaru = {
                viewModel.dismissSuccessDialog()
                onTransaksiSelesai()
            }
        )
    }

    // Failed Pop-Up Dialog
    if (state.showFailedDialog) {
        PembayaranFailedDialog(
            errorMessage = state.errorMessage,
            onDismiss = { viewModel.dismissFailedDialog() }
        )
    }

    // Responsif: token ukuran menyesuaikan lebar layar agar seluruh isi
    // pembayaran tetap utuh di HP sempit (320dp). Pada lebar standar (>= 360dp)
    // nilai ukuran tidak berubah.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp <= 340
    val pagePadding = if (isSmallScreen) 12.dp else 14.dp
    val itemSpacing = if (isSmallScreen) 12.dp else 14.dp
    val cardPadding = if (isSmallScreen) 14.dp else 16.dp
    val totalCardPadding = if (isSmallScreen) 14.dp else 18.dp
    val totalFontSize = if (isSmallScreen) 20.sp else 23.sp
    val totalDecorSize = if (isSmallScreen) 46.dp else 56.dp
    val totalDecorIconSize = if (isSmallScreen) 22.dp else 28.dp
    val cashInputMinHeight = if (isSmallScreen) 46.dp else 50.dp
    val cashFontSize = if (isSmallScreen) 16.sp else 18.sp
    val changeFontSize = if (isSmallScreen) 16.sp else 18.sp
    val confirmButtonHeight = if (isSmallScreen) 48.dp else 52.dp
    val confirmFontSize = if (isSmallScreen) 14.sp else 15.sp

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pembayaran",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF0F172A),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Order ID: ${state.orderId}",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    navController.popBackStackSafely(expectedRoute = AppScreen.Pembayaran.route)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            AdaptiveFormList(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(horizontal = pagePadding),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
            ) {
            // ── 1. Total Tagihan Card (Gambar)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRoyalBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(totalCardPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TOTAL TAGIHAN (${state.totalItemCount} ITEM)",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xCCFFFFFF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${formatRupiah(state.grandTotal)}",
                                fontFamily = interfamily,
                                fontSize = totalFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (state.ppnLabel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = state.ppnLabel,
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xEEFFFFFF),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Decorative Soft Translucent Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x26FFFFFF),
                            modifier = Modifier.size(totalDecorSize)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color(0x88FFFFFF),
                                    modifier = Modifier.size(totalDecorIconSize)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Rincian Pesanan Accordion Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleRincianExpanded() }
                                .padding(cardPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Rincian Pesanan (${state.totalPcsCount} Pcs)",
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Icon(
                                imageVector = if (state.isRincianExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color(0xFF64748B)
                            )
                        }

                        // Expanded Cart Items List
                        AnimatedVisibility(
                            visible = state.isRincianExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = cardPadding, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                state.cartItems.forEach { item ->
                                    val itemPrice = item.getEffectivePrice(state.taxSetting)
                                    val itemSubtotal = item.getEffectiveSubtotal(state.taxSetting)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.produk.name,
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.quantity} Pcs x Rp ${formatRupiah(itemPrice)}",
                                                fontFamily = interfamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Text(
                                            text = "Rp ${formatRupiah(itemSubtotal)}",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                                if (state.promoDiscountAmount > 0) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Diskon Promo (${state.selectedPromo?.nama ?: ""})",
                                            fontFamily = interfamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF16A34A)
                                        )
                                        Text(
                                            text = "- Rp ${formatRupiah(state.promoDiscountAmount)}",
                                            fontFamily = interfamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // ── 2.3 Promo Selection Card
            item {
                var promoDropdownExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (state.selectedPromo != null) PrimaryRoyalBlue else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = null,
                                tint = PrimaryRoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Promo / Diskon",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        val eligiblePromos = state.eligiblePromos
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (state.selectedPromo != null) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (state.selectedPromo != null) PrimaryRoyalBlue.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = eligiblePromos.isNotEmpty()) { promoDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (state.selectedPromo != null) {
                                        val promo = state.selectedPromo!!
                                        val diskonLabel = if (promo.diskonType == "%") "${promo.nilaiDiskon.toInt()}%" else "Rp ${formatRupiah(promo.nilaiDiskon)}"
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = promo.nama,
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryRoyalBlue,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Diskon $diskonLabel (-Rp ${formatRupiah(state.promoDiscountAmount)})",
                                                fontFamily = interfamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF16A34A),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.onSelectPromo(null) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus Promo",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = if (eligiblePromos.isEmpty()) "Tidak ada promo untuk produk ini" else "Pilih Promo",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            color = if (eligiblePromos.isEmpty()) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                        if (eligiblePromos.isNotEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Pilih",
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }

                            if (eligiblePromos.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = promoDropdownExpanded,
                                    onDismissRequest = { promoDropdownExpanded = false },
                                    modifier = Modifier.width(maxWidth)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Tanpa Promo",
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        },
                                        onClick = {
                                            viewModel.onSelectPromo(null)
                                            promoDropdownExpanded = false
                                        }
                                    )
                                    eligiblePromos.forEach { promo ->
                                        val diskonLabel = if (promo.diskonType == "%") "${promo.nilaiDiskon.toInt()}%" else "Rp ${formatRupiah(promo.nilaiDiskon)}"
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = promo.nama,
                                                        fontFamily = interfamily,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Diskon $diskonLabel ${if (promo.tipePromo.isNotBlank()) "• ${promo.tipePromo}" else ""}",
                                                        fontFamily = interfamily,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF16A34A)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.onSelectPromo(promo)
                                                promoDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 2.4 Nama Pelanggan Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Nama Pelanggan (Opsional)",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        BasicTextField(
                            value = state.customerName,
                            onValueChange = viewModel::onCustomerNameChange,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontFamily = interfamily,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            decorationBox = { inner ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (state.customerName.isEmpty()) {
                                        Text(
                                            text = "Masukkan nama pelanggan...",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8),
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }

            // ── 2.4.5 Nomor Meja Input Card (Tampil jika fitur Meja Aktif di Profil)
            if (state.tableSetting.isTableEnabled) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TableBar,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Nomor Meja (Opsional)",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            BasicTextField(
                                value = state.tableNumber,
                                onValueChange = viewModel::onTableNumberChange,
                                textStyle = TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontFamily = interfamily,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                decorationBox = { inner ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (state.tableNumber.isEmpty()) {
                                            Text(
                                                text = "Masukkan nomor meja...",
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier.align(Alignment.CenterStart)
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ── 2.5 Catatan Pesanan Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Catatan Pesanan (Opsional)",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        BasicTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotesChange,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontFamily = interfamily,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            decorationBox = { inner ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (state.notes.isEmpty()) {
                                        Text(
                                            text = "Tambah catatan khusus pesanan...",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8),
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }

            // ── 3. METODE PEMBAYARAN Grid Header & Cards (Gambar)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "METODE PEMBAYARAN",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    val enabledMethods = remember(state.paymentMethodSetting) {
                        val list = mutableListOf<Triple<String, String, Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, Color>>>()
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
                                    PaymentMethodCard(
                                        title = title,
                                        subtitle = subtitle,
                                        icon = style.first,
                                        iconBgColor = style.second,
                                        iconTint = style.third,
                                        isSelected = state.metodePembayaran.equals(title, ignoreCase = true),
                                        onSelect = { viewModel.onMetodePembayaranSelect(title) },
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

            // ── 4. Uang yang di Terima & Kembalian Section (Visible when Tunai selected)
            if (state.metodePembayaran.equals("Tunai", ignoreCase = true)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cardPadding),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Uang yang di Terima",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )

                            // Amount Input Field
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = cashInputMinHeight),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rp  ",
                                        fontFamily = interfamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )

                                    BasicTextField(
                                        value = state.uangDiterimaText,
                                        onValueChange = { viewModel.onUangDiterimaChange(it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color(0xFF0F172A),
                                            fontSize = cashFontSize,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        ),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.CenterStart,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (state.uangDiterimaText.isEmpty()) {
                                                    Text(
                                                        text = "0",
                                                        fontFamily = interfamily,
                                                        fontSize = cashFontSize,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFCBD5E1),
                                                        modifier = Modifier.align(Alignment.CenterStart)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }



                            // Light Green Kembalian Box (Persis Gambar)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CurrencyExchange,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Kembalian",
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "Rp ${formatRupiah(state.kembalian)}",
                                        fontFamily = interfamily,
                                        fontSize = changeFontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 4b. Bukti Pembayaran (Non-Tunai: QRIS, Transfer, Kartu Debit, dll.)
            if (!state.metodePembayaran.equals("Tunai", ignoreCase = true)) {
                item {
                    BuktiPembayaranCard(
                        metodePembayaran = state.metodePembayaran,
                        buktiUri = state.buktiPembayaranUri,
                        isUploading = state.isUploadingBukti,
                        uploadProgress = state.uploadBuktiProgress,
                        uploadMessage = state.uploadBuktiMessage,
                        onOpenPicker = buktiPickerState.openPicker,
                        onPickGallery = buktiPickerState.launchGallery,
                        onCaptureCamera = buktiPickerState.launchCamera,
                        onRemoveBukti = { viewModel.onBuktiPembayaranChange(null) }
                    )
                }
            }

            // ── 5. Cetak Struk & Printer Status Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleCetakStrukOtomatis() }
                    ) {
                        Checkbox(
                            checked = state.isCetakStrukOtomatis,
                            onCheckedChange = { viewModel.toggleCetakStrukOtomatis() },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryRoyalBlue)
                        )
                        Text(
                            text = "Cetak struk otomatis",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(state.printerStatus.bgColor),
                        modifier = Modifier.clickable {
                            if (state.printerStatus != PrinterStatus.SIAP) {
                                viewModel.checkPrinterStatus(context)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Printer siap: ${state.printerName.ifBlank { "Tersambung" }}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(state.printerStatus.dotColor))
                            )
                            Text(
                                text = state.printerStatus.label,
                                fontFamily = interfamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(state.printerStatus.dotColor),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // ── 6. Tombol Konfirmasi Bayar (Item Terakhir di LazyColumn)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.prosesPembayaran() },
                    enabled = !state.isLoading && state.subtotal > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(confirmButtonHeight),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRoyalBlue,
                        disabledContainerColor = Color(0xFFE2E8F0)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Konfirmasi Bayar",
                                fontFamily = interfamily,
                                fontSize = confirmFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "  |  Rp ${formatRupiah(state.grandTotal)}",
                                fontFamily = interfamily,
                                fontSize = confirmFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog Pilihan Sumber Bukti Pembayaran (Kamera vs Galeri)
    GlobalImagePickerDialog(
        show = buktiPickerState.showDialog,
        onDismissRequest = buktiPickerState.closePicker,
        onCameraSelect = buktiPickerState.launchCamera,
        onGallerySelect = buktiPickerState.launchGallery,
        title = "Upload Bukti Pembayaran",
        description = "Pilih metode pengambilan bukti pembayaran non-tunai:"
    )
}
}

@Composable
fun PaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Responsif: ukuran kartu metode pembayaran menyesuaikan lebar layar.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp <= 340
    val cardMinHeight = if (isSmallScreen) 66.dp else 74.dp
    val cardPadding = if (isSmallScreen) 8.dp else 10.dp
    val iconSurfaceSize = if (isSmallScreen) 34.dp else 38.dp
    val iconSize = if (isSmallScreen) 18.dp else 20.dp
    val iconGap = if (isSmallScreen) 6.dp else 8.dp
    val titleSize = if (isSmallScreen) 12.sp else 12.5.sp

    Card(
        modifier = modifier
            .defaultMinSize(minHeight = cardMinHeight)
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
                    .padding(cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBgColor,
                    modifier = Modifier.size(iconSurfaceSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(iconGap))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = interfamily,
                        fontSize = titleSize,
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

            // Selected Checkmark Badge (Persis Gambar)
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryRoyalBlue,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuktiPembayaranCard(
    metodePembayaran: String,
    buktiUri: String?,
    isUploading: Boolean,
    uploadProgress: Float?,
    uploadMessage: String,
    onOpenPicker: () -> Unit = {},
    onPickGallery: () -> Unit,
    onCaptureCamera: () -> Unit,
    onRemoveBukti: () -> Unit
) {
    // Responsif: ukuran kartu bukti pembayaran menyesuaikan lebar layar.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp <= 340
    val cardPadding = if (isSmallScreen) 14.dp else 16.dp
    val imageHeight = if (isSmallScreen) 150.dp else 180.dp
    val actionButtonHeight = if (isSmallScreen) 40.dp else 42.dp
    val headerTitleSize = if (isSmallScreen) 12.5.sp else 13.5.sp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Bukti Pembayaran ($metodePembayaran)",
                            fontFamily = interfamily,
                            fontSize = headerTitleSize,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
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
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (buktiUri != null) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (buktiUri != null) "Terlampir" else "Opsional",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (buktiUri != null) Color(0xFF059669) else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Main Upload Area / Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            ) {
                if (buktiUri != null) {
                    // Preview Image View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imageHeight)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = buktiUri,
                                contentDescription = "Bukti Pembayaran",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status Tag on top-right
                            Surface(
                                shape = RoundedCornerShape(bottomStart = 10.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.75f),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Bukti Valid",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Action Buttons: Ganti Foto (Galeri/Kamera) & Hapus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPickGallery,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(actionButtonHeight),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ubah (Galeri)",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            OutlinedButton(
                                onClick = onCaptureCamera,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(actionButtonHeight),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ubah (Kamera)",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            IconButton(
                                onClick = onRemoveBukti,
                                modifier = Modifier
                                    .size(actionButtonHeight)
                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Bukti",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Empty State: Dua Tombol Pilihan (Galeri & Foto Kamera)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Pilih sumber bukti transaksi non-tunai:",
                                fontFamily = interfamily,
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Opsi 1: Galeri / File
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(86.dp)
                                        .clickable { onPickGallery() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.2.dp, Color(0xFFE0E7FF))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFEEF2FF),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Image,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4F46E5),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Galeri",
                                            fontFamily = interfamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }

                                // Opsi 2: Ambil Foto Kamera
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(86.dp)
                                        .clickable { onCaptureCamera() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF4F46E5),
                                    border = BorderStroke(1.2.dp, Color(0xFF4F46E5))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.2f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Kamera",
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

                // In-place Progress Animation Overlay
                GlobalImageUploadOverlay(
                    isUploading = isUploading,
                    modifier = Modifier.matchParentSize(),
                    progress = uploadProgress,
                    title = "Memproses Bukti Pembayaran...",
                    message = uploadMessage.ifBlank { "Mengompresi otomatis (Maks. 2MB)..." },
                    type = ImageUploadType.BUKTI_PEMBAYARAN,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PembayaranScreenPreview() {
    MaterialTheme {
        PembayaranScreen(navController = rememberNavController())
    }
}
