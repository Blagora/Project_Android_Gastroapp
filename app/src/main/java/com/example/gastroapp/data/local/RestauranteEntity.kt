package com.example.gastroapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.gastroapp.model.HorarioDia
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "restaurantes")
@TypeConverters(Converters::class)
data class RestauranteEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val descripcion: String,
    val galeriaImagenes: List<String>,
    val calificacionPromedio: Float,
    val horario: Map<String, HorarioDia>,
    val latitud: Double?,
    val longitud: Double?
)