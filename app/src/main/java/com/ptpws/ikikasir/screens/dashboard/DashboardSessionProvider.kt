package com.example.app.ui.screen

import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state sesi & hak akses menu untuk layer UI Dashboard.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface DashboardSessionProvider {
    val sessionState: StateFlow<UserSessionState>
    fun isAllowed(menuKey: String): Boolean
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke DashboardViewModel asli.
 */
class HiltDashboardSessionProvider(private val viewModel: DashboardViewModel) : DashboardSessionProvider {
    override val sessionState: StateFlow<UserSessionState> get() = viewModel.sessionState
    override fun isAllowed(menuKey: String): Boolean = viewModel.isAllowed(menuKey)
}
