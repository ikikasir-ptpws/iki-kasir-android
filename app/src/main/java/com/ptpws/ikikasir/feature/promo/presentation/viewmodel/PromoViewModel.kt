package com.ptpws.ikikasir.feature.promo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.usecase.DeletePromoUseCase
import com.ptpws.ikikasir.feature.promo.domain.usecase.GetPromoListUseCase
import com.ptpws.ikikasir.feature.promo.domain.usecase.TogglePromoStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromoListState(
    val promoList: List<Promo> = emptyList(),
    val filteredList: List<Promo> = emptyList(),
    val searchQuery: String = "",
    val selectedFilterTab: String = "Semua", // "Semua", "Aktif", "Akan Datang", "Kedaluwarsa"
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PromoViewModel @Inject constructor(
    private val getPromoListUseCase: GetPromoListUseCase,
    private val togglePromoStatusUseCase: TogglePromoStatusUseCase,
    private val deletePromoUseCase: DeletePromoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PromoListState())
    val state: StateFlow<PromoListState> = _state.asStateFlow()

    init {
        loadPromoList()
    }

    private fun loadPromoList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getPromoListUseCase().collect { list ->
                _state.update { currentState ->
                    val filtered = applyFilterAndSearch(list, currentState.searchQuery, currentState.selectedFilterTab)
                    currentState.copy(
                        promoList = list,
                        filteredList = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(currentState.promoList, query, currentState.selectedFilterTab)
            currentState.copy(searchQuery = query, filteredList = filtered)
        }
    }

    fun onFilterTabSelected(tab: String) {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(currentState.promoList, currentState.searchQuery, tab)
            currentState.copy(selectedFilterTab = tab, filteredList = filtered)
        }
    }

    fun toggleStatus(promoId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            togglePromoStatusUseCase(promoId, !currentStatus).collect { result ->
                result.onSuccess {
                    _state.update { it.copy(userMessage = "Status promo berhasil diubah") }
                }.onFailure { err ->
                    _state.update { it.copy(errorMessage = "Gagal mengubah status: ${err.message}") }
                }
            }
        }
    }

    fun deletePromo(promoId: String) {
        viewModelScope.launch {
            deletePromoUseCase(promoId).collect { result ->
                result.onSuccess {
                    _state.update { it.copy(userMessage = "Promo berhasil dihapus") }
                }.onFailure { err ->
                    _state.update { it.copy(errorMessage = "Gagal menghapus promo: ${err.message}") }
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(userMessage = null, errorMessage = null) }
    }

    private fun applyFilterAndSearch(
        list: List<Promo>,
        query: String,
        filterTab: String
    ): List<Promo> {
        return list.filter { promo ->
            val matchesQuery = query.isBlank() ||
                    promo.nama.contains(query, ignoreCase = true) ||
                    promo.tipePromo.contains(query, ignoreCase = true)

            val matchesTab = when (filterTab) {
                "Aktif" -> promo.isActive
                "Akan Datang" -> !promo.isActive
                "Kedaluwarsa" -> false
                else -> true
            }

            matchesQuery && matchesTab
        }
    }
}
