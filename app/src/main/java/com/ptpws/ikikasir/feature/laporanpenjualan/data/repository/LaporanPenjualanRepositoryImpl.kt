package com.ptpws.ikikasir.feature.laporanpenjualan.data.repository

import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.ProdukTerjualItem
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.repository.LaporanPenjualanRepository
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaporanPenjualanRepositoryImpl @Inject constructor(
    private val penjualanRepository: PenjualanRepository,
    private val kategoriRepository: KategoriRepository
) : LaporanPenjualanRepository {

    override fun getLaporanPenjualan(
        periode: PeriodeLaporanPenjualan,
        customStartMillis: Long?,
        customEndMillis: Long?
    ): Flow<LaporanPenjualanSummary> {
        return combine(
            penjualanRepository.getAllTransaksi(),
            kategoriRepository.getKategoriList()
        ) { allTransactions, kategoriList ->
            val kategoriMap = kategoriList.associate { it.id to it.name }
            aggregateSoldProducts(
                transactions = allTransactions,
                kategoriMap = kategoriMap,
                periode = periode,
                customStartMillis = customStartMillis,
                customEndMillis = customEndMillis
            )
        }
    }

    private fun aggregateSoldProducts(
        transactions: List<PenjualanTransaksi>,
        kategoriMap: Map<String, String>,
        periode: PeriodeLaporanPenjualan,
        customStartMillis: Long?,
        customEndMillis: Long?
    ): LaporanPenjualanSummary {
        val (startMillis, endMillis, label) = computeTimeRange(periode, customStartMillis, customEndMillis)

        val filteredTransactions = transactions.filter { tx ->
            val isNotRefund = !tx.status.equals("REFUND", ignoreCase = true) &&
                    !tx.status.equals("REFUNDED", ignoreCase = true) &&
                    !tx.status.equals("BATAL", ignoreCase = true)

            val txTimeMillis = try {
                tx.createdAt?.toDate()?.time ?: (tx.createdAt?.seconds?.times(1000L) ?: System.currentTimeMillis())
            } catch (e: Exception) {
                try {
                    tx.createdAt?.seconds?.times(1000L) ?: System.currentTimeMillis()
                } catch (e2: Exception) {
                    System.currentTimeMillis()
                }
            }

            val inDateRange = if (startMillis == null || endMillis == null) {
                true
            } else {
                txTimeMillis in startMillis..endMillis
            }

            isNotRefund && inDateRange
        }

        val productMap = mutableMapOf<String, ProductAccumulator>()

        filteredTransactions.forEach { tx ->
            if (tx.items.isNotEmpty()) {
                tx.items.forEach { item ->
                    val key = if (item.produk.id.isNotBlank()) item.produk.id else item.name.trim().lowercase()
                    val catName = kategoriMap[item.produk.categoryId] ?: "Umum"
                    val acc = productMap.getOrPut(key) {
                        ProductAccumulator(
                            productId = item.produk.id,
                            name = item.name.ifBlank { item.produk.name },
                            kategori = catName,
                            imageUrl = item.produk.imageUrl,
                            hargaSatuan = item.price
                        )
                    }
                    acc.unitTerjual += item.quantity
                    acc.totalOmzet += item.totalPrice
                    if (acc.hargaSatuan <= 0 && item.price > 0) acc.hargaSatuan = item.price
                    if (acc.imageUrl.isBlank() && item.produk.imageUrl.isNotBlank()) acc.imageUrl = item.produk.imageUrl
                    if (acc.kategori.isBlank() || acc.kategori == "Umum") {
                        if (catName.isNotBlank() && catName != "Umum") acc.kategori = catName
                    }
                }
            }
        }

        val totalUnit = productMap.values.sumOf { it.unitTerjual }
        val totalOmzet = productMap.values.sumOf { it.totalOmzet }
        val totalProdukUnik = productMap.size

        // Sort by unitTerjual descending, secondary totalOmzet descending
        val sortedList = productMap.values
            .sortedWith(compareByDescending<ProductAccumulator> { it.unitTerjual }.thenByDescending { it.totalOmzet })

        val items = sortedList.mapIndexed { index, acc ->
            val contribution = if (totalOmzet > 0) (acc.totalOmzet / totalOmzet) * 100.0 else 0.0
            ProdukTerjualItem(
                productId = acc.productId,
                namaProduk = acc.name,
                kategori = acc.kategori.ifBlank { "Umum" },
                imageUrl = acc.imageUrl,
                hargaSatuan = if (acc.hargaSatuan > 0) acc.hargaSatuan else if (acc.unitTerjual > 0) acc.totalOmzet / acc.unitTerjual else 0.0,
                unitTerjual = acc.unitTerjual,
                totalOmzet = acc.totalOmzet,
                kontribusiPersen = contribution,
                rank = index + 1
            )
        }

        val bestSeller = items.firstOrNull()

        return LaporanPenjualanSummary(
            totalUnitTerjual = totalUnit,
            totalOmzet = totalOmzet,
            totalProdukUnik = totalProdukUnik,
            produkTerlarisNama = bestSeller?.namaProduk ?: "-",
            produkTerlarisQty = bestSeller?.unitTerjual ?: 0,
            filterLabel = label,
            items = items
        )
    }

    private fun computeTimeRange(
        periode: PeriodeLaporanPenjualan,
        customStartMillis: Long?,
        customEndMillis: Long?
    ): Triple<Long?, Long?, String> {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val now = System.currentTimeMillis()

        return when (periode) {
            PeriodeLaporanPenjualan.SEMUA -> Triple(null, null, "Semua Waktu")

            PeriodeLaporanPenjualan.HARI_INI -> {
                val start = getStartOfDay(now)
                val end = getEndOfDay(now)
                Triple(start, end, "Hari Ini (${dateFormat.format(Date(now))})")
            }

            PeriodeLaporanPenjualan.TUJUH_HARI -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                }
                val start = getStartOfDay(cal.timeInMillis)
                val end = getEndOfDay(now)
                Triple(start, end, "7 Hari Terakhir")
            }

            PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -29)
                }
                val start = getStartOfDay(cal.timeInMillis)
                val end = getEndOfDay(now)
                Triple(start, end, "30 Hari Terakhir")
            }

            PeriodeLaporanPenjualan.BULAN_INI -> {
                val calStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                Triple(calStart.timeInMillis, calEnd.timeInMillis, "Bulan Ini (${monthFormat.format(Date(now))})")
            }

            PeriodeLaporanPenjualan.KUSTOM -> {
                if (customStartMillis != null && customEndMillis != null) {
                    val start = getStartOfDay(customStartMillis)
                    val end = getEndOfDay(customEndMillis)
                    val sStr = dateFormat.format(Date(customStartMillis))
                    val eStr = dateFormat.format(Date(customEndMillis))
                    val label = if (sStr == eStr) sStr else "$sStr - $eStr"
                    Triple(start, end, label)
                } else {
                    Triple(null, null, "Kustom")
                }
            }
        }
    }

    private fun getStartOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private class ProductAccumulator(
        val productId: String,
        val name: String,
        var kategori: String,
        var imageUrl: String,
        var hargaSatuan: Double,
        var unitTerjual: Int = 0,
        var totalOmzet: Double = 0.0
    )
}
