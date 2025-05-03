package com.example.gastroapp.domain.repository

import com.example.gastroapp.model.Restaurante

interface RestauranteRepository {

    /**
     * Declara la operación para obtener la lista de restaurantes.
     * La implementación se encargará de cómo obtenerlos (Firebase, red, base de datos local, etc.).
     *
     * @param callback Una función que será invocada con el resultado (la lista de restaurantes).
     */
    fun obtenerRestaurantes(callback: (List<Restaurante>) -> Unit)

    /**
     * Declara la operación para poblar la base de datos con datos de prueba.
     * La implementación decidirá cómo hacerlo.
     * (Esta función es más una utilidad de desarrollo que una operación de dominio pura).
     */
    fun poblarRestaurantesFirebase()
}