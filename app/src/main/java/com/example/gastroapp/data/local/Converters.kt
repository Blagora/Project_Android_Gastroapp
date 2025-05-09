package com.example.gastroapp.data.local // O donde pongas tus convertidores

import androidx.room.TypeConverter
import com.example.gastroapp.model.HorarioDia
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Para List<String> (galeriaImagenes)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        }
    }

    // Para Map<String, HorarioDia> (horario)
    @TypeConverter
    fun fromHorarioMap(value: Map<String, HorarioDia>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toHorarioMap(value: String?): Map<String, HorarioDia>? {
        return value?.let {
            val mapType = object : TypeToken<Map<String, HorarioDia>>() {}.type
            gson.fromJson(it, mapType)
        }
    }
}