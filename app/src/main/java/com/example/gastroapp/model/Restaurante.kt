package com.example.gastroapp.model

import com.google.firebase.firestore.GeoPoint // Importa GeoPoint

data class Restaurante(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val galeriaImagenes: List<String> = listOf(),
    val calificacionPromedio: Float = 0.0f,
    val horario: Map<String, HorarioDia> = mapOf(),
    val ubicacion: GeoPoint? = null
)