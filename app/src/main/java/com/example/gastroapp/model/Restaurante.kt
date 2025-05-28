package com.example.gastroapp.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class PlatoMenu(
    @DocumentId
    val id: String = "",
    val nombrePlato: String = "",
    val descripcionPlato: String = "",
    val seccionPlato: String = "",
    val precioPlato: Long = 0L
)

data class Restaurante(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val direccion: String = "",
    val categorias: List<String> = listOf(),
    val galeriaImagenes: List<String> = listOf(),
    val calificacionPromedio: Float = 0.0f,
    val horario: Map<String, HorarioDia> = emptyMap(),
    val ubicacion: GeoPoint? = null,
    var menu: List<PlatoMenu> = emptyList(),
    val direccionTexto: String? = null
)