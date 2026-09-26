package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.usecase.SaveStokAdjustmentUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import com.ptpws.ikikasir.feature.produk.domain.usecase.UpdateProdukUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class ProsesPembayaranUseCase @Inject constructor(
    private val repository: PenjualanRepository,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val saveStokAdjustmentUseCase: SaveStokAdjustmentUseCase,
    private val antreanRepository: AntreanRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(
        kodeTransaksi: String,
        items: List<CartItem>,
        subtotal: Double,
        ppnAmount: Double = 0.0,
        totalBayar: Double,
        metodePembayaran: String,
        discount: Double = 0.0,
        notes: String = "",
        customerName: String = "",
        tableNumber: String = ""
    ): Flow<Result<PenjualanTransaksi>> = flow {
        if (items.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("Tidak ada produk dalam keranjang pesanan.")))
            return@flow
        }

        val totalTagihan = (subtotal - discount).coerceAtLeast(0.0)
        val isTunai = metodePembayaran.equals("Tunai", ignoreCase = true)
        if (isTunai && totalBayar < totalTagihan) {
            emit(Result.failure(IllegalArgumentException("Nominal uang yang diterima kurang dari total tagihan.")))
            return@flow
        }

        val kembalian = if (isTunai) (totalBayar - totalTagihan).coerceAtLeast(0.0) else 0.0

        // Gunakan invoice number (kodeTransaksi) sebagai transactionId
        val invoiceId = if (kodeTransaksi.isNotBlank()) kodeTransaksi else "INV-2026-${(1000..9999).random()}"

        val userUid = firebaseAuth.currentUser?.uid ?: ""
        val kasirNama = firebaseAuth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseAuth.currentUser?.email?.substringBefore("@")
            ?: "Kasir"

        // Deduct stock for each purchased product & save stock movements
        items.forEach { cartItem ->
            val stokSebelum = cartItem.produk.stock
            val stokSesudah = (stokSebelum - cartItem.quantity).coerceAtLeast(0)
            val updatedProduk = cartItem.produk.copy(stock = stokSesudah)

            updateProdukUseCase(updatedProduk).collect { /* update product stock */ }

            val movement = StockMovement(
                movementId = cartItem.produk.id,
                productId = cartItem.produk.id,
                productName = cartItem.produk.name,
                barcode = cartItem.produk.barcode,
                type = MovementType.OUT,
                quantity = cartItem.quantity,
                stockBefore = stokSebelum,
                stockAfter = stokSesudah,
                source = "SALE",
                userId = userUid,
                createdBy = kasirNama,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            saveStokAdjustmentUseCase(movement).collect { /* fire and forget */ }
        }

        // Hitung real queueSequence dari database
        val realQueueSeq = try {
            antreanRepository.getNextQueueSequence()
        } catch (e: Exception) {
            1
        }

        // Subtotal SEBELUM PPN (harga item-item saja)
        val subtotalBeforeTax = items.sumOf { it.totalPrice }

        val transaksi = PenjualanTransaksi(
            transactionId = invoiceId,
            transactionNumber = invoiceId,
            items = items,
            subtotal = subtotalBeforeTax,    // selalu harga sebelum PPN
            ppnAmount = ppnAmount,           // nominal PPN
            discount = discount,
            total = totalTagihan,            // grand total (sudah termasuk PPN eksklusif)
            paymentMethod = metodePembayaran,
            paymentAmount = if (isTunai) totalBayar else totalTagihan,
            change = kembalian,
            status = "COMPLETED",
            notes = notes,
            createdBy = kasirNama,
            customerName = customerName,
            tableNumber = tableNumber,
            queueSequence = realQueueSeq,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        repository.simpanTransaksi(transaksi).collect { result ->
            result.fold(
                onSuccess = {
                    // Buat Antrean aktif (menunggu konfirmasi oleh kasir di WaitingList)
                    // QueueHistory TIDAK dibuat di sini — hanya dibuat saat kasir klik ✓/✗
                    // di WaitingListScreen melalui AntreanViewModel.selesaikan()/batalkan()
                    try {
                        val antrean = Antrean(
                            id = invoiceId,
                            transactionId = invoiceId,
                            queueSequence = realQueueSeq,
                            status = AntreanStatus.WAITING,
                            customerName = customerName,
                            tableNumber = tableNumber,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now()
                        )
                        antreanRepository.insertAntrean(antrean).collect { /* save antrean */ }
                    } catch (e: Exception) {
                        android.util.Log.e("ProsesPembayaranUseCase", "Failed auto-creating antrean: ${e.message}", e)
                    }

                    emit(Result.success(transaksi))
                },
                onFailure = { emit(Result.failure(it)) }
            )
        }
    }
}
