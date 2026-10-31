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
     *   v
     * Cek session/local cache expired
     *   v
     * Apakah ada data di session cache expired ?
     *   +- Ya  -> Apakah session data masih aktif ?
     *   |         +- Tidak          -> [Popup Kadaluarsa]
     *   |         +- Ya & Segar     -> Login Aplikasi (tanpa hit server)
     *   |         +- Ya & Basi(>1j) -> Re-verify ke server
     *   |
     *   +- Tidak -> Fetch API data expired di server
     *               v
     *             Apakah data masih aktif ?
     *               +- Ya    -> Simpan di Session -> Login Aplikasi
     *               +- Tidak -> [Popup Kadaluarsa]
     *
     * Pada NetworkError:
     *   - Jika cache masih aktif (meski basi) -> fallback ke cache
     *   - Jika tidak ada cache valid           -> NetworkError
     */
    suspend fun execute(email: String, forceRefresh: Boolean = false): SubscriptionFlowResult {
        if (forceRefresh) {
            sessionManager.clearSession()
        }

        // 1. Cek session/local cache expired
        val hasCache = sessionManager.hasExpiredSession(email)

        // 2. Apakah ada data di session cache expired ?
        if (hasCache) {
            val isStillActive = sessionManager.isSessionDataActive(email)
            val cachedData = sessionManager.getCachedSession()

            // Jika cache sudah TIDAK aktif (expired berdasarkan waktu lokal) -> langsung blokir
            if (!isStillActive || cachedData == null) {
                return SubscriptionFlowResult.ExpiredFromCache(
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

            // Cache aktif secara lokal. Jika MASIH SEGAR (<1 jam) -> langsung izinkan tanpa hit server
            if (!sessionManager.isCacheStale()) {
                return SubscriptionFlowResult.ProceedFromCache(cachedData)
            }

            // Cache sudah BASI (>1 jam) -> wajib re-verify ke server
            android.util.Log.d("CheckSubscription", "Cache basi (>1 jam), re-verify ke server: $email")
        }

        // 3. Fetch API data expired di server
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
            // Server tidak bisa diakses: fallback ke cache lokal jika masih aktif
            val cachedData = sessionManager.getCachedSession()
            if (hasCache && cachedData != null && sessionManager.isSessionDataActive(email)) {
                android.util.Log.w("CheckSubscription", "Server unreachable, pakai cache sebagai fallback: $email")
                SubscriptionFlowResult.ProceedFromCache(cachedData)
            } else {
                SubscriptionFlowResult.NetworkError(err)
            }
        }
    }
}
