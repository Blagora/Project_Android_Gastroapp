package com.example.gastroapp.di

import com.example.gastroapp.data.repository.RestauranteRepositoryImpl // <-- Verifica esta ruta (DATA)
import com.example.gastroapp.domain.repository.RestauranteRepository // <-- Verifica esta ruta (DOMAIN)
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        restaurantRepositoryImpl: RestauranteRepositoryImpl // El parámetro es la IMPLEMENTACIÓN
    ): RestauranteRepository // El retorno es la INTERFAZ
}