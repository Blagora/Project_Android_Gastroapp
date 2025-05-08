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

            // Convertir explícitamente a un mapa para evitar problemas de serialización
            val restaurantMap = mapOf(
                "nombre" to restaurant.nombre,
                "descripcion" to restaurant.descripcion,
                "calificacionPromedio" to restaurant.calificacionPromedio,
                "galeriaImagenes" to restaurant.galeriaImagenes,
                "horario" to convertHorarioToMap(restaurant.horario),
                "ubicacion" to restaurant.ubicacion
            )

            // Usar add en lugar de document().set() para generar un ID automático
            val docRef = restaurantesCollection.add(restaurantMap).await()
            Log.d(TAG, "Restaurante añadido exitosamente con ID: ${docRef.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al añadir restaurante: ${restaurant.nombre}", e)
            false
        }
    }

    // Convertir el mapa de horarios a un formato que Firestore pueda guardar fácilmente
    private fun convertHorarioToMap(horario: Map<String, HorarioDia>): Map<String, Map<String, String>> {
        return horario.mapValues { (_, horarioDia) ->
            mapOf(
                "horaApertura" to horarioDia.inicio,
                "horaCierre" to horarioDia.fin
            )
        }
    }

    companion object {
        private const val TAG = "RestaurantRepository"

        suspend fun loadSampleData(): Int {
            val repository = FirebaseRestaurantRepository()
            var countSuccess = 0

            Log.d(TAG, "Iniciando carga de datos de muestra...")

            val sampleRestaurants = listOf(
                Restaurante(
                    nombre = "La Cevichería Bogotana",
                    descripcion = "Auténticos ceviches peruanos con toque colombiano. Ambiente moderno y acogedor.",
                    calificacionPromedio = 4.5f,
                    galeriaImagenes = listOf(
                        "https://example.com/cevicheria1.jpg",
                        "https://example.com/cevicheria2.jpg"
                    ),
                    horario = mapOf(
                        "lunes" to HorarioDia("12:00", "22:00"),
                        "martes" to HorarioDia("12:00", "22:00"),
                        "miercoles" to HorarioDia("12:00", "22:00"),
                        "jueves" to HorarioDia("12:00", "22:00"),
                        "viernes" to HorarioDia("12:00", "23:00"),
                        "sabado" to HorarioDia("11:00", "23:00"),
                        "domingo" to HorarioDia("11:00", "22:00")
                    ),
                    ubicacion = GeoPoint(4.624335, -74.063644)
                ),
                Restaurante(
                    nombre = "Parrillada La Sabana",
                    descripcion = "Auténtica parrillada bogotana. Carnes de primera calidad y ambiente familiar.",
                    calificacionPromedio = 4.7f,
                    galeriaImagenes = listOf(
                        "https://example.com/parrillada1.jpg",
                        "https://example.com/parrillada2.jpg"
                    ),
                    horario = mapOf(
                        "lunes" to HorarioDia("11:30", "23:00"),
                        "martes" to HorarioDia("11:30", "23:00"),
                        "miercoles" to HorarioDia("11:30", "23:00"),
                        "jueves" to HorarioDia("11:30", "23:00"),
                        "viernes" to HorarioDia("11:30", "24:00"),
                        "sabado" to HorarioDia("11:30", "24:00"),
                        "domingo" to HorarioDia("11:30", "23:00")
                    ),
                    ubicacion = GeoPoint(4.632846, -74.085064)
                )
                // Añade el resto de los restaurantes aquí...
            )

            for (restaurant in sampleRestaurants) {
                try {
                    val success = repository.addRestaurant(restaurant)
                    if (success) countSuccess++
                    // Añadir un pequeño retraso para evitar problemas con límites de frecuencia
                    kotlinx.coroutines.delay(100)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar el restaurante: ${restaurant.nombre}", e)
                }
            }

            Log.d(TAG, "Carga completada: $countSuccess restaurantes añadidos")
            return countSuccess
        }
    }
}