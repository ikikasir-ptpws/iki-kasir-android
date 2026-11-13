package com.example.app.ui.screen

import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk Dashboard.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface DashboardUiProvider {
    val sessionState: StateFlow<UserSessionState>
    val analyticsState: StateFlow<DashboardAnalytics>
    val isInitialLoading: StateFlow<Boolean>
    val isRefreshing: StateFlow<Boolean>
    val selectedFilter: StateFlow<PeriodeLaporanPenjualan>
    val customStartDateMillis: StateFlow<Long?>
    val customEndDateMillis: StateFlow<Long?>
    val customDateLabel: StateFlow<String>
    val isBalanceVisible: StateFlow<Boolean>
    fun isAllowed(menuKey: String): Boolean
    fun setFilter(filter: PeriodeLaporanPenjualan)
    fun setCustomDateRange(startMillis: Long, endMillis: Long)
    fun toggleBalanceVisibility()
    fun refreshDashboard()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke DashboardViewModel asli.
 */
class HiltDashboardUiProvider(private val viewModel: DashboardViewModel) : DashboardUiProvider {
    override val sessionState: StateFlow<UserSessionState> get() = viewModel.sessionState
    override val analyticsState: StateFlow<DashboardAnalytics> get() = viewModel.analyticsState
    override val isInitialLoading: StateFlow<Boolean> get() = viewModel.isInitialLoading
    override val isRefreshing: StateFlow<Boolean> get() = viewModel.isRefreshing
    override val selectedFilter: StateFlow<PeriodeLaporanPenjualan> get() = viewModel.selectedFilter
    override val customStartDateMillis: StateFlow<Long?> get() = viewModel.customStartDateMillis
    override val customEndDateMillis: StateFlow<Long?> get() = viewModel.customEndDateMillis
    override val customDateLabel: StateFlow<String> get() = viewModel.customDateLabel
    override val isBalanceVisible: StateFlow<Boolean> get() = viewModel.isBalanceVisible
    override fun isAllowed(menuKey: String): Boolean = viewModel.isAllowed(menuKey)
    override fun setFilter(filter: PeriodeLaporanPenjualan) = viewModel.setFilter(filter)
    override fun setCustomDateRange(startMillis: Long, endMillis: Long) = viewModel.setCustomDateRange(startMillis, endMillis)
    override fun toggleBalanceVisibility() = viewModel.toggleBalanceVisibility()
    override fun refreshDashboard() = viewModel.refreshDashboard()
}
