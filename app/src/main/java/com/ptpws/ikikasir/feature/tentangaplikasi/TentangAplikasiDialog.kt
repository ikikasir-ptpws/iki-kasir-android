package com.ptpws.ikikasir.feature.tentangaplikasi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

// ── Model Data Dummy (Tergabung dalam Satu File) ──

data class TentangAplikasiInfo(
    val namaAplikasi: String,
    val tagline: String,
    val versi: String,
    val pengembang: String,
    val deskripsiLengkap: String,
    val fiturList: List<FiturAplikasiItem>,
    val keunggulanList: List<String>,
    val copyright: String
)

data class FiturAplikasiItem(
    val judul: String,
    val deskripsiSingkat: String,
    val tagKategori: String,
    val ikonType: String
)

// ── Sumber Data Dummy ──

private val dummyTentangAplikasiInfo = TentangAplikasiInfo(
    namaAplikasi = "IkiKasir",
    tagline = "Aplikasi Kasir Pintar & Manajemen Usaha",
    versi = "Versi 1.0",
    pengembang = "PT PWS (Putra Wisanggeni Satu)",
    deskripsiLengkap = "IkiKasir adalah aplikasi kasir pintar (Point of Sale) modern yang dirancang khusus untuk mempermudah operasional usaha Anda. Sangat cocok untuk toko kelontong, retail, kafe, resto, minimarket, hingga aneka jenis usaha UMKM dalam mengelola penjualan, stok barang, cetak nota, hingga pembukuan keuangan secara cepat dan tanpa ribet.",
    fiturList = listOf(
        FiturAplikasiItem(
            judul = "Kasir Cepat & Praktis",
            deskripsiSingkat = "Proses transaksi penjualan dalam hitungan detik, pencarian produk instan, kalkulasi otomatis diskon & PPN, serta dukungan nomor antrean pesanan.",
            tagKategori = "Transaksi",
            ikonType = "KASIR"
        ),
        FiturAplikasiItem(
            judul = "Manajemen Produk & Stok",
            deskripsiSingkat = "Kelola katalog produk, foto barang, harga modal (HPP) & harga jual, kategori, serta pantau sisa stok dengan peringatan saat persediaan menipis.",
            tagKategori = "Katalog",
            ikonType = "PRODUK"
        ),
        FiturAplikasiItem(
            judul = "Multi-Metode Pembayaran",
            deskripsiSingkat = "Dukungan pembayaran lengkap: Tunai dengan hitungan kembalian otomatis, QRIS, Transfer Bank, dan Kartu Debit/Kredit.",
            tagKategori = "Bayar",
            ikonType = "PEMBAYARAN"
        ),
        FiturAplikasiItem(
            judul = "Cetak Struk & Printer Bluetooth",
            deskripsiSingkat = "Hubungkan ke printer thermal bluetooth untuk cetak nota instan dengan logo toko, rincian PPN, catatan, hingga info WiFi pelanggan.",
            tagKategori = "Printer",
            ikonType = "PRINTER"
        ),
        FiturAplikasiItem(
            judul = "Laporan Penjualan & Keuangan",
            deskripsiSingkat = "Analisis omzet harian, laba kotor & laba bersih, ranking produk terlaris, serta ekspor laporan berkas rapi ke Excel (.xlsx).",
            tagKategori = "Analitik",
            ikonType = "LAPORAN"
        ),
        FiturAplikasiItem(
            judul = "Fitur Refund & Batal Transaksi",
            deskripsiSingkat = "Kelola pengembalian dana transaksi (refund) dengan transparansi status di Room DB & Cloud, menjaga pembukuan tetap seimbang.",
            tagKategori = "Pengembalian",
            ikonType = "REFUND"
        ),
        FiturAplikasiItem(
            judul = "Bisa Digunakan Saat Offline",
            deskripsiSingkat = "Kasir tetap berfungsi melayani pembeli walau tanpa internet. Data tersimpan aman di HP dan otomatis tersinkronisasi saat koneksi kembali aktif.",
            tagKategori = "Keandalan",
            ikonType = "CLOUD"
        ),
        FiturAplikasiItem(
            judul = "Audit Log & Hak Akses",
            deskripsiSingkat = "Keamanan terjaga dengan pembatasan hak akses kasir/admin serta pencatatan audit log otomatis untuk setiap aktivitas penting.",
            tagKategori = "Keamanan",
            ikonType = "KEAMANAN"
        )
    ),
    keunggulanList = listOf(
        "Tampilan modern, bersih, dan sangat mudah digunakan oleh siapa saja",
        "Mendukung printer thermal bluetooth ukuran 58mm & 80mm",
        "Penyimpanan ganda lokal (Room DB) & Cloud (Firestore)",
        "Ekspor laporan lengkap langsung ke file Excel di memori HP",
        "Bisa digunakan tanpa internet (Full Offline Support)"
    ),
    copyright = "© 2026 PT PWS. Dibuat dengan presisi untuk memajukan UMKM Indonesia."
)

// ── Komponen Pop-up Dialog ──

@Composable
fun TentangAplikasiDialog(
    onDismiss: () -> Unit
) {
    val info = dummyTentangAplikasiInfo
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ── HEADER ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logoikikasir),
                                    contentDescription = "Logo IkiKasir",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = info.namaAplikasi,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFECFDF5)
                                ) {
                                    Text(
                                        text = info.versi,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = info.tagline,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // ── SCROLLABLE BODY ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Intro Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Apa itu IkiKasir?",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.deskripsiLengkap,
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Section: Fitur Utama
                    Text(
                        text = "Fitur & Kemudahan IkiKasir",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    // Fitur Cards
                    info.fiturList.forEach { fitur ->
                        FiturItemCard(fitur = fitur)
                    }

                    // Section: Keunggulan
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "✨ Keunggulan Utama",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            info.keunggulanList.forEach { keunggulan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Text(
                                        text = keunggulan,
                                        fontFamily = interfamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF14532D),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // Section: Developer & Copyright
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Dikembangkan oleh:",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = info.pengembang,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = info.copyright,
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // ── FOOTER ACTION ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Saya Mengerti",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiturItemCard(fitur: FiturAplikasiItem) {
    val (icon, bgCol, tintCol) = when (fitur.ikonType) {
        "KASIR" -> Triple(Icons.Default.ShoppingCart, Color(0xFFEEF2FF), Color(0xFF4F46E5))
        "PRODUK" -> Triple(Icons.Default.Inventory2, Color(0xFFECFDF5), Color(0xFF10B981))
        "PEMBAYARAN" -> Triple(Icons.Default.Payment, Color(0xFFFFFBEB), Color(0xFFF59E0B))
        "PRINTER" -> Triple(Icons.Default.Print, Color(0xFFF3E8FF), Color(0xFF9333EA))
        "LAPORAN" -> Triple(Icons.Default.Assessment, Color(0xFFE0F2FE), Color(0xFF0284C7))
        "REFUND" -> Triple(Icons.Default.Replay, Color(0xFFFFEDD5), Color(0xFFEA580C))
        "CLOUD" -> Triple(Icons.Default.Sync, Color(0xFFF0FDF4), Color(0xFF16A34A))
        else -> Triple(Icons.Default.Shield, Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgCol),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintCol,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fitur.judul,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = fitur.tagKategori,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = fitur.deskripsiSingkat,
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 17.sp
                )
            }
        }
    }
}
