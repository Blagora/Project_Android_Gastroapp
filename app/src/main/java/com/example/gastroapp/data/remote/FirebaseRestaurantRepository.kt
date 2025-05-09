package com.example.gastroapp.data.remote

import android.util.Log
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.model.HorarioDia
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class FirebaseRestaurantRepository {
    private val TAG = "RestaurantRepository"
    private val db = FirebaseFirestore.getInstance()
    private val restaurantesCollection = db.collection("restaurantes")

    suspend fun addRestaurant(restaurant: Restaurante): Boolean {
        return try {
            Log.d(TAG, "Añadiendo restaurante: ${restaurant.nombre}")

            val restaurantMap = mapOf(
                "nombre" to restaurant.nombre,
                "descripcion" to restaurant.descripcion,
                "calificacionPromedio" to restaurant.calificacionPromedio,
                "galeriaImagenes" to restaurant.galeriaImagenes,
                "horario" to convertHorarioToMap(restaurant.horario), // Ya usa la función corregida
                "ubicacion" to restaurant.ubicacion
                // El ID se generará automáticamente por Firestore si lo usas
            )
            // Para generar ID automático y no preocuparte por el campo "id" en el modelo al escribir:
            val docRef = restaurantesCollection.add(restaurantMap).await()
            Log.d(TAG, "Restaurante añadido exitosamente con ID: ${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al añadir restaurante: ${restaurant.nombre}", e)
            false
        }
    }

    private fun convertHorarioToMap(horario: Map<String, HorarioDia>): Map<String, Map<String, Any>> { // Any para incluir Int
        return horario.mapValues { (_, horarioDia) ->
            mapOf(
                "inicio" to horarioDia.inicio,
                "fin" to horarioDia.fin,
                "maxReservas" to horarioDia.maxReservas
            )
        }
    }

    companion object {
        private const val TAG_COMPANION = "FirebaseRestaurantRepoC"

        suspend fun loadSampleData(): Int {
            val repository = FirebaseRestaurantRepository()
            var countSuccess = 0

            Log.d(TAG_COMPANION, "Iniciando carga de datos de muestra...")

            val sampleRestaurants = listOf(
                Restaurante(
                    // ...
                    // Asegúrate de usar com.google.firebase.firestore.GeoPoint aquí
                    ubicacion = com.google.firebase.firestore.GeoPoint(4.624335, -74.063644) // O solo GeoPoint si la importación es única y correcta
                ),
                Restaurante(
                    // ...
                    ubicacion = com.google.firebase.firestore.GeoPoint(4.632846, -74.085064) // O solo GeoPoint
                )
                // ...
            )
            // ...
            return countSuccess
        }
    }
}