package com.ptpws.ikikasir.core.database

import androidx.room.TypeConverter
import com.google.firebase.Timestamp

/**
 * Room TypeConverter untuk menyimpan Firebase Timestamp sebagai Long (millis)
 * di SQLite, dan mengkonversinya kembali ke Timestamp saat dibaca.
 */
class TimestampConverter {

    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.let { it.seconds * 1000L + it.nanoseconds / 1_000_000L }
    }

    @TypeConverter
    fun toTimestamp(millis: Long?): Timestamp? {
        return millis?.let {
            val seconds = it / 1000L
            val nanos = ((it % 1000L) * 1_000_000L).toInt()
            Timestamp(seconds, nanos)
        }
    }
}