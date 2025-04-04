package com.example.gastroapp.presentation.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gastroapp.presentation.events.models.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor() : ViewModel() {

    private val _currentEvents = MutableLiveData<List<Event>>()
    val currentEvents: LiveData<List<Event>> = _currentEvents

    private val _upcomingEvents = MutableLiveData<List<Event>>()
    val upcomingEvents: LiveData<List<Event>> = _upcomingEvents

    init {
        loadMockEvents()
    }

    fun getCurrentLocation() {
        // TODO: Implementar obtención de ubicación real
        // Por ahora usamos una ubicación fija de Bogotá
        loadMockEvents()
    }

    private fun loadMockEvents() {
        val currentEventsList = listOf(
            Event(
                id = 1,
                title = "Burger Máster Bogotá",
                description = "Concurso de hamburguesas del 10 al 17 de octubre de 2024 en la ciudad de Bogotá",
                price = "18.000 COP",
                imageUrl = "https://example.com/burger.jpg",
                date = "10-17 Oct 2024",
                location = "Bogotá D.C",
                restaurantName = "Burger Máster"
            ),
            Event(
                id = 2,
                title = "Bogotá eats A cielo abierto",
                description = "Vuelve el festival gastronómico más importante de Bogotá del 13-14-15 de Octubre",
                price = "15.000 COP",
                imageUrl = "https://example.com/food-festival.jpg",
                date = "13-15 Oct 2024",
                location = "Parque 93, Bogotá",
                restaurantName = "Varios restaurantes"
            )
        )

        val upcomingEventsList = listOf(
            Event(
                id = 3,
                title = "Gastrofest",
                description = "Gastrofest 2024 trae una oferta para quienes, en sus recorridos turísticos, disfrutan de la gastronomía",
                price = "Entrada libre",
                imageUrl = "https://example.com/gastrofest.jpg",
                date = "4-20 Oct 2024",
                location = "Zona G, Bogotá",
                restaurantName = "Varios restaurantes"
            )
        )

        _currentEvents.value = currentEventsList
        _upcomingEvents.value = upcomingEventsList
    }
} 