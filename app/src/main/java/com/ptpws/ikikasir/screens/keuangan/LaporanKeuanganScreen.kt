package com.ptpws.ikikasir.screens.keuangan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent  // ← GANTI KE SINI
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(
    onBack: () -> Unit = {},
    onLihatSemuaProduk: () -> Unit = {}
) {
    val hariLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    val chartModel = remember {
        ChartEntryModelProducer(
            listOf(
                listOf(
                    entryOf(0, 12f),
                    entryOf(1, 8.5f),
                    entryOf(2, 15f),
                    entryOf(3, 0f),
                    entryOf(4, 9f),
                    entryOf(5, 6.5f),
                    entryOf(6, 4.2f)
                ),
                listOf(
                    entryOf(0, 0f),
                    entryOf(1, 0f),
                    entryOf(2, 0f),
                    entryOf(3, 42.85f),
                    entryOf(4, 0f),
                    entryOf(5, 0f),
                    entryOf(6, 0f)
                )
            )
        ).getModel()!!
    }

    val bottomAxisFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            hariLabels.getOrElse(value.toInt()) { "" }
        }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Keuangan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.Black,
                        fontSize = 20.sp
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
                )
            )
        }
    ) { paddingValues ->

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Filter Tanggal
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "7 Hari Terakhir",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pilih Tanggal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF4F46E5)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Card: Omzet Total
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "OMZET TOTAL",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Rp 42.850.000",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+12.4%",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "vs minggu lalu",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                        )
                    }
                }
            }

            // Card: Laba Bersih
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LABA BERSIH",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Rp 15.230.500",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF111827)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+5.2%",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF059669)
                                )
                                Text(
                                    text = "dari kemarin",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFF3F4F6),
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        )
                    }
                }
            }

            // Card: Grafik Performa
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Grafik Performa",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )

                        Chart(
                            chart = columnChart(
                                columns = listOf(
                                    lineComponent(
                                        color = Color(0xFFE0E7FF),
                                        thickness = 20.dp,
                                        shape = com.patrykandpatrick.vico.core.component.shape.Shapes.rectShape
                                    ),
                                    lineComponent(
                                        color = Color(0xFF4F46E5),
                                        thickness = 20.dp,
                                        shape =  com.patrykandpatrick.vico.core.component.shape.Shapes.rectShape
                                    )
                                ),
                                spacing = 8.dp,
                                mergeMode = ColumnChart.MergeMode.Stack
                            ),
                            model = chartModel,
                            startAxis = rememberStartAxis(
                                itemPlacer = remember {
                                    AxisItemPlacer.Vertical.default(maxItemCount = 4)
                                },
                                guideline = null,
                                tick = null
                            ),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = bottomAxisFormatter,
                                guideline = null,
                                tick = null
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            //Header Produk Terlaris
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Produk Terlaris",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Lihat Semua",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.clickable { onLihatSemuaProduk() }
                    )
                }
            }

            // Card: Produk Terlaris
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        ProdukTerlarisItem(
                            namaProduk = "Red Hyper Runner v2",
                            sku = "SKU: SHOE-RED-42",
                            jumlahTerjual = "124 Unit",
                            persentase = "+15%",
                            imagePainter = painterResource(R.drawable.kopi)
                        )
                        HorizontalDivider(
                            color = Color(0xFFF3F4F6),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        ProdukTerlarisItem(
                            namaProduk = "Essential Silver Watch",
                            sku = "SKU: WTC-SIL-MIN",
                            jumlahTerjual = "98 Unit",
                            persentase = "+2%",
                            imagePainter = painterResource(R.drawable.kopi)
                        )
                        HorizontalDivider(
                            color = Color(0xFFF3F4F6),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        ProdukTerlarisItem(
                            namaProduk = "Acoustic Pro Headphones",
                            sku = "SKU: AUD-HP-BLK",
                            jumlahTerjual = "76 Unit",
                            persentase = "+8%",
                            imagePainter = painterResource(R.drawable.kopi)
                        )
                        HorizontalDivider(
                            color = Color(0xFFF3F4F6),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        ProdukTerlarisItem(
                            namaProduk = "Sport Running Shoes",
                            sku = "SKU: SHOE-BLK-40",
                            jumlahTerjual = "61 Unit",
                            persentase = "+3%",
                            imagePainter = painterResource(R.drawable.kopi)
                        )
                    }
                }
            }
        }
    }
}

//Composable: Item produk terlaris

@Composable
fun ProdukTerlarisItem(
    namaProduk: String,
    sku: String,
    jumlahTerjual: String,
    persentase: String,
    imagePainter: Painter? = null
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            if (imagePainter != null) {
                Image(
                    painter =  imagePainter,
                    contentDescription = namaProduk,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = null,
                    tint = Color(0xFFD1D5DB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = namaProduk,
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sku,
                fontFamily = interfamily,
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = jumlahTerjual,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF111827)
            )
            Text(
                text = persentase,
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color(0xFF059669)
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun LaporanKeuanganScreenPreview() {
    LaporanKeuanganScreen ()
    
}