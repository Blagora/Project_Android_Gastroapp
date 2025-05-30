package com.example.gastroapp.presentation.eventos.models

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val startDate: String,
    val endDate: String,
    val location: String,
    val isUpcoming: Boolean = false
)

/**
 * Lista de eventos actuales y próximos para mostrar en la pantalla de eventos
 */
object EventsData {
    // Eventos en curso
    val currentEvents = listOf(
        Event(
            id = "1",
            title = "Burger Máster Bogotá",
            description = "Concurso de hamburguesas del 10 al 17 de octubre de 2024 en la ciudad de Bogotá",
            price = "18.000 COP",
            imageUrl = "https://burgersandmore.com.co/wp-content/uploads/2019/05/burger-master-2.jpg",
            startDate = "10/10/2024",
            endDate = "17/10/2024",
            location = "Bogotá"
        ),
        Event(
            id = "2",
            title = "Bogotá Eats A Cielo Abierto",
            description = "Vuelve el festival gastronómico más importante de Bogotá del 13-14-15 de Octubre",
            price = "desde 15.000 COP",
            imageUrl = "https://www.colombia.co/wp-content/uploads/2019/08/Feria-gastron%C3%B3mica-en-Bogot%C3%A1-ProColombia-1280x854.jpg",
            startDate = "13/10/2024",
            endDate = "15/10/2024",
            location = "Bogotá"
        )
    )

    // Eventos próximos
    val upcomingEvents = listOf(
        Event(
            id = "3",
            title = "Gastrofest",
            description = "Gastrofest 2024 trae una oferta para quienes, en sus recorridos turísticos, disfrutan de la gastronomía 4-20 OCT",
            price = "Entrada libre",
            imageUrl = "https://www.radionacional.co/sites/default/files/styles/portadas_campanias/public/imagenes/foto_detalle_mompox_fest_0.jpg",
            startDate = "04/10/2024",
            endDate = "20/10/2024",
            location = "Bogotá",
            isUpcoming = true
        ),
        Event(
            id = "4",
            title = "Festival Gastronómico Del Caribe",
            description = "Sabores del Caribe, una experiencia única con los mejores platos de la cocina del litoral colombiano",
            price = "25.000 COP",
            imageUrl = "https://www.elheraldo.co/sites/default/files/styles/width_860/public/articulo/2022/07/14/sabor_barranquilla_1.jpg",
            startDate = "25/10/2024",
            endDate = "30/10/2024",
            location = "Cartagena",
            isUpcoming = true
        )
    )
} 