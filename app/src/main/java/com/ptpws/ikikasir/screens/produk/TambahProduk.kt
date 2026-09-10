package com.ptpws.ikikasir.screens.produk

import java.io.File
import java.io.FileOutputStream

import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode



import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriViewModel
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.TambahProdukViewModel

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

            // Harga Jual & Stok
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            value = formState.price,
                            onValueChange = { viewModel.onPriceChange(it) },
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
                            val isSelected = catId == formState.categoryId || (formState.categoryId.isBlank() && idx == 0)
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
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // Pengaturan Diskon Card
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pengaturan Diskon",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = Color(0xFF111827)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Diskon Produk ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = interfamily,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "*opsional",
                                    fontSize = 12.sp,
                                    fontFamily = interfamily,
                                    color = Color(0xFFEF4444)
                                )
                            }

                            OutlinedTextField(
                                value = formState.discount,
                                onValueChange = { viewModel.onDiscountChange(it) },
                                placeholder = { Text("0", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    Row(
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF3F4F6))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (formState.discountType == "PERCENT") Color(0xFF4F46E5) else Color.Transparent,
                                            modifier = Modifier.clickable { viewModel.onDiscountTypeChange("PERCENT") }
                                        ) {
                                            Text(
                                                text = "%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (formState.discountType == "PERCENT") Color.White else Color(0xFF6B7280),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (formState.discountType == "FIXED") Color(0xFF4F46E5) else Color.Transparent,
                                            modifier = Modifier.clickable { viewModel.onDiscountTypeChange("FIXED") }
                                        ) {
                                            Text(
                                                text = "Rp",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (formState.discountType == "FIXED") Color.White else Color(0xFF6B7280),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
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
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simpan Produk",
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
}

// Camera Barcode Scanner Launcher using GmsBarcodeScanning (ML Kit Code Scanner)
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
                    Toast.makeText(context, "Barcode berhasil discan: " + rawValue, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCanceledListener {
                // Scan dibatalkan pengguna
            }
            .addOnFailureListener { e ->
                val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
                    putExtra("SCAN_MODE", "PRODUCT_MODE")
                }
                if (scanIntent.resolveActivity(context.packageManager) != null) {
                    launcher.launch(scanIntent)
                } else {
                    Toast.makeText(context, "Gagal membuka kamera scanner: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
    } catch (e: Exception) {
        val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
            putExtra("SCAN_MODE", "PRODUCT_MODE")
        }
        if (scanIntent.resolveActivity(context.packageManager) != null) {
            launcher.launch(scanIntent)
        } else {
            Toast.makeText(context, "Gagal membuka kamera scanner: " + e.message, Toast.LENGTH_LONG).show()
        }
    }
}
// Save uploaded image permanently to internal storage so it persists across emulator restarts
private fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val imagesDir = File(context.filesDir, "product_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val fileName = "img_" + System.currentTimeMillis() + ".jpg"
        val destinationFile = File(imagesDir, fileName)
        val outputStream = FileOutputStream(destinationFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destinationFile).toString()
    } catch (e: Exception) {
        uri.toString()
    }
}