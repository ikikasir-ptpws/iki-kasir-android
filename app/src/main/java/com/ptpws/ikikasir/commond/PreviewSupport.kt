package com.ptpws.ikikasir.commond

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.app.ui.screen.DashboardSessionProvider
import com.example.app.ui.screen.UserSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Helper untuk Compose Preview.
 *
 * CATATAN PENTING: `@Preview` di Android Studio berjalan di layoutlib, yang **tidak**
 * meng-instantiasi Application aplikasi. Karena itu:
 * - `hiltViewModel()` gagal (activity preview bukan @AndroidEntryPoint)
 * - `EntryPointAccessors.fromApplication()` gagal ("Could not find an Application in the given context")
 * - Firebase / Room tidak bisa dipakai di preview
 *
 * Solusinya: screen menerima interface state yang sempit (mis. [DashboardSessionProvider]),
 * sehingga preview cukup memakai implementasi stub di bawah — layout, navigasi, dan
 * struktur UI yang di-preview sama persis dengan versi production.
 */
class PreviewSessionProvider(
    override val sessionState: StateFlow<UserSessionState> = MutableStateFlow(
        UserSessionState(roleName = "Admin", isLoading = false)
    ),
    private val allowedMenus: (String) -> Boolean = { true }
) : DashboardSessionProvider {
    override fun isAllowed(menuKey: String): Boolean = allowedMenus(menuKey)
}

/**
 * Provider sesi untuk preview. Default: semua menu diizinkan, sesi selesai dimuat.
 */
@Composable
fun rememberPreviewSessionProvider(
    allowedMenus: (String) -> Boolean = { true }
): DashboardSessionProvider = remember {
    PreviewSessionProvider(allowedMenus = allowedMenus)
}
