package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.DeleteProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.SyncProdukUseCase
import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProdukViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val deleteProdukUseCase: DeleteProdukUseCase,
    private val syncProdukUseCase: SyncProdukUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(ProdukListState())
    val state: StateFlow<ProdukListState> = _state.asStateFlow()

    init {
        observeNetwork()
        loadProdukList()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _state.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    private fun loadProdukList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getProdukUseCase()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Gagal memuat produk"
                        )
                    }
                }
                .collect { list ->
                    _state.update { current ->
                        val filtered = filterList(list, current.searchQuery, current.selectedCategoryId)
                        current.copy(
                            produkList = list,
                            filteredList = filtered,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredList = filterList(current.produkList, query, current.selectedCategoryId)
            )
        }
    }

    fun onCategoryFilterChange(categoryId: String?) {
        _state.update { current ->
            current.copy(
                selectedCategoryId = categoryId,
                filteredList = filterList(current.produkList, current.searchQuery, categoryId)
            )
        }
    }

    private fun filterList(list: List<Produk>, query: String, categoryId: String?): List<Produk> {
        return list.filter { produk ->
            val matchesQuery = query.isBlank() ||
                    produk.name.contains(query, ignoreCase = true) ||
                    produk.barcode.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || produk.categoryId == categoryId
            matchesQuery && matchesCategory
        }
    }

    fun requestDeleteProduk(produk: Produk) {
        _state.update { it.copy(produkToDelete = produk) }
    }

    fun dismissDeleteDialog() {
        _state.update { it.copy(produkToDelete = null) }
    }

    fun deleteProduk(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, produkToDelete = null) }
            deleteProdukUseCase(id).collect { result ->
                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Produk berhasil dihapus"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus produk"
                        )
                    }
                }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            syncProdukUseCase().collect { result ->
                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isSyncing = false,
                            userMessage = "Sinkronisasi berhasil"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Sinkronisasi gagal"
                        )
                    }
                }
            }
        }
    }

    fun clearUserMessage() {
        _state.update { it.copy(userMessage = null, errorMessage = null) }
    }
}