package com.ptpws.ikikasir.feature.penjualan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.penjualan.data.local.dao.TransactionDao
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.penjualan.data.remote.datasource.PenjualanRemoteDataSource
import com.ptpws.ikikasir.feature.penjualan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PenjualanRepo"

@Singleton
class PenjualanRepositoryImpl @Inject constructor(
    private val localDao: TransactionDao,
    private val remoteDataSource: PenjualanRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : PenjualanRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync pending transactions when connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network online, auto-syncing pending transactions...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during transaction auto-sync: ", e)
                    }
                }
            }
        }
    }

    override fun getCartItems(): Flow<List<CartItem>> {
        return _cartItems.asStateFlow()
    }

    override suspend fun addToCart(produk: Produk, quantity: Int) {
        _cartItems.update { current ->
            val existing = current.find { it.produk.id == produk.id }
            if (existing != null) {
                current.map {
                    if (it.produk.id == produk.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                }
            } else {
                current + CartItem(produk = produk, quantity = quantity)
            }
        }
    }

    override suspend fun removeFromCart(produkId: String) {
        _cartItems.update { current ->
            current.filterNot { it.produk.id == produkId }
        }
    }

    override suspend fun updateQuantity(produkId: String, quantity: Int) {
        _cartItems.update { current ->
            if (quantity <= 0) {
                current.filterNot { it.produk.id == produkId }
            } else {
                current.map {
                    if (it.produk.id == produkId) {
                        it.copy(quantity = quantity)
                    } else it
                }
            }
        }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getAllTransaksi(): Flow<List<PenjualanTransaksi>> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllTransactions()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote transactions: ", e)
                }
            }
        }
        return localDao.getAllTransactionsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun simpanTransaksi(transaksi: PenjualanTransaksi): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "simpanTransaksi: id='${transaksi.transactionId}', number='${transaksi.transactionNumber}', isOnline=$isOnline")

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Saving transaction to Firestore ('transactions')...")
                remoteDataSource.saveTransaction(transaksi.toDto())
                Log.d(TAG, "Online: Firestore save success, saving to Room (isSynced=true)")
                localDao.insertOrUpdate(transaksi.toEntity(isSynced = true))
                clearCart()
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save transaction to Firestore, saving locally (isSynced=false): ", e)
                localDao.insertOrUpdate(transaksi.toEntity(isSynced = false))
                clearCart()
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Saving transaction to Room (isSynced=false)")
            localDao.insertOrUpdate(transaksi.toEntity(isSynced = false))
            clearCart()
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingTransaksi(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Cannot sync: Device is offline")
                emit(Result.failure(Exception("Tidak ada koneksi internet")))
                return@flow
            }
            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        val unsyncedItems = localDao.getUnsyncedTransactions()
        Log.d(TAG, "Found ${unsyncedItems.size} unsynced transactions")

        for (item in unsyncedItems) {
            try {
                remoteDataSource.saveTransaction(item.toDomain().toDto())
                localDao.markAsSynced(item.transactionId)
                Log.d(TAG, "Synced transactionId: ${item.transactionId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync transactionId ${item.transactionId}: ", e)
            }
        }

        try {
            val remoteList = remoteDataSource.getAllTransactions()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote transactions: ", e)
        }
    }
}
