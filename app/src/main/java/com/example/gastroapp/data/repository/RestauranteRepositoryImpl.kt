package com.example.gastroapp.data.repository

import android.util.Log
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestauranteRepositoryImpl @Inject constructor() : RestauranteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val restaurantesRef = db.collection("restaurantes")

    override fun obtenerRestaurantes(callback: (List<Restaurante>) -> Unit) {
        restaurantesRef.get()
            .addOnSuccessListener { result ->
                val restaurantes = mutableListOf<Restaurante>()
                for (document in result) {
                    try {
                        val id = document.id
                        val nombre = document.getString("nombre") ?: ""
                        val descripcion = document.getString("descripcion") ?: ""
                        val calificacionPromedio = document.getDouble("calificacionPromedio")?.toFloat() ?: 0f
                        val galeriaImagenes = document.get("galeriaImagenes") as? List<String> ?: listOf()
                        val ubicacion: GeoPoint? = document.getGeoPoint("ubicacion") // Asegúrate que es nullable GeoPoint?
                        val horarioMap = document.get("horario") as? Map<String, Any> ?: mapOf()

                        // --- CORRECCIÓN AQUÍ ---
                        val horario = mutableMapOf<String, HorarioDia>() // Usa solo HorarioDia como tipo
                        // --- FIN CORRECCIÓN ---

                        for ((dia, datos) in horarioMap) {
                            if (datos is Map<*, *>) {
                                val inicio = datos["inicio"] as? String ?: ""
                                val fin = datos["fin"] as? String ?: ""
                                val maxReservas = (datos["maxReservas"] as? Number)?.toInt() ?: 0
                                horario[dia] = HorarioDia(inicio, fin, maxReservas)
                            }
                        }

                        val restaurante = Restaurante(
                            id = id,
                            nombre = nombre,
                            descripcion = descripcion,
                            galeriaImagenes = galeriaImagenes,
                            calificacionPromedio = calificacionPromedio,
                            horario = horario,
                            ubicacion = ubicacion
                        )

                        restaurantes.add(restaurante)
                    } catch (e: Exception) {
                        Log.e("RestauranteRepositoryImpl", "Error al procesar documento: ${document.id}", e)
                    }
                }
                callback(restaurantes)
            }
            .addOnFailureListener { exception ->
                Log.e("RestauranteRepositoryImpl", "Error obteniendo restaurantes", exception)
                callback(emptyList())
            }
    }

    override fun poblarRestaurantesFirebase() {
        // Coordenadas aproximadas para Bogotá, Colombia
        val ubicacionBogota = GeoPoint(4.6097, -74.0817)

        val restaurantesFicticios = listOf(
            // Restaurante 1: Italiano
            crearRestaurante(
                nombre = "La Trattoria di Roma",
                descripcion = "Auténtica cocina italiana con recetas tradicionales. Pastas hechas a mano.",
                galeriaImagenes = listOf( /* "URL_ITALIANO_1", "URL_ITALIANO_2" */ ), // Reemplaza con tus URLs reales
                calificacionPromedio = 4.7f,
                horario = crearHorarioCompleto(
                    lunes = crearHorarioDia("19:00", "22:30", 20), // Solo cena
                    martes = crearHorarioDia("12:00", "15:30", 25).plus("19:00" to "22:30" as Any), // Almuerzo y cena - Nota: Esto es un ejemplo simple, Firestore podría necesitar una estructura más compleja para múltiples turnos
                    miercoles = crearHorarioDia("12:00", "15:30", 25).plus("19:00" to "22:30" as Any),
                    jueves = crearHorarioDia("12:00", "15:30", 30).plus("19:00" to "23:00" as Any),
                    viernes = crearHorarioDia("12:00", "16:00", 35).plus("19:00" to "23:30" as Any),
                    sabado = crearHorarioDia("12:30", "16:00", 40).plus("19:00" to "23:30" as Any),
                    domingo = crearHorarioDia("13:00", "17:00", 30)  // Solo almuerzo extendido
                ),
                ubicacion = ubicacionBogota // Ubicación en Bogotá
            ),

            // Restaurante 2: Español
            crearRestaurante(
                nombre = "El Mesón Castellano",
                descripcion = "Cocina tradicional española, especialidad en carnes a la brasa.",
                galeriaImagenes = listOf( /* "URL_ESPANOL_1", "URL_ESPANOL_2" */ ), // Reemplaza con tus URLs reales
                calificacionPromedio = 4.5f,
                horario = crearHorarioCompleto(
                    lunes = crearHorarioDia("", "", 0), // Cerrado
                    martes = crearHorarioDia("13:00", "16:30", 20), // Solo almuerzo
                    miercoles = crearHorarioDia("13:00", "16:30", 20),
                    jueves = crearHorarioDia("13:00", "16:30", 25).plus("20:00" to "23:00" as Any), // Almuerzo y cena
                    viernes = crearHorarioDia("13:00", "16:30", 30).plus("20:00" to "23:30" as Any),
                    sabado = crearHorarioDia("13:00", "17:00", 35).plus("20:00" to "23:30" as Any),
                    domingo = crearHorarioDia("13:00", "17:00", 30) // Solo almuerzo
                ),
                ubicacion = ubicacionBogota // Ubicación en Bogotá
            ),

            // Restaurante 3: Japonés
            crearRestaurante(
                nombre = "Sushi Kenzo",
                descripcion = "Auténtica experiencia japonesa con pescado fresco importado.",
                galeriaImagenes = listOf( /* "URL_JAPONES_1", "URL_JAPONES_2" */ ), // Reemplaza con tus URLs reales
                calificacionPromedio = 4.8f,
                horario = crearHorarioCompleto(
                    lunes = crearHorarioDia("12:30", "15:00", 15).plus("19:30" to "22:00" as Any),
                    martes = crearHorarioDia("12:30", "15:00", 15).plus("19:30" to "22:00" as Any),
                    miercoles = crearHorarioDia("12:30", "15:00", 18).plus("19:30" to "22:30" as Any),
                    jueves = crearHorarioDia("12:30", "15:00", 18).plus("19:30" to "22:30" as Any),
                    viernes = crearHorarioDia("12:30", "15:30", 20).plus("19:30" to "23:00" as Any),
                    sabado = crearHorarioDia("13:00", "15:30", 20).plus("19:30" to "23:00" as Any),
                    domingo = crearHorarioDia("", "", 0) // Cerrado
                ),
                ubicacion = ubicacionBogota // Ubicación en Bogotá
            ),

            // Restaurante 4: Francés Bistrot
            crearRestaurante(
                nombre = "Le Petit Bistrot",
                descripcion = "Un rincón de París. Cocina francesa tradicional y vinos selectos.",
                galeriaImagenes = listOf( /* "URL_FRANCES_1", "URL_FRANCES_2" */ ), // Reemplaza con tus URLs reales
                calificacionPromedio = 4.6f,
                horario = crearHorarioCompleto(
                    lunes = crearHorarioDia("", "", 0), // Cerrado
                    martes = crearHorarioDia("19:00", "23:00", 25), // Solo cena
                    miercoles = crearHorarioDia("19:00", "23:00", 25),
                    jueves = crearHorarioDia("13:00", "15:30", 20).plus("19:00" to "23:00" as Any), // Almuerzo y cena
                    viernes = crearHorarioDia("13:00", "15:30", 25).plus("19:00" to "23:30" as Any),
                    sabado = crearHorarioDia("13:00", "16:00", 30).plus("19:00" to "23:30" as Any),
                    domingo = crearHorarioDia("13:00", "16:00", 25) // Solo almuerzo
                ),
                ubicacion = ubicacionBogota // Ubicación en Bogotá
            ),

            // Restaurante 5: Parrilla Argentina
            crearRestaurante(
                nombre = "La Parrilla Argentina",
                descripcion = "Las mejores carnes argentinas a la parrilla y vinos Malbec.",
                galeriaImagenes = listOf( /* "URL_ARGENTINO_1", "URL_ARGENTINO_2" */ ), // Reemplaza con tus URLs reales
                calificacionPromedio = 4.4f,
                horario = crearHorarioCompleto(
                    lunes = crearHorarioDia("19:30", "23:00", 30), // Solo cena
                    martes = crearHorarioDia("19:30", "23:00", 30),
                    miercoles = crearHorarioDia("12:30", "15:30", 25).plus("19:30" to "23:00" as Any),
                    jueves = crearHorarioDia("12:30", "15:30", 25).plus("19:30" to "23:30" as Any),
                    viernes = crearHorarioDia("12:30", "16:00", 35).plus("19:30" to "00:00" as Any),
                    sabado = crearHorarioDia("13:00", "16:00", 40).plus("19:30" to "00:00" as Any),
                    domingo = crearHorarioDia("13:00", "17:00", 35)
                ),
                ubicacion = ubicacionBogota
            )
        )

        restaurantesFicticios.forEach { restauranteData ->
            restaurantesRef.add(restauranteData)
                .addOnSuccessListener { documentReference ->
                    Log.d("FirebaseImpl", "Restaurante añadido con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("FirebaseImpl", "Error añadiendo restaurante", e)
                }
        }
    }

    private fun crearRestaurante(
        nombre: String, descripcion: String, galeriaImagenes: List<String>,
        calificacionPromedio: Float, horario: Map<String, Any>, ubicacion: GeoPoint // Recibe GeoPoint NO NULO para escribir
    ): Map<String, Any> {
        return mapOf(
            "nombre" to nombre, "descripcion" to descripcion, "galeriaImagenes" to galeriaImagenes,
            "calificacionPromedio" to calificacionPromedio, "horario" to horario, "ubicacion" to ubicacion
        )
    }

    private fun crearHorarioDia(inicio: String, fin: String, maxReservas: Int): Map<String, Any> {
        return mapOf("inicio" to inicio, "fin" to fin, "maxReservas" to maxReservas)
    }

    private fun crearHorarioCompleto(
        lunes: Map<String, Any>, martes: Map<String, Any>, miercoles: Map<String, Any>,
        jueves: Map<String, Any>, viernes: Map<String, Any>, sabado: Map<String, Any>,
        domingo: Map<String, Any>
    ): Map<String, Map<String, Any>> {
        return mapOf(
            "lunes" to lunes, "martes" to martes, "miercoles" to miercoles, "jueves" to jueves,
            "viernes" to viernes, "sabado" to sabado, "domingo" to domingo
        )
    }
}