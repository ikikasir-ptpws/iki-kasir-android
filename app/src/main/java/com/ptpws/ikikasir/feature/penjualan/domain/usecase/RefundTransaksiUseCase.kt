package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.antrean.data.local.dao.AntreanDao
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.usecase.SaveStokAdjustmentUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.domain.usecase.UpdateProdukUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class RefundTransaksiUseCase @Inject constructor(
    private val penjualanRepository: PenjualanRepository,
    private val produkDao: ProdukDao,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val saveStokAdjustmentUseCase: SaveStokAdjustmentUseCase,
    private val antreanDao: AntreanDao,
    private val antreanRepository: AntreanRepository,
    private val logActivityUseCase: LogActivityUseCase,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(
        transaksi: PenjualanTransaksi,
        reason: String = ""
    ): Flow<Result<PenjualanTransaksi>> = flow {
        if (transaksi.status.equals("REFUND", ignoreCase = true) ||
            transaksi.status.equals("REFUNDED", ignoreCase = true) ||
            transaksi.status.equals("BATAL", ignoreCase = true)
        ) {
            emit(Result.failure(IllegalStateException("Transaksi ini sudah di-refund sebelumnya.")))
            return@flow
        }

        val userUid = firebaseAuth.currentUser?.uid ?: ""
        val kasirNama = firebaseAuth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseAuth.currentUser?.email?.substringBefore("@")
            ?: transaksi.createdBy.ifBlank { "Kasir" }

        try {
            // 1. Kembalikan stok untuk setiap produk & catat mutasi stok IN (REFUND)
            transaksi.items.forEach { cartItem ->
                val productId = cartItem.produk.id
                if (productId.isNotBlank()) {
                    val dbProdukEntity = produkDao.getProdukById(productId)
                    val baseProduk = dbProdukEntity?.toDomain() ?: cartItem.produk
                    val stokSebelum = baseProduk.stock
                    val stokSesudah = stokSebelum + cartItem.quantity
                    val updatedProduk = baseProduk.copy(
                        stock = stokSesudah,
                        updatedAt = Timestamp.now()
                    )

                    updateProdukUseCase(updatedProduk).collect { /* update produk Room + Firestore */ }

                    val movement = StockMovement(
                        movementId = UUID.randomUUID().toString(),
                        productId = baseProduk.id,
                        productName = baseProduk.name,
                        barcode = baseProduk.barcode,
                        type = MovementType.IN,
                        quantity = cartItem.quantity,
                        stockBefore = stokSebelum,
                        stockAfter = stokSesudah,
                        source = "REFUND",
                        userId = userUid,
                        createdBy = kasirNama,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    saveStokAdjustmentUseCase(movement).collect { /* catat riwayat stok */ }
                }
            }

            // 2. Batalkan antrean jika masih ada di daftar antrean
            try {
                val antrean = antreanDao.getAntreanByTransactionId(transaksi.transactionId)
                if (antrean != null) {
                    antreanRepository.deleteAntrean(antrean.id).collect { /* batalkan antrean */ }
                }
            } catch (e: Exception) {
                // Ignore jika antrean tidak ditemukan
            }

            // 3. Update status transaksi pada data transactions: hanya field status yang berubah menjadi "REFUND"
            val refundedTransaksi = transaksi.copy(
                status = "REFUND",
                updatedAt = Timestamp.now()
            )

            penjualanRepository.simpanTransaksi(refundedTransaksi).collect { result ->
                result.fold(
                    onSuccess = {
                        try {
                            logActivityUseCase(
                                title = "Refund Transaksi",
                                description = "Transaksi #${transaksi.transactionNumber} berhasil di-refund oleh $kasirNama. Alasan: ${reason.ifBlank { "-" }}",
                                category = "TRANSAKSI",
                                action = "REFUND",
                                isWarning = true,
                                actorId = userUid,
                                actorName = kasirNama
                            )
                        } catch (e: Exception) {
                            // Non-critical log
                        }
                        emit(Result.success(refundedTransaksi))
                    },
                    onFailure = { error ->
                        emit(Result.failure(error))
                    }
                )
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
