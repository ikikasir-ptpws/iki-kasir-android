package com.ptpws.ikikasir.feature.kategori.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.usecase.DeleteKategoriUseCase
import com.ptpws.ikikasir.feature.kategori.domain.usecase.GetKategoriUseCase
import com.ptpws.ikikasir.feature.kategori.domain.usecase.SyncKategoriUseCase
import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KategoriViewModel @Inject constructor(
    private val getKategoriUseCase: GetKategoriUseCase,
    private val deleteKategoriUseCase: DeleteKategoriUseCase,
    private val syncKategoriUseCase: SyncKategoriUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(KategoriListState())
    val state: StateFlow<KategoriListState> = _state.asStateFlow()

    init {
        observeNetwork()
        loadKategoriList()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _state.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    private fun loadKategoriList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getKategoriUseCase()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Gagal memuat kategori"
                        )
                    }
                }
                .collect { list ->
                    _state.update { current ->
                        val filtered = filterList(list, current.searchQuery)
                        current.copy(
                            kategoriList = list,
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
                filteredList = filterList(current.kategoriList, query)
            )
        }
    }

    private fun filterList(list: List<Kategori>, query: String): List<Kategori> {
        if (query.isBlank()) return list
        return list.filter {
            it.nama.contains(query, ignoreCase = true) ||
                    it.deskripsi.contains(query, ignoreCase = true)
        }
    }

    fun requestDeleteKategori(kategori: Kategori) {
        _state.update { it.copy(kategoriToDelete = kategori) }
    }

    fun dismissDeleteDialog() {
        _state.update { it.copy(kategoriToDelete = null) }
    }

    fun deleteKategori(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, kategoriToDelete = null) }
            deleteKategoriUseCase(id).collect { result ->
                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Kategori berhasil dihapus"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus kategori"
                        )
                    }
                }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            syncKategoriUseCase().collect { result ->
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
