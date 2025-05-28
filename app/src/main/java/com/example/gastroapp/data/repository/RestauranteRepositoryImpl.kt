package com.example.gastroapp.data.repository

import android.util.Log
import com.google.firebase.firestore.ktx.toObjects
import com.example.gastroapp.data.local.RestauranteDao
import com.example.gastroapp.data.local.RestauranteEntity
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.model.PlatoMenu
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
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

    override suspend fun getRestaurantById(id: String): Restaurante? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Intentando obtener restaurante con ID: $id desde Firebase")
                val documentSnapshot = restaurantesRef.document(id).get().await()
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "Documento encontrado para ID: $id en Firebase")
                    val restaurante = mapDocumentToRestaurante(documentSnapshot)

                    // Si el restaurante se mapeó correctamente, obtenemos y asignamos su menú
                    if (restaurante != null) {
                        Log.d(TAG, "Restaurante base '${restaurante.nombre}' mapeado. Obteniendo su menú...")
                        val menuItems = getMenuPorRestauranteIdInternal(id) // Llamada a la función interna
                        restaurante.menu = menuItems
                        Log.d(TAG, "Menú con ${menuItems.size} items asignado a '${restaurante.nombre}'")
                    } else {
                        Log.w(TAG, "mapDocumentToRestaurante devolvió null para ID: $id")
                    }
                    restaurante
                } else {
                    Log.w(TAG, "No se encontró documento para ID: $id en Firebase.")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo restaurante por ID '$id' y su menú", e)
                null
            }
        }
    }

    private fun mapDocumentToRestaurante(document: DocumentSnapshot): Restaurante? {
        return try {
            val id = document.id
            val nombre = document.getString("nombre") ?: ""
            val descripcion = document.getString("descripcion") ?: ""
            val calificacionPromedio = document.getDouble("calificacionPromedio")?.toFloat() ?: 0f
            val galeriaImagenes = document.get("galeriaImagenes") as? List<String> ?: emptyList()
            val ubicacion = document.getGeoPoint("ubicacion")
            val horarioMap = document.get("horario") as? Map<String, Any> ?: emptyMap()
            val direccionTexto = document.getString("direccionTexto")

            val horarioMapFirestore = document.get("horario") as? Map<String, Map<String, String>> ?: emptyMap()
            val horarioConvertido = horarioMapFirestore.mapValues { entry ->
                val datosDia = entry.value
                HorarioDia(
                    inicio = datosDia["horaApertura"] ?: "",
                    fin = datosDia["horaCierre"] ?: "",
                    maxReservas = (datosDia["maxReservas"] as? Number)?.toInt() ?: 0
                )
            }

            Restaurante(
                id = id,
                nombre = nombre,
                descripcion = descripcion,
                galeriaImagenes = galeriaImagenes,
                calificacionPromedio = calificacionPromedio,
                horario = horarioConvertido,
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


    private suspend fun getMenuPorRestauranteIdInternal(restauranteId: String): List<PlatoMenu> {
        return try {
            Log.d(TAG, "Obteniendo menú (interno) para restaurante ID: $restauranteId")
            val snapshot = restaurantesRef.document(restauranteId)
                .collection("menu")
                .get()
                .await()
            val platos = snapshot.toObjects<PlatoMenu>()
            Log.d(TAG, "Menú (interno) obtenido con ${platos.size} platos para ID: $restauranteId")
            platos
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener menú (interno) para $restauranteId", e)
            emptyList()
        }
    }

    override suspend fun getRestauranteConMenu(restauranteId: String): Restaurante? {
        return getRestaurantById(restauranteId)
    }

    override suspend fun getMenuPorRestauranteId(restauranteId: String): List<PlatoMenu> {
        return getMenuPorRestauranteIdInternal(restauranteId)
    }
}