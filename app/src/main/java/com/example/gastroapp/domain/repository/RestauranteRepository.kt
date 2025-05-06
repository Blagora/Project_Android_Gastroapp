package com.example.gastroapp.domain.repository

import com.example.gastroapp.model.Restaurante
import kotlinx.coroutines.flow.Flow

interface RestauranteRepository {

    /**
     * Obtiene un Flow de la lista de restaurantes desde el caché local
     */
    fun getRestaurantes(): Flow<List<Restaurante>>

    /**
     * Actualiza el caché local con datos de Firebase
     */
    suspend fun refreshRestaurantes()
}