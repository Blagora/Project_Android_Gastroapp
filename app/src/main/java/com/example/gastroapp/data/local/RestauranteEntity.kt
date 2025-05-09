package com.example.gastroapp.data.local // O donde tengas tus entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.gastroapp.model.HorarioDia // Asegúrate que el import sea correcto
// import com.google.firebase.firestore.GeoPoint // No guardes GeoPoint directamente

@Entity(tableName = "restaurantes_table")
@TypeConverters(Converters::class) // Necesitarás TypeConverters para List<String> y Map
data class RestauranteEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val galeriaImagenes: List<String>, // Se manejará con TypeConverter
    val calificacionPromedio: Float,
    val horario: Map<String, HorarioDia>, // Se manejará con TypeConverter
    val latitud: Double?, // Para la ubicación
    val longitud: Double? // Para la ubicación
)