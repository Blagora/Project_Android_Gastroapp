package com.example.gastroapp.data.repository

import android.util.Log
import com.example.gastroapp.domain.repository.EventoRepository
import com.example.gastroapp.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects // O usa .toObjects(Evento::class.java) si no tienes la extensión ktx
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventoRepository {

    private val eventosCollection = firestore.collection("eventos")
    private val TAG = "EventoRepoImpl"

    override suspend fun getEventosEnCurso(): List<Evento> {
        return try {
            val snapshot = eventosCollection
                .whereEqualTo("estado", "En curso")
                .orderBy("fechaInicio", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(Evento::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener eventos en curso", e)
            emptyList()
        }
    }

    override suspend fun getEventosProximos(): List<Evento> {
        return try {
            val snapshot = eventosCollection
                .whereEqualTo("estado", "Próximo")
                .orderBy("fechaInicio", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(Evento::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener eventos próximos", e)
            emptyList()
        }
    }

    override suspend fun getEventoById(eventoId: String): Evento? {
        return try {
            val documentSnapshot = eventosCollection.document(eventoId).get().await()
            documentSnapshot.toObject(Evento::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener evento por ID: $eventoId", e)
            null
        }
    }
}