package com.ptpws.ikikasir.screens.penjualan

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import android.content.Intent
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectivePrice
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.HiltKasirUiProvider
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirViewModel
import com.ptpws.ikikasir.screens.penjualan.component.PilihProdukTersediaDialog
import com.ptpws.ikikasir.screens.penjualan.component.StokTidakCukupDialog
import com.ptpws.ikikasir.commond.BarcodeMismatchDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Primary Royal Blue Brand Color matching Gambar
private val PrimaryRoyalBlue = Color(0xFF3B32D1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    navController: NavController,
    onScanProduk: () -> Unit = {},
    onBayar: () -> Unit = {},
    viewModel: KasirUiProvider = HiltKasirUiProvider(hiltViewModel<KasirViewModel>())
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val cartItems = state.cartItems
    val totalItemCount = state.totalItemCount
    val subtotal = state.subtotal

    val triggerBarcodeScanner = com.ptpws.ikikasir.core.util.rememberBarcodeScanner { code ->
        viewModel.onBarcodeScanned(code)
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshKatalog()
    }

    state.errorMessage?.let { message ->
        BarcodeMismatchDialog(
            message = "$message. Pastikan kode yang dipindai adalah barcode atau QR produk yang terdaftar.",
            onDismiss = viewModel::clearError
        )
    }

    val windowSizeInfo = com.ptpws.ikikasir.core.responsive.presentation.util.LocalWindowSizeInfo.current

    if (windowSizeInfo.shouldUseSplitScreen) {
        KasirSplitScreen(
            state = state,
            viewModel = viewModel,
            navController = navController,
            onBayar = onBayar,
            onScanBarcode = { triggerBarcodeScanner() }
        )
    } else {
        KasirCompactScreen(
            state = state,
            viewModel = viewModel,
            navController = navController,
            onBayar = onBayar,
            triggerBarcodeScanner = { triggerBarcodeScanner() }
        )
    }
}

// ── Custom Dashed Border Button (+ Tambah Produk Lainnya) Rata Kanan Fixed Sticky
@Composable
fun DashedTambahProdukButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stroke = Stroke(
        width = 2.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    val borderColor = Color(0xFFC7D2FE)

    // Responsif: ukuran tombol menyesuaikan lebar layar agar tetap rapi di HP sempit.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp <= 340
    val buttonHPadding = if (isSmallScreen) 10.dp else 14.dp
    val buttonVPadding = if (isSmallScreen) 6.dp else 8.dp
    val circleSize = if (isSmallScreen) 22.dp else 26.dp
    val circleIconSize = if (isSmallScreen) 13.dp else 16.dp
    val textSize = if (isSmallScreen) 12.sp else 13.sp
    val innerSpacing = if (isSmallScreen) 6.dp else 8.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = CornerRadius(100.dp.toPx())
                )
            }
            .clickable { onClick() }
            .padding(horizontal = buttonHPadding, vertical = buttonVPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(innerSpacing)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryRoyalBlue,
                modifier = Modifier.size(circleSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(circleIconSize)
                    )
                }
            }
            Text(
                text = "Tambah Produk Lainnya",
                fontFamily = interfamily,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                color = PrimaryRoyalBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    categoryName: String,
    taxSetting: TaxSetting = TaxSetting(),
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSetQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    formatRupiah: (Double) -> String
) {
    var quantityText by remember(item.produk.id) { mutableStateOf(item.quantity.toString()) }
    var wasQuantityFocused by remember(item.produk.id) { mutableStateOf(false) }
    var showStockWarning by remember(item.produk.id) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Responsif: token ukuran menyesuaikan lebar layar (HP sempit 320dp) agar
    // isi kartu tidak terpotong. Pada lebar standar (>= 360dp) nilai tetap sama.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp <= 340
    val cardPadding = if (isSmallScreen) 10.dp else 12.dp
    val imageSize = if (isSmallScreen) 56.dp else 70.dp
    val imageCorner = if (isSmallScreen) 10.dp else 14.dp
    val imageIconSize = if (isSmallScreen) 22.dp else 28.dp
    val itemGap = if (isSmallScreen) 8.dp else 12.dp
    val nameFontSize = if (isSmallScreen) 13.sp else 14.sp
    val priceFontSize = if (isSmallScreen) 13.sp else 15.sp
    val counterBtnSize = if (isSmallScreen) 24.dp else 26.dp
    val counterIconSize = if (isSmallScreen) 12.dp else 14.dp
    val qtyFieldWidth = if (isSmallScreen) 40.dp else 48.dp
    val qtyFieldHeight = if (isSmallScreen) 28.dp else 32.dp
    val counterSpacing = if (isSmallScreen) 4.dp else 6.dp

    LaunchedEffect(item.quantity) {
        if (!wasQuantityFocused) {
            quantityText = item.quantity.toString()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Surface(
                shape = RoundedCornerShape(imageCorner),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(imageSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.produk.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.produk.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_box_archive),
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(imageIconSize)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(itemGap))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                // Name & Close [X] Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.produk.name,
                        fontFamily = interfamily,
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus Item",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onRemove() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Category Pill Badge + Stock status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Dark Pill Badge
                    if (categoryName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF525252)
                        ) {
                            Text(
                                text = categoryName,
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Stock status badge
                    val isLowStock = item.produk.stock <= item.produk.lowStockThreshold
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLowStock) Color(0xFFFFE4E6) else Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Sisa ${item.produk.stock} Unit",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFE11D48) else Color(0xFF16A34A),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.note.isNotBlank()) {
                        Text(
                            text = "• ${item.note}",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Counter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rp ${formatRupiah(item.getEffectivePrice(taxSetting))}",
                        fontFamily = interfamily,
                        fontSize = priceFontSize,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRoyalBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Counter Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(counterSpacing)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(counterBtnSize)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onDecrement() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurangi",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(counterIconSize)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .width(qtyFieldWidth)
                                    .height(qtyFieldHeight),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                BasicTextField(
                                    value = quantityText,
                                    onValueChange = { input ->
                                        if (input.all { it in '0'..'9' }) {
                                            quantityText = input
                                            val requestedQty = input.toIntOrNull()
                                            if (requestedQty != null) {
                                                if (requestedQty > item.produk.stock) {
                                                    showStockWarning = true
                                                    focusManager.clearFocus()
                                                } else {
                                                    onSetQuantity(requestedQty)
                                                }
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                wasQuantityFocused = true
                                                coroutineScope.launch {
                                                    bringIntoViewRequester.bringIntoView()
                                                }
                                            } else if (wasQuantityFocused) {
                                                wasQuantityFocused = false
                                                val requestedQty = quantityText.toIntOrNull()
                                                if (requestedQty != null && requestedQty <= item.produk.stock) {
                                                    onSetQuantity(requestedQty)
                                                } else {
                                                    quantityText = item.quantity.toString()
                                                }
                                            }
                                        },
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            innerTextField()
                                        }
                                    }
                                )
                            }

                            val canIncrement = item.quantity < item.produk.stock
                            Box(
                                modifier = Modifier
                                    .size(counterBtnSize)
                                    .clip(CircleShape)
                                    .background(if (canIncrement) PrimaryRoyalBlue else Color(0xFFCBD5E1))
                                    .clickable {
                                        if (canIncrement) onIncrement() else showStockWarning = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = Color.White,
                                    modifier = Modifier.size(counterIconSize)
                                )
                            }
                        }
                    }
                }
            }

            if (showStockWarning) {
                StokTidakCukupDialog(
                    namaProduk = item.produk.name,
                    stokTersedia = item.produk.stock,
                    onGunakanStok = {
                        showStockWarning = false
                        quantityText = item.produk.stock.toString()
                        onSetQuantity(item.produk.stock)
                    },
                    onUbahJumlah = {
                        showStockWarning = false
                        quantityText = item.quantity.toString()
                    },
                    onDismiss = {
                        showStockWarning = false
                        quantityText = item.quantity.toString()
                    }
                )
            }
        }
    }
}

@com.ptpws.ikikasir.core.responsive.presentation.components.AllDevicesResponsivePreviews
@Composable
fun KasirScreenPreview() {
    MaterialTheme {
        KasirScreen(navController = rememberNavController())
    }
}