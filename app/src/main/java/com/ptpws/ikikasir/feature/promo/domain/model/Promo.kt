package com.ptpws.ikikasir.feature.promo.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

data class Promo(
    val id: String = "",
    val name: String = "",
    val promoType: String = "",
    val items: List<PromoProductItem> = emptyList(),
    val discountType: String = "Rp", // "Rp" or "%"
    val discountValue: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    // Compatibility getters for code using Indonesian field names
    val nama: String get() = name
    val tipePromo: String get() = promoType
    val diskonType: String get() = discountType
    val nilaiDiskon: Double get() = discountValue
    val tanggalMulai: String get() = startDate
    val tanggalBerakhir: String get() = endDate
}

private val promoDateFormatter = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("d MMM uuuu")
    .toFormatter(Locale.forLanguageTag("id-ID"))
    .withResolverStyle(ResolverStyle.STRICT)

private fun parsePromoDate(value: String): LocalDate? {
    if (value.isBlank()) return null

    return try {
        LocalDate.parse(value.trim(), promoDateFormatter)
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

fun Promo.isAvailableOn(date: LocalDate = LocalDate.now()): Boolean {
    if (!isActive) return false

    val start = if (startDate.isBlank()) null else parsePromoDate(startDate) ?: return false
    val end = if (endDate.isBlank()) null else parsePromoDate(endDate) ?: return false

    return (start == null || !date.isBefore(start)) &&
        (end == null || !date.isAfter(end))
}

fun Promo.isExpiredOn(date: LocalDate = LocalDate.now()): Boolean =
    endDate.isNotBlank() && parsePromoDate(endDate)?.isBefore(date) == true

fun Promo.isUpcomingOn(date: LocalDate = LocalDate.now()): Boolean =
    startDate.isNotBlank() && parsePromoDate(startDate)?.isAfter(date) == true

fun Promo.isEndingWithinDays(days: Long, date: LocalDate = LocalDate.now()): Boolean {
    val end = parsePromoDate(endDate) ?: return false
    return isAvailableOn(date) && !end.isBefore(date) && !end.isAfter(date.plusDays(days))
}
