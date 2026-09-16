package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetTransaksiByIdUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.state.DetailTransaksiState
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTransaksiViewModel @Inject constructor(
    private val getTransaksiByIdUseCase: GetTransaksiByIdUseCase,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase,
    private val produkDao: ProdukDao
) : ViewModel() {

    private val _state = MutableStateFlow(DetailTransaksiState())
    val state: StateFlow<DetailTransaksiState> = _state.asStateFlow()

    fun loadTransaksi(transactionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            if (transactionId.isNotBlank()) {
                getTransaksiByIdUseCase(transactionId).collect { tx ->
                    if (tx != null) {
                        val enrichedTx = enrichTransactionWithImages(tx)
                        _state.update { it.copy(transaksi = enrichedTx, isLoading = false) }
                    } else {
                        // Fallback to latest transaction if specific ID is not found in DB
                        getAllTransaksiUseCase().collect { list ->
                            val latest = list.firstOrNull()
                            val enrichedTx = latest?.let { enrichTransactionWithImages(it) }
                            _state.update {
                                it.copy(
                                    transaksi = enrichedTx,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } else {
                // Fallback to latest transaction if no transactionId argument was passed
                getAllTransaksiUseCase().collect { list ->
                    val latest = list.firstOrNull()
                    val enrichedTx = latest?.let { enrichTransactionWithImages(it) }
                    _state.update {
                        it.copy(
                            transaksi = enrichedTx,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun enrichTransactionWithImages(tx: PenjualanTransaksi): PenjualanTransaksi {
        val updatedItems = tx.items.map { item ->
            if (item.produk.imageUrl.isBlank() && item.produk.id.isNotBlank()) {
                val dbProduk = produkDao.getProdukById(item.produk.id)
                if (dbProduk != null && dbProduk.imageUrl.isNotBlank()) {
                    item.copy(produk = item.produk.copy(imageUrl = dbProduk.imageUrl))
                } else {
                    item
                }
            } else {
                item
            }
        }
        return tx.copy(items = updatedItems)
    }
}
