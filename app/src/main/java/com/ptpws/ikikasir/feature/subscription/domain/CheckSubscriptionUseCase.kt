package com.ptpws.ikikasir.feature.subscription.domain

import com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiService
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionCheckResult

sealed class SubscriptionFlowResult {
    // 1. Session cache ada & masih aktif -> Lanjut Login Aplikasi
    data class ProceedFromCache(val cached: SubscriptionCheckResult) : SubscriptionFlowResult()

    // 2. Belum ada di cache -> Fetch API data expired sukses & aktif -> Simpan di Session -> Lanjut Login Aplikasi
    data class ProceedFromServer(val result: SubscriptionCheckResult) : SubscriptionFlowResult()

    // 3. Session cache ada tapi TIDAK aktif (expired) -> Tampilkan Popup Kadaluarsa
    data class ExpiredFromCache(val cached: SubscriptionCheckResult) : SubscriptionFlowResult()

    // 4. Fetch API server mengembalikan data TIDAK aktif (expired / suspended / not found) -> Tampilkan Popup Kadaluarsa
    data class ExpiredFromServer(val result: SubscriptionCheckResult) : SubscriptionFlowResult()

    // 5. Gangguan koneksi ke server saat belum memiliki cache
    data class NetworkError(val errorMessage: String) : SubscriptionFlowResult()
}

class CheckSubscriptionUseCase(
    private val sessionManager: ExpiredSessionManager,
    private val apiService: SubscriptionApiService = SubscriptionApiService()
) {
    /**
     * Menjalankan alur lengkap sesuai diagram:
     *
     * Android
     *   ↓
     * Cek session/local cache expired
     *   ↓
     * Apakah ada data di session cache expired ?
     *   ├─ Ya  ─→ Apakah session data masih aktif ?
     *   │           ├─ Ya    ─→ Login Aplikasi
     *   │           └─ Tidak ─→ [Popup Kadaluarsa] (Tidak bisa dibuka)
     *   │
     *   └─ Tidak ─→ Fetch API data expired diserver
     *                 ↓
     *               Apakah data masih aktif ?
     *                 ├─ Ya    ─→ Simpan di Session ─→ Login Aplikasi
     *                 └─ Tidak ─→ [Popup Kadaluarsa] (Tidak bisa dibuka)
     */
    suspend fun execute(email: String, forceRefresh: Boolean = false): SubscriptionFlowResult {
        if (forceRefresh) {
            sessionManager.clearSession()
        }

        // 1. Cek session/local cache expired
        val hasCache = sessionManager.hasExpiredSession(email)

        // 2. Apakah ada data di session cache expired ?
        if (hasCache) {
            // Ya -> Cek Apakah session data masih aktif ?
            val isStillActive = sessionManager.isSessionDataActive(email)
            val cachedData = sessionManager.getCachedSession()

            return if (isStillActive && cachedData != null) {
                // Ya -> Login Aplikasi
                SubscriptionFlowResult.ProceedFromCache(cachedData)
            } else {
                // Tidak -> Tampilkan Popup Kadaluarsa
                SubscriptionFlowResult.ExpiredFromCache(
                    cachedData ?: SubscriptionCheckResult(
                        isActive = false,
                        canAccess = false,
                        status = "EXPIRED",
                        isExpired = true,
                        email = email,
                        message = "Masa aktif langganan Anda telah berakhir. Silakan hubungi admin IKIKASIR untuk perpanjangan."
                    )
                )
            }
        }

        // 3. Tidak -> Fetch API data expired diserver
        val apiResult = apiService.checkSubscription(email)

        return if (apiResult.isSuccess) {
            val data = apiResult.getOrThrow()

            // Simpan status terbaru ke session cache lokal
            sessionManager.saveSession(data)

            // Apakah data masih aktif ?
            if (data.isActive) {
                // Ya -> Login Aplikasi
                SubscriptionFlowResult.ProceedFromServer(data)
            } else {
                // Tidak -> Tampilkan Popup Kadaluarsa
                SubscriptionFlowResult.ExpiredFromServer(data)
            }
        } else {
            val err = apiResult.exceptionOrNull()?.message ?: "Gagal terhubung ke server backend IKIKASIR."
            SubscriptionFlowResult.NetworkError(err)
        }
    }
}
