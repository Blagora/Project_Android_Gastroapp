    package com.example.gastroapp.domain.repository

    import com.example.gastroapp.model.Restaurante
    import com.example.gastroapp.model.PlatoMenu
    import kotlinx.coroutines.flow.Flow

    interface RestauranteRepository {
        fun getRestaurantes(): Flow<List<Restaurante>>
        suspend fun refreshRestaurantes()
        suspend fun getRestaurantById(id: String): Restaurante?
        suspend fun getMenuPorRestauranteId(restauranteId: String): List<PlatoMenu>
        suspend fun getRestauranteConMenu(restauranteId: String): Restaurante?
    }