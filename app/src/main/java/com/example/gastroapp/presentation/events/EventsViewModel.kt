package com.example.gastroapp.presentation.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gastroapp.presentation.events.models.Event
import com.example.gastroapp.presentation.events.models.EventsData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor() : ViewModel() {

    private val _currentEvents = MutableLiveData<List<Event>>()
    val currentEvents: LiveData<List<Event>> = _currentEvents

    private val _upcomingEvents = MutableLiveData<List<Event>>()
    val upcomingEvents: LiveData<List<Event>> = _upcomingEvents

    init {
        loadEvents()
    }

    fun getCurrentLocation() {
        // TODO: Implementar obtención de ubicación real
        // Por ahora usamos una ubicación fija de Bogotá
    }

    private fun loadEvents() {
        _currentEvents.value = EventsData.currentEvents
        _upcomingEvents.value = EventsData.upcomingEvents
    }
} 