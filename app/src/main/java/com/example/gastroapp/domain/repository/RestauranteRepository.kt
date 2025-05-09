    package com.example.gastroapp.domain.repository

    import com.example.gastroapp.model.Restaurante
    import kotlinx.coroutines.flow.Flow

    interface RestauranteRepository {
        fun getRestaurantes(): Flow<List<Restaurante>>
        suspend fun refreshRestaurantes()
        suspend fun getRestaurantById(id: String): Restaurante? // ASEGÚRATE DE QUE ESTÉ ASÍ
    }