package com.ptpws.ikikasir.core.database

import androidx.room.TypeConverter
import org.json.JSONObject

class MapConverter {

    @TypeConverter
    fun fromMap(map: Map<String, Boolean>?): String {
        if (map.isNullOrEmpty()) return "{}"
        return JSONObject(map as Map<*, *>).toString()
    }

    @TypeConverter
    fun toMap(jsonString: String?): Map<String, Boolean> {
        if (jsonString.isNullOrBlank()) return emptyMap()
        return try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, Boolean>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getBoolean(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
