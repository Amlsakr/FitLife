package com.aml_sakr.fitlife.core.data.workout

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutPlanConverters {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromStringList(values: List<String>?): String = gson.toJson(values.orEmpty(), stringListType)

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        value
            ?.takeIf { it.isNotBlank() }
            ?.let(::decodeStringList)
            .orEmpty()

    private fun decodeStringList(value: String): List<String> =
        runCatching {
            gson.fromJson<List<String>>(value, stringListType)
        }.getOrElse {
            value.split(LIST_SEPARATOR)
        }

    private companion object {
        const val LIST_SEPARATOR = "\u001f"
    }
}
