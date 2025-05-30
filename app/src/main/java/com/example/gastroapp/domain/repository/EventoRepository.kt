package com.example.gastroapp.domain.repository

import com.example.gastroapp.model.Evento

interface EventoRepository {
    suspend fun getEventosEnCurso(): List<Evento>
    suspend fun getEventosProximos(): List<Evento>
    suspend fun getEventoById(eventoId: String): Evento?
}