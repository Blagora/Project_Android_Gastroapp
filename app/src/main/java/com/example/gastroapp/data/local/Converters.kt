package com.example.gastroapp.data.local

import androidx.room.TypeConverter
import com.example.gastroapp.model.HorarioDia
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromHorarioMap(value: Map<String, HorarioDia>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHorarioMap(value: String): Map<String, HorarioDia> {
        val mapType = object : TypeToken<Map<String, HorarioDia>>() {}.type
        return gson.fromJson(value, mapType)
    }
}