package com.ptpws.ikikasir.feature.promo.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk PromoUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface PromoUiProvider {
    val state: StateFlow<PromoListState>
    fun refreshPromoList()
    fun onSearchQueryChange(query: String)
    fun onFilterTabSelected(tab: String)
    fun toggleStatus(promoId: String, currentStatus: Boolean)
    fun deletePromo(promoId: String)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke PromoViewModel asli.
 */
class HiltPromoUiProvider(private val viewModel: PromoViewModel) : PromoUiProvider {
    override val state: StateFlow<PromoListState> get() = viewModel.state
    override fun refreshPromoList() = viewModel.refreshPromoList()
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun onFilterTabSelected(tab: String) = viewModel.onFilterTabSelected(tab)
    override fun toggleStatus(promoId: String, currentStatus: Boolean) = viewModel.toggleStatus(promoId, currentStatus)
    override fun deletePromo(promoId: String) = viewModel.deletePromo(promoId)
}
