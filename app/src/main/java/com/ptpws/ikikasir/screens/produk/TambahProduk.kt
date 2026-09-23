package com.ptpws.ikikasir.screens.produk

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriViewModel
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.TambahProdukViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(
    onBack: () -> Unit = {},
    viewModel: TambahProdukViewModel = hiltViewModel(),
    kategoriViewModel: KategoriViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val kategoriState by kategoriViewModel.state.collectAsState()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPrintDialog by remember { mutableStateOf(false) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileSizeInBytes = inputStream?.available() ?: 0
                inputStream?.close()

                val maxSizeBytes = 2 * 1024 * 1024 // 2MB
                if (fileSizeInBytes > maxSizeBytes) {
                    Toast.makeText(context, "Ukuran foto terlalu besar. Maksimal 2MB!", Toast.LENGTH_LONG).show()
                } else {
                    val permanentUriString = saveImageToInternalStorage(context, uri)
                    val permanentUri = Uri.parse(permanentUriString)
                    imageUri = permanentUri
                    viewModel.onImageUrlChange(permanentUriString)
                }
            } catch (e: Exception) {
                val permanentUriString = saveImageToInternalStorage(context, uri)
                val permanentUri = Uri.parse(permanentUriString)
                imageUri = permanentUri
                viewModel.onImageUrlChange(permanentUriString)
            }
        }
    }

    val barcodeScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCAN_RESULT")
            if (!scannedCode.isNullOrBlank()) {
                viewModel.onBarcodeChange(scannedCode)
            }
        }
    }

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            Toast.makeText(context, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onBack()
        }
    }

    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditMode) "Edit Produk" else "Tambah Produk",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Dotted Container Upload Foto Produk
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null || formState.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUri ?: formState.imageUrl,
                                contentDescription = "Foto Produk",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White, RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload",
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Tambah Foto Produk (opsional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = interfamily,
                                    color = Color(0xFF374151)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Format JPG, PNG atau HEIC. Maks 2MB.",
                    fontSize = 11.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Nama Produk
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nama Produk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = Color(0xFF374151)
                    )
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        placeholder = { Text("Contoh: Kopi Susu Gula Aren", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF4F46E5)
                        )
                    )
                }
            }

            // Barcode / Kode Produk (opsional) + Scan + Generate Otomatis
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Barcode / Kode Produk ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "(opsional)",
                                fontSize = 12.sp,
                                fontFamily = interfamily,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        Text(
                            text = "Generate Otomatis",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.clickable {
                                val randomBarcode = "899" + (100000000..999999999).random()
                                viewModel.onBarcodeChange(randomBarcode)
                            }
                        )
                    }

                    OutlinedTextField(
                        value = formState.barcode,
                        onValueChange = { viewModel.onBarcodeChange(it) },
                        placeholder = { Text("Contoh: 8992753210123", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (formState.barcode.isNotBlank()) {
                                // Barcode sudah ada → tampil tombol Cetak
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .clickable { showPrintDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Print,
                                            contentDescription = "Cetak",
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Cetak",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                }
                            } else {
                                // Barcode kosong → tampil tombol Scan
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .clickable {
                                            triggerBarcodeScanner(context, barcodeScanLauncher) { code ->
                                                viewModel.onBarcodeChange(code)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Scan",
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Scan",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = Color(0xFF4F46E5)
                        )
                    )
                }
            }

            // Harga Beli & Harga Jual + Card HPP & Estimasi Laba Kotor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Harga Beli",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF374151)
                                )
                                OutlinedTextField(
                                    value = formState.costPrice,
                                    onValueChange = { viewModel.onCostPriceChange(it) },
                                    leadingIcon = {
                                        Text("Rp", fontSize = 14.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                                    },
                                    placeholder = { Text("0", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedBorderColor = Color(0xFF4F46E5)
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Harga Jual",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF374151)
                                )
                                OutlinedTextField(
                                    value = formState.sellingPrice,
                                    onValueChange = { viewModel.onSellingPriceChange(it) },
                                    leadingIcon = {
                                        Text("Rp", fontSize = 14.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                                    },
                                    placeholder = { Text("0", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        focusedBorderColor = Color(0xFF4F46E5)
                                    )
                                )
                            }
                        }

                        // Box Estimasi Laba Kotor & HPP
                        val costPriceDouble = formState.costPrice.toDoubleOrNull() ?: 0.0
                        val sellingPriceDouble = formState.sellingPrice.toDoubleOrNull() ?: 0.0
                        val labaKotor = sellingPriceDouble - costPriceDouble
                        val marginPercent = if (sellingPriceDouble > 0) (labaKotor / sellingPriceDouble * 100) else 0.0
                        val isProfitable = labaKotor >= 0

                        val formattedCostPrice = try {
                            NumberFormat.getNumberInstance(Locale("id", "ID")).format(costPriceDouble.toLong())
                        } catch (e: Exception) {
                            "0"
                        }
                        val formattedLabaKotor = try {
                            NumberFormat.getNumberInstance(Locale("id", "ID")).format(kotlin.math.abs(labaKotor).toLong())
                        } catch (e: Exception) {
                            "0"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isProfitable) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isProfitable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Harga Modal (HPP)",
                                        fontSize = 11.sp,
                                        fontFamily = interfamily,
                                        color = Color(0xFF6B7280)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Rp $formattedCostPrice",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = Color(0xFF111827)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Estimasi Laba Kotor",
                                            fontSize = 11.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFF6B7280)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isProfitable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = if (isProfitable) String.format(Locale.US, "+%.1f%%", marginPercent) else String.format(Locale.US, "%.1f%%", marginPercent),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interfamily,
                                                color = if (isProfitable) Color(0xFF16A34A) else Color(0xFFDC2626),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isProfitable) "+Rp $formattedLabaKotor / unit" else "-Rp $formattedLabaKotor / unit",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = if (isProfitable) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stok & Batas Stok (Default 5)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Stok",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interfamily,
                            color = Color(0xFF374151)
                        )
                        OutlinedTextField(
                            value = formState.stock,
                            onValueChange = { viewModel.onStockChange(it) },
                            placeholder = { Text("0", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = Color(0xFF4F46E5)
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Batas Stok ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "(Default 5)",
                                fontSize = 12.sp,
                                fontFamily = interfamily,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        OutlinedTextField(
                            value = formState.lowStockThreshold,
                            onValueChange = { viewModel.onLowStockThresholdChange(it) },
                            placeholder = { Text("5", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedBorderColor = Color(0xFF4F46E5)
                            )
                        )
                    }
                }
            }

            // Pilih Kategori Produk
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pilih Kategori Produk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = Color(0xFF374151)
                    )
                    val categories = if (kategoriState.kategoriList.isNotEmpty()) {
                        kategoriState.kategoriList.map { it.id to it.name }
                    } else {
                        listOf("makanan" to "Makanan", "minuman" to "Minuman", "snack" to "Snack", "dessert" to "Dessert", "buah" to "Buah")
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories.size) { idx ->
                            val (catId, catName) = categories[idx]
                            val isSelected = catId == formState.categoryId && formState.categoryId.isNotBlank()
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategoryIdChange(catId) },
                                label = {
                                    Text(
                                        text = catName,
                                        fontSize = 13.sp,
                                        fontFamily = interfamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4F46E5),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF374151)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Color.Transparent,
                                    borderColor = Color(0xFFE5E7EB),
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 0.dp
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }




            // Visibilitas Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Visibilitas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = Color(0xFF1F2937)
                            )
                        }

                        // Toggle row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PointOfSale,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tampilkan di Kasir",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Langsung muncul pada layar pemesanan kasir",
                                        fontSize = 11.sp,
                                        fontFamily = interfamily,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                Switch(
                                    checked = formState.isVisibleInCashier,
                                    onCheckedChange = { viewModel.onVisibilityChange(it) },
                                    modifier = Modifier.scale(scale = 0.7f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF059669),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFE5E7EB)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            // Bottom Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151))
                    ) {
                        Text("Batal", fontFamily = interfamily, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { viewModel.simpanProduk() },
                        enabled = !formState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        if (formState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (formState.isEditMode) "Perbarui Produk" else "Tambah Produk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Cetak Barcode & QR Code
    if (showPrintDialog) {
        CetakBarcodeDialog(
            barcode = formState.barcode,
            productName = formState.name.ifBlank { "Produk" },
            onDismiss = { showPrintDialog = false },
            onPrint = { bmp ->
                printBarcodePdf(context, bmp, formState.name.ifBlank { "Produk" })
            },
            onSavePdf = { bmp ->
                saveBarcodeAsPdf(context, formState.barcode, formState.name.ifBlank { "Produk" }, bmp)
            }
        )
    }
}

// ─── Helper: Generate Barcode (Code128) Bitmap via ZXing ────────────────────
private fun generateBarcodeBitmap(content: String, width: Int = 900, height: Int = 300): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content, BarcodeFormat.CODE_128, width, height, hints
        )
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) { null }
}

// ─── Helper: Generate QR Code Bitmap via ZXing ───────────────────────────────
private fun generateQrBitmap(content: String, size: Int = 400): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content, BarcodeFormat.QR_CODE, size, size, hints
        )
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) { null }
}

// ─── Helper: Print via Android PrintManager ──────────────────────────────────
private fun printBarcodePdf(context: Context, bitmap: Bitmap, productName: String) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Barcode - $productName"
        val printAdapter = object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val pdfDoc = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDoc.startPage(pageInfo)
                drawBarcodePageToCanvas(page.canvas, bitmap, productName)
                pdfDoc.finishPage(page)
                pdfDoc.writeTo(FileOutputStream(destination!!.fileDescriptor))
                pdfDoc.close()
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            }
        }
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ─── Helper: Gambar konten barcode ke Canvas (dipakai saat print & PDF) ──────
private fun drawBarcodePageToCanvas(canvas: Canvas, barcodeBitmap: Bitmap, productName: String) {
    val pageWidth = canvas.width.toFloat()
    // Judul produk
    val titlePaint = Paint().apply {
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
        color = android.graphics.Color.BLACK
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(productName, pageWidth / 2f, 80f, titlePaint)

    // Gambar barcode di tengah halaman
    val barcodeScaled = Bitmap.createScaledBitmap(barcodeBitmap, 480, 160, false)
    val barcodeLeft = (pageWidth - 480) / 2f
    canvas.drawBitmap(barcodeScaled, barcodeLeft, 120f, null)

    // Label kode barcode di bawah barcode
    val codePaint = Paint().apply {
        textSize = 20f
        color = android.graphics.Color.BLACK
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.15f
    }
    // Ambil teks barcode dari bitmap tidak tersedia, di-pass lewat caller
}

// ─── Helper: Save PDF ke Downloads ───────────────────────────────────────────
private fun saveBarcodeAsPdf(context: Context, barcode: String, productName: String, bitmap: Bitmap) {
    try {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        val pageWidth = canvas.width.toFloat()

        // Judul
        val titlePaint = Paint().apply {
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(productName, pageWidth / 2f, 80f, titlePaint)

        // Barcode image
        val barcodeScaled = Bitmap.createScaledBitmap(bitmap, 480, 160, false)
        canvas.drawBitmap(barcodeScaled, (pageWidth - 480) / 2f, 120f, null)

        // Kode barcode text
        val codePaint = Paint().apply {
            textSize = 18f
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        canvas.drawText(barcode, pageWidth / 2f, 300f, codePaint)

        // Generate QR dan gambar
        val qrBmp = generateQrBitmap(barcode, 300)
        if (qrBmp != null) {
            canvas.drawBitmap(qrBmp, (pageWidth - 300) / 2f, 340f, null)
            val qrLabelPaint = Paint().apply {
                textSize = 16f
                color = android.graphics.Color.DKGRAY
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("QR Code", pageWidth / 2f, 660f, qrLabelPaint)
        }

        pdfDoc.finishPage(page)

        // Simpan ke Downloads
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val fileName = "barcode_${productName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)
        pdfDoc.writeTo(FileOutputStream(file))
        pdfDoc.close()

        Toast.makeText(context, "PDF disimpan di Downloads/$fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ─── Camera Barcode Scanner Launcher (ML Kit) ────────────────────────────────
private fun triggerBarcodeScanner(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onResult: (String) -> Unit
) {
    try {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrBlank()) {
                    onResult(rawValue)
                    Toast.makeText(context, "Barcode berhasil discan: $rawValue", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCanceledListener { }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal scan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka scanner: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ─── Save uploaded product image to internal storage ─────────────────────────
private fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val imagesDir = File(context.filesDir, "product_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val fileName = "img_" + System.currentTimeMillis() + ".jpg"
        val destinationFile = File(imagesDir, fileName)
        FileOutputStream(destinationFile).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        Uri.fromFile(destinationFile).toString()
    } catch (e: Exception) {
        uri.toString()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ─── Composable: Dialog Cetak Barcode & QR Code ───────────────────────────────
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CetakBarcodeDialog(
    barcode: String,
    productName: String,
    onDismiss: () -> Unit,
    onPrint: (Bitmap) -> Unit,
    onSavePdf: (Bitmap) -> Unit
) {
    val barcodeBitmap = remember(barcode) { generateBarcodeBitmap(barcode) }
    val qrBitmap = remember(barcode) { generateQrBitmap(barcode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cetak Barcode & QR",
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = productName,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✕", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Barcode Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Barcode (Code128)",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    if (barcodeBitmap != null) {
                        Image(
                            bitmap = barcodeBitmap.asImageBitmap(),
                            contentDescription = "Barcode",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gagal generate barcode", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Text(
                        text = barcode,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A),
                        letterSpacing = 2.sp
                    )
                }
            }

            // QR Code Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "QR Code",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(160.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gagal generate QR", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Simpan PDF
                OutlinedButton(
                    onClick = {
                        val bmpForPdf = barcodeBitmap ?: return@OutlinedButton
                        onSavePdf(bmpForPdf)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF4F46E5)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5))
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simpan PDF",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Cetak via Printer
                Button(
                    onClick = {
                        val bmpForPrint = barcodeBitmap ?: return@Button
                        onPrint(bmpForPrint)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cetak",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}