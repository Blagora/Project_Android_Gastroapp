package com.example.gastroapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class Evento(
    @DocumentId
    val id: String = "",
    val nombreEvento: String = "",
    val descripcionEvento: String = "",
    val imagenUrl: String = "",
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null,
    val precioEvento: String = "0",
    val ciudad: String = "",
    val ubicacionTexto: String? = null,
    val ubicacionGeo: GeoPoint? = null,
    val estado: String = "", // "Proximo", "En curso", "Finalizado"
    val organizador: String? = null,
    val linkInformacion: String? = null,
    val restaurantesParticipantes: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null
)