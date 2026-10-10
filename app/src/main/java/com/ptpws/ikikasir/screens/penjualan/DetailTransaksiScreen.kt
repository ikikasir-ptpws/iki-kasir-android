package com.ptpws.ikikasir.screens.penjualan

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GenerateStrukPdfUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.PrintStrukUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.DetailTransaksiViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransaksiScreen(
    transactionId: String = "",
    onBack: () -> Unit = {},
    onCetakStruk: () -> Unit = {},
    onRefund: () -> Unit = {},
    viewModel: DetailTransaksiViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val printScope = rememberCoroutineScope()
    val printStrukUseCase = remember { PrintStrukUseCase() }
    val generateStrukPdfUseCase = remember { GenerateStrukPdfUseCase() }

    val onSavePdf = {
        val transaksi = state.transaksi
        if (transaksi != null) {
            try {
                val pdfFile = generateStrukPdfUseCase(context, transaksi)
                if (pdfFile != null && pdfFile.exists()) {
                    Toast.makeText(
                        context,
                        "PDF Struk berhasil disimpan di Downloads",
                        Toast.LENGTH_LONG
                    ).show()
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            pdfFile
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, "Buka Struk PDF")
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "Gagal membuat file PDF struk.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Data transaksi belum siap.", Toast.LENGTH_SHORT).show()
        }
    }

    val printReceipt = {
        val transaksi = state.transaksi
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
        onCetakStruk()
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

    LaunchedEffect(transactionId) {
        viewModel.loadTransaksi(transactionId)
    }

    var showRefundConfirmDialog by remember { mutableStateOf(false) }

    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSavePdf) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Simpan PDF",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        }
    ) { paddingValues ->

        val tx = state.transaksi

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                com.ptpws.ikikasir.commond.DetailTransaksiShimmer()
            }
        } else if (tx == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Data transaksi tidak ditemukan",
                    fontFamily = interfamily,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            val dateObj = tx.createdAt.toDate()
            val formattedDateTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(dateObj)
            val kasirName = tx.createdBy.ifBlank { "Admin" }
            val isLunas = tx.status.equals("COMPLETED", ignoreCase = true) || tx.status.equals("LUNAS", ignoreCase = true)
            val isRefunded = tx.status.equals("REFUND", ignoreCase = true) || tx.status.equals("REFUNDED", ignoreCase = true) || tx.status.equals("BATAL", ignoreCase = true)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Card 1: Nomor Transaksi & Status Header (Gambar 1 Layout)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nomor Transaksi",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!tx.isSynced) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Pending",
                                                fontSize = 11.sp,
                                                fontFamily = interfamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when {
                                                    isRefunded -> Color(0xFFFEE2E2)
                                                    isLunas -> Color(0xFFE6F4F1)
                                                    else -> Color(0xFFFEF3C7)
                                                },
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isRefunded -> "REFUND"
                                                isLunas -> "LUNAS"
                                                else -> tx.status
                                            },
                                            fontSize = 11.sp,
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isRefunded -> Color(0xFFDC2626)
                                                isLunas -> Color(0xFF0D9488)
                                                else -> Color(0xFFD97706)
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = tx.transactionNumber,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                fontSize = 22.sp,
                                color = Color(0xFF4F46E5),
                                lineHeight = 28.sp
                            )

                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Tanggal dan Waktu",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = formattedDateTime,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Kasir",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = kasirName,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            if (tx.customerName.isNotBlank()) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pelanggan",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = tx.customerName,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            if (tx.tableNumber.isNotBlank()) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nomor Meja",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = tx.tableNumber,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Card 2: Daftar Produk (Gambar 1 Layout)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Daftar Produk",
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            tx.items.forEachIndexed { index, item ->
                                ProdukCardRow(
                                    namaProduk = item.produk.name,
                                    qty = item.quantity,
                                    hargaSatuan = "Rp ${formatRupiah(item.price)}",
                                    totalHarga = "Rp ${formatRupiah(item.totalPrice)}",
                                    imageUrl = item.produk.imageUrl
                                )

                                if (index < tx.items.size - 1) {
                                    HorizontalDivider(
                                        color = Color(0xFFF1F5F9),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Card 3: Catatan Pesanan (KONDISIONAL - Gambar 1 Layout)
                // Hanya muncul jika catatan diisi oleh kasir/user
                if (tx.notes.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Catatan Pesanan",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                // Box Catatan Kuning Lembut (Gambar 1 Layout)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "\"${tx.notes}\"",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFF92400E),
                                            lineHeight = 18.sp
                                        )

                                        HorizontalDivider(color = Color(0xFFFDE68A), thickness = 1.dp)

                                        Text(
                                            text = "Ditambahkan oleh: $kasirName",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Card 4: Ringkasan Pembayaran (Gambar 1 Layout)
                item {
                    val totalSubtotal = if (tx.subtotal > 0) tx.subtotal else tx.items.sumOf { it.subtotal }
                    val isPpnAktif = tx.isTaxActive()
                    val isPpnEksklusif = tx.checkIsPpnEksklusif()
                    val formattedPercent = tx.getFormattedPercentage()
                    val effectivePpn = tx.getEffectivePpnAmount()
                    val totalPembayaran = if (tx.total > 0) tx.total else (totalSubtotal - tx.discount + (if (isPpnEksklusif) effectivePpn else 0.0)).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RingkasanBaris(
                                label = "Subtotal",
                                nilai = "Rp ${formatRupiah(totalSubtotal)}",
                                nilaiColor = Color(0xFF334155)
                            )

                            if (isPpnAktif && effectivePpn > 0) {
                                RingkasanBaris(
                                    label = "PPN ($formattedPercent)",
                                    nilai = if (isPpnEksklusif) "+ Rp ${formatRupiah(effectivePpn)}" else "Rp ${formatRupiah(effectivePpn)}",
                                    nilaiColor = Color(0xFF334155)
                                )
                            }

                            if (tx.discount > 0) {
                                RingkasanBaris(
                                    label = "Promo/Diskon",
                                    nilai = "- Rp ${formatRupiah(tx.discount)}",
                                    nilaiColor = Color(0xFF059669)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Pembayaran",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Rp ${formatRupiah(totalPembayaran)}",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Metode Pembayaran",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallet,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tx.paymentMethod,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tombol Simpan PDF & Cetak Struk
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tombol Simpan PDF
                        OutlinedButton(
                            onClick = onSavePdf,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF4F46E5)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF4F46E5)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF4F46E5)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simpan PDF",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }

                        // Tombol Cetak Struk
                        Button(
                            onClick = onPrintReceipt,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cetak Struk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // ── Tombol Refund
                item {
                    OutlinedButton(
                        onClick = {
                            if (isRefunded) {
                                Toast.makeText(context, "Transaksi ini sudah di-refund sebelumnya.", Toast.LENGTH_SHORT).show()
                            } else {
                                showRefundConfirmDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isRefunded && !state.isRefunding,
                        border = BorderStroke(
                            1.5.dp,
                            if (isRefunded) Color(0xFFCBD5E1) else Color(0xFFEF4444)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isRefunded) Color(0xFF94A3B8) else Color(0xFFEF4444),
                            disabledContentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (state.isRefunding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memproses Refund...",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFFEF4444)
                            )
                        } else {
                            Icon(
                                imageVector = if (isRefunded) Icons.Default.CheckCircle else Icons.Default.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isRefunded) Color(0xFF94A3B8) else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRefunded) "Transaksi Telah Di-refund" else "Refund",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (isRefunded) Color(0xFF94A3B8) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            // ── Dialog Konfirmasi Refund Transaksi
            if (showRefundConfirmDialog) {
                var refundReason by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = {
                        if (!state.isRefunding) showRefundConfirmDialog = false
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFFEE2E2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Konfirmasi Refund Transaksi",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Apakah Anda yakin ingin memproses refund untuk transaksi ${tx.transactionNumber} sebesar Rp ${formatRupiah(tx.total)}?",
                                fontFamily = interfamily,
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                lineHeight = 20.sp
                            )

                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Status transaksi akan diubah menjadi REFUNDED dan stok untuk ${tx.items.sumOf { it.quantity }} produk akan otomatis dikembalikan ke inventaris.",
                                        fontFamily = interfamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = refundReason,
                                onValueChange = { refundReason = it },
                                label = { Text("Alasan Refund (Opsional)", fontFamily = interfamily, fontSize = 12.sp) },
                                placeholder = { Text("Misal: Pesanan salah / Batal beli", fontFamily = interfamily, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = !state.isRefunding
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.refundTransaksi(
                                    reason = refundReason,
                                    onSuccess = {
                                        showRefundConfirmDialog = false
                                        Toast.makeText(
                                            context,
                                            "Refund berhasil! Status transaksi diperbarui & stok produk dikembalikan.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onRefund()
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !state.isRefunding,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isRefunding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Memproses...", fontFamily = interfamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Text("Ya, Proses Refund", fontFamily = interfamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showRefundConfirmDialog = false },
                            enabled = !state.isRefunding
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = interfamily,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProdukCardRow(
    namaProduk: String,
    qty: Int,
    hargaSatuan: String,
    totalHarga: String,
    imageUrl: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = namaProduk,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = namaProduk,
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "x$qty @ $hargaSatuan",
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Text(
            text = totalHarga,
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
fun RingkasanBaris(
    label: String,
    nilai: String,
    nilaiColor: Color = Color(0xFF334155)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = nilai,
            fontFamily = interfamily,
            fontSize = 13.sp,
            color = nilaiColor
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailTransaksiScreenPreview() {
    MaterialTheme {
        DetailTransaksiScreen()
    }
}