package com.ptpws.ikikasir.feature.promo.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PromoValidityTest {

    @Test
    fun expiredPromoIsNotAvailable() {
        val promo = Promo(startDate = "28 Sep 2025", endDate = "29 Sep 2025")

        assertFalse(promo.isAvailableOn(LocalDate.of(2025, 9, 30)))
        assertTrue(promo.isExpiredOn(LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun promoIsAvailableOnBothBoundaryDates() {
        val promo = Promo(startDate = "28 Sep 2025", endDate = "29 Sep 2025")

        assertFalse(promo.isAvailableOn(LocalDate.of(2025, 9, 27)))
        assertTrue(promo.isAvailableOn(LocalDate.of(2025, 9, 28)))
        assertTrue(promo.isAvailableOn(LocalDate.of(2025, 9, 29)))
        assertFalse(promo.isAvailableOn(LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun invalidDateDoesNotMakePromoAvailable() {
        val promo = Promo(endDate = "not-a-date")

        assertFalse(promo.isAvailableOn(LocalDate.of(2025, 9, 29)))
    }

    @Test
    fun blankDatesRemainUnrestrictedAndInactivePromosStayUnavailable() {
        val date = LocalDate.of(2025, 9, 29)

        assertTrue(Promo().isAvailableOn(date))
        assertFalse(Promo(isActive = false).isAvailableOn(date))
    }

    @Test
    fun supportsIsoDates() {
        val promo = Promo(startDate = "2025-09-28", endDate = "2025-09-29")

        assertTrue(promo.isAvailableOn(LocalDate.of(2025, 9, 29)))
    }
}
