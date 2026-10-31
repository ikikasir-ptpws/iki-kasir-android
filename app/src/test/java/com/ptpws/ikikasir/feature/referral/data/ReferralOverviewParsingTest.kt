package com.ptpws.ikikasir.feature.referral.data

import com.ptpws.ikikasir.feature.referral.data.remote.ReferralOverviewResponseDto
import com.ptpws.ikikasir.feature.referral.data.remote.ReferralInfoDto
import com.ptpws.ikikasir.feature.referral.data.remote.UserReferralDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Uji parsing ringkasan referral yang dikirim backend.
 *
 * Backend mengirim data dalam struktur datar. Aplikasi Android versi lama
 * membaca object `referralInfo` + list `userReferrals`, jadi keduanya
 * harus tetap bisa dibaca.
 */
class ReferralOverviewParsingTest {

    @Test
    fun flatResponseIsReadCorrectly() {
        val dto = ReferralOverviewResponseDto(
            customerId = "CUS-001",
            referralCode = "IKIKASIR-ABCDE",
            uplineName = "MUhib",
            uplineCode = "CUS-000",
            uplineBoundDate = "10 September 2026",
            isBound = true,
            availableBalance = 30000.0,
            totalEarnings = 45000.0,
            totalWithdrawn = 15000.0,
            rewardPerActiveUser = 15000.0,
            referredUsers = listOf(
                UserReferralDto(
                    id = "LEAD-1",
                    storeName = "Toko Seatri",
                    ownerName = "Seatri",
                    isActive = false,
                    status = "Menunggu Verifikasi"
                )
            )
        )

        val info = dto.resolveReferralInfo()

        assertEquals("IKIKASIR-ABCDE", info.referralCode)
        assertEquals("MUhib", info.uplineName)
        assertTrue(info.isBound == true)
        assertEquals(30000.0, info.availableBalance ?: 0.0, 0.0)
        assertEquals(1, dto.resolveUserReferrals().size)
        assertEquals("Toko Seatri", dto.resolveUserReferrals().first().storeName)
    }

    @Test
    fun nestedResponseIsStillSupported() {
        val dto = ReferralOverviewResponseDto(
            referralInfo = ReferralInfoDto(
                referralCode = "IKIKASIR-0WTCK",
                uplineName = "MUhib",
                isBound = true,
                availableBalance = 125000.0
            ),
            userReferrals = listOf(
                UserReferralDto(id = "LEAD-2", storeName = "Toko Lampar", ownerName = "Lampar")
            )
        )

        val info = dto.resolveReferralInfo()

        assertEquals("IKIKASIR-0WTCK", info.referralCode)
        assertEquals("MUhib", info.uplineName)
        assertEquals(125000.0, info.availableBalance ?: 0.0, 0.0)
        assertEquals(1, dto.resolveUserReferrals().size)
        assertEquals("Lampar", dto.resolveUserReferrals().first().ownerName)
    }

    @Test
    fun emptyResponseDoesNotCrash() {
        val dto = ReferralOverviewResponseDto()

        val info = dto.resolveReferralInfo()

        assertNull(info.referralCode)
        assertEquals(0, dto.resolveUserReferrals().size)
    }
}
