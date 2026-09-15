package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProsesPembayaranUseCase @Inject constructor(
    private val repository: PenjualanRepository
) {
    suspend operator fun invoke(
        kodeTransaksi: String,
        items: List<CartItem>,
        subtotal: Double,
        totalBayar: Double,
        metodePembayaran: String
    ): Flow<Result<PenjualanTransaksi>> = flow {
        if (items.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("Tidak ada produk dalam keranjang pesanan.")))
            return@flow
        }

        val isTunai = metodePembayaran.equals("Tunai", ignoreCase = true)
        if (isTunai && totalBayar < subtotal) {
            emit(Result.failure(IllegalArgumentException("Nominal uang yang diterima kurang dari total tagihan.")))
            return@flow
        }

        val kembalian = if (isTunai) (totalBayar - subtotal).coerceAtLeast(0.0) else 0.0

        val transaksi = PenjualanTransaksi(
            id = System.currentTimeMillis().toString(),
            kodeTransaksi = kodeTransaksi,
            items = items,
            subtotal = subtotal,
            totalBayar = if (isTunai) totalBayar else subtotal,
            kembalian = kembalian,
            metodePembayaran = metodePembayaran,
            timestamp = System.currentTimeMillis()
        )

        repository.simpanTransaksi(transaksi).collect { result ->
            result.fold(
                onSuccess = { emit(Result.success(transaksi)) },
                onFailure = { emit(Result.failure(it)) }
            )
        }
    }
}
