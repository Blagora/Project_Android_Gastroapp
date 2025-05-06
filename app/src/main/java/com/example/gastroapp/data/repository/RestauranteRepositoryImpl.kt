package com.example.gastroapp.data.repository

import com.example.gastroapp.data.local.RestauranteDao
import com.example.gastroapp.data.local.RestauranteEntity
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class RestauranteRepositoryImpl @Inject constructor(
    private val restauranteDao: RestauranteDao
) : RestauranteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val restaurantesRef = db.collection("restaurantes")

    override fun getRestaurantes(): Flow<List<Restaurante>> {
        return restauranteDao.getAllRestaurantes().map { entities ->
            entities.map { it.toRestaurante() }
        }
    }

    override suspend fun refreshRestaurantes() {
        withContext(Dispatchers.IO) {
            try {
                val restaurantes = fetchRestaurantesFromFirebase()
                restauranteDao.refreshRestaurantes(
                    restaurantes.map { it.toEntity() }
                )
            } catch (e: Exception) {
                // Si falla la actualización, seguimos usando el caché local
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchRestaurantesFromFirebase(): List<Restaurante> = 
        suspendCancellableCoroutine { continuation ->
            val task = restaurantesRef.get()
                .addOnSuccessListener { result ->
                    val restaurantes = result.documents.mapNotNull { document ->
                        try {
                            val id = document.id
                            val nombre = document.getString("nombre") ?: ""
                            val descripcion = document.getString("descripcion") ?: ""
                            val calificacionPromedio = document.getDouble("calificacionPromedio")?.toFloat() ?: 0f
                            val galeriaImagenes = document.get("galeriaImagenes") as? List<String> ?: listOf()
                            val ubicacion = document.getGeoPoint("ubicacion")
                            val horarioMap = document.get("horario") as? Map<String, Any> ?: mapOf()

                            val horario = horarioMap.mapValues { (_, datos) ->
                                if (datos is Map<*, *>) {
                                    val inicio = datos["inicio"] as? String ?: ""
                                    val fin = datos["fin"] as? String ?: ""
                                    val maxReservas = (datos["maxReservas"] as? Number)?.toInt() ?: 0
                                    HorarioDia(inicio, fin, maxReservas)
                                } else {
                                    HorarioDia("", "", 0)
                                }
                            }

                            Restaurante(
                                id = id,
                                nombre = nombre,
                                descripcion = descripcion,
                                galeriaImagenes = galeriaImagenes,
                                calificacionPromedio = calificacionPromedio,
                                horario = horario,
                                ubicacion = ubicacion
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    if (!continuation.isCompleted) {
                        continuation.resume(restaurantes) {}
                    }
                }
                .addOnFailureListener { exception ->
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(exception)
                    }
                }

            continuation.invokeOnCancellation {
                task.isCanceled
            }
        }

    private fun RestauranteEntity.toRestaurante(): Restaurante {
        return Restaurante(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            galeriaImagenes = galeriaImagenes,
            calificacionPromedio = calificacionPromedio,
            horario = horario,
            ubicacion = latitud?.let { lat -> longitud?.let { lng -> 
                GeoPoint(lat, lng)
            }}
        )
    }

    private fun Restaurante.toEntity(): RestauranteEntity {
        return RestauranteEntity(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            galeriaImagenes = galeriaImagenes,
            calificacionPromedio = calificacionPromedio,
            horario = horario,
            latitud = ubicacion?.latitude,
            longitud = ubicacion?.longitude
        )
    }
}