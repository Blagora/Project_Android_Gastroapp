package com.example.gastroapp.presentation.events.models

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val date: String,
    val location: String,
    val restaurantName: String
) 