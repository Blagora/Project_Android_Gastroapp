package com.example.gastroapp.data.repository

import android.util.Log
import com.example.gastroapp.data.local.RestauranteDao
import com.example.gastroapp.data.local.RestauranteEntity
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import com.google.firebase.firestore.DocumentSnapshot // Necesario para la función de mapeo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await // Necesario para .await() en Tasks de Firebase
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Singleton
class RestauranteRepositoryImpl @Inject constructor(
    private val restauranteDao: RestauranteDao
) : RestauranteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val restaurantesRef = db.collection("restaurantes")
    private val TAG = "RepositoryImpl" // TAG para los logs

    override fun getRestaurantes(): Flow<List<Restaurante>> {
        return restauranteDao.getAllRestaurantes().map { entities ->
            Log.d(TAG, "Mapeando ${entities.size} entidades de Room a modelo Restaurante")
            entities.map { it.toRestaurante() }
        }
    }

    override suspend fun refreshRestaurantes() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Iniciando refreshRestaurantes desde Firebase")
                val restaurantesFirebase = fetchRestaurantesFromFirebase() // Renombrado para claridad
                Log.d(TAG, "Restaurantes a guardar en Room: ${restaurantesFirebase.size}")
                restauranteDao.refreshRestaurantes(
                    restaurantesFirebase.map { it.toEntity() }
                )
                Log.d(TAG, "Restaurantes guardados en Room.")
            } catch (e: Exception) {
                Log.e(TAG, "Error en refreshRestaurantes", e)
            }
        }
    }

    // ***** IMPLEMENTACIÓN COMPLETADA DEL MÉTODO *****
    override suspend fun getRestaurantById(id: String): Restaurante? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Intentando obtener restaurante con ID: $id desde Firebase")
                val documentSnapshot = restaurantesRef.document(id).get().await()
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "Documento encontrado para ID: $id en Firebase")
                    mapDocumentToRestaurante(documentSnapshot) // Usar la función auxiliar de mapeo
                } else {
                    Log.w(TAG, "No se encontró documento para ID: $id en Firebase. Intentando buscar en Room.")
                    // Opcional: Fallback para buscar en la base de datos local Room
                    // val entity = restauranteDao.getRestauranteById(id) // Necesitarías añadir este método a tu DAO
                    // entity?.toRestaurante()
                    null // Si no se implementa el fallback a Room, devuelve null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo restaurante por ID '$id'", e)
                null
            }
        }
    }
    // ***** FIN DE LA IMPLEMENTACIÓN *****


    // Función auxiliar para mapear un DocumentSnapshot de Firestore a un objeto Restaurante
    // Movida aquí para ser reutilizable
    private fun mapDocumentToRestaurante(document: DocumentSnapshot): Restaurante? {
        return try {
            val id = document.id
            val nombre = document.getString("nombre") ?: ""
            val descripcion = document.getString("descripcion") ?: ""
            val calificacionPromedio = document.getDouble("calificacionPromedio")?.toFloat() ?: 0f
            val galeriaImagenes = document.get("galeriaImagenes") as? List<String> ?: emptyList()
            val ubicacion = document.getGeoPoint("ubicacion")
            val horarioMap = document.get("horario") as? Map<String, Any> ?: emptyMap()

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
            Log.e(TAG, "Error mapeando documento ${document.id} a Restaurante: ${e.message}", e)
            null
        }
    }

    private suspend fun fetchRestaurantesFromFirebase(): List<Restaurante> =
        suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "Iniciando fetchRestaurantesFromFirebase")
            // val task = // Ya no necesitamos la variable task si solo la usamos para el invokeOnCancellation
            restaurantesRef.get()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "Documentos de Firebase obtenidos: ${result.documents.size}")
                    val restaurantes = result.documents.mapNotNull { document ->
                        // La lógica de mapeo ahora está en mapDocumentToRestaurante
                        mapDocumentToRestaurante(document)
                    }
                    Log.d(TAG, "Restaurantes mapeados desde Firebase: ${restaurantes.size}")
                    if (!continuation.isCompleted) {
                        continuation.resume(restaurantes)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error en fetchRestaurantesFromFirebase", exception)
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(exception)
                    }
                }

            continuation.invokeOnCancellation {
                Log.d(TAG, "fetchRestaurantesFromFirebase cancelado por corrutina")
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