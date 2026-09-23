package com.ptpws.ikikasir.commond

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Premium Modern Material 3 Date Range Picker Dialog for selecting Start Date and End Date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerDialog(
    initialStartDateMillis: Long? = null,
    initialEndDateMillis: Long? = null,
    onDismissRequest: () -> Unit,
    onDateRangeSelected: (startDateMillis: Long, endDateMillis: Long) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDateMillis,
        initialSelectedEndDateMillis = initialEndDateMillis
    )

    val selectedStart = dateRangePickerState.selectedStartDateMillis
    val selectedEnd = dateRangePickerState.selectedEndDateMillis

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 1. Header Row (Icon + Title + Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEEF2FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Filter Rentang Tanggal",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Pilih tanggal mulai & tanggal akhir",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Start Date & End Date Display Cards
                val sdfDisplay = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val startText = if (selectedStart != null) sdfDisplay.format(Date(selectedStart)) else "Pilih Tanggal"
                val endText = if (selectedEnd != null) sdfDisplay.format(Date(selectedEnd)) else "Pilih Tanggal"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mulai Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedStart != null) Color(0xFFEEF2FF) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedStart != null) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = "DARI",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = startText,
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedStart != null) Color(0xFF4F46E5) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )

                    // Selesai Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedEnd != null) Color(0xFFEEF2FF) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedEnd != null) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = "SAMPAI",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = endText,
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedEnd != null) Color(0xFF4F46E5) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Quick Presets Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        QuickPresetChip(label = "Hari Ini") {
                            val todayUtc = getTodayUtcMillis()
                            dateRangePickerState.setSelection(todayUtc, todayUtc)
                        }
                    }
                    item {
                        QuickPresetChip(label = "7 Hari Terakhir") {
                            val todayUtc = getTodayUtcMillis()
                            val startUtc = getDaysAgoUtcMillis(7)
                            dateRangePickerState.setSelection(startUtc, todayUtc)
                        }
                    }
                    item {
                        QuickPresetChip(label = "30 Hari Terakhir") {
                            val todayUtc = getTodayUtcMillis()
                            val startUtc = getDaysAgoUtcMillis(30)
                            dateRangePickerState.setSelection(startUtc, todayUtc)
                        }
                    }
                    item {
                        QuickPresetChip(label = "Bulan Ini") {
                            val startOfMonthUtc = getStartOfMonthUtcMillis()
                            val todayUtc = getTodayUtcMillis()
                            dateRangePickerState.setSelection(startOfMonthUtc, todayUtc)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // 4. DateRangePicker Calendar Grid with vibrant indigo colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White,
                            selectedDayContainerColor = Color(0xFF4F46E5),
                            selectedDayContentColor = Color.White,
                            dayInSelectionRangeContainerColor = Color(0xFFEEF2FF),
                            dayInSelectionRangeContentColor = Color(0xFF3730A3),
                            todayDateBorderColor = Color(0xFF4F46E5),
                            todayContentColor = Color(0xFF4F46E5),
                            weekdayContentColor = Color(0xFF64748B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // 5. Action Buttons (Batal & Terapkan Filter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            fontSize = 14.sp
                        )
                    }

                    val isEnabled = selectedStart != null && selectedEnd != null
                    Button(
                        onClick = {
                            if (selectedStart != null && selectedEnd != null) {
                                onDateRangeSelected(selectedStart, selectedEnd)
                            }
                        },
                        enabled = isEnabled,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            disabledContainerColor = Color(0xFFC7D2FE)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "Terapkan Filter",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.height(30.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                fontFamily = interfamily
            )
        }
    }
}

private fun getTodayUtcMillis(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun getDaysAgoUtcMillis(daysAgo: Int): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.DAY_OF_YEAR, -daysAgo)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun getStartOfMonthUtcMillis(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

/**
 * Formats start and end UTC millis from DateRangePicker into human readable text label.
 */
fun formatDateRangeLabel(startUtcMillis: Long, endUtcMillis: Long): String {
    val calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startUtcMillis }
    val calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endUtcMillis }

    val sdfFull = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val sdfShort = SimpleDateFormat("dd MMM", Locale("id", "ID")).apply { timeZone = TimeZone.getTimeZone("UTC") }

    return if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
        calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR)
    ) {
        sdfFull.format(Date(startUtcMillis))
    } else if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR)) {
        "${sdfShort.format(Date(startUtcMillis))} - ${sdfFull.format(Date(endUtcMillis))}"
    } else {
        "${sdfFull.format(Date(startUtcMillis))} - ${sdfFull.format(Date(endUtcMillis))}"
    }
}

/**
 * Returns epoch timestamp in seconds for the start of day (00:00:00.000) in local timezone
 * derived from the UTC millis selected in DateRangePicker.
 */
fun getStartOfDayLocalSeconds(utcMillis: Long): Long {
    val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, calendarUtc.get(Calendar.YEAR))
        set(Calendar.MONTH, calendarUtc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, calendarUtc.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return localCal.timeInMillis / 1000
}

/**
 * Returns epoch timestamp in seconds for the end of day (23:59:59.999) in local timezone
 * derived from the UTC millis selected in DateRangePicker.
 */
fun getEndOfDayLocalSeconds(utcMillis: Long): Long {
    val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, calendarUtc.get(Calendar.YEAR))
        set(Calendar.MONTH, calendarUtc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, calendarUtc.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return localCal.timeInMillis / 1000
}
