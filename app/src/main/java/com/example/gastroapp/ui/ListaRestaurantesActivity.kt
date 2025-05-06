package com.example.gastroapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.adapter.RestauranteAdapter
import com.example.gastroapp.databinding.ActivityListaRestaurantesBinding
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.domain.repository.RestauranteRepository // <-- IMPORTANTE: Importa la INTERFAZ del DOMAIN
import dagger.hilt.android.AndroidEntryPoint // Import para Hilt
import javax.inject.Inject // Import para Hilt

@AndroidEntryPoint // <-- AÑADE esta anotación para habilitar Hilt en la Activity
class ListaRestaurantesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaRestaurantesBinding
    private lateinit var restauranteAdapter: RestauranteAdapter

    @Inject // <-- Hilt inyectará la dependencia aquí
    lateinit var restauranteRepository: RestauranteRepository // <-- Declara la INTERFAZ

    // ELIMINA la instanciación manual:
    // private val restauranteRepository = RestauranteRepository()

    private val restaurantes = mutableListOf<Restaurante>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla el binding ANTES de llamar a super.onCreate si usas alguna función de ciclo de vida con el binding
        // pero generalmente después está bien si sólo se usa en onCreate y posteriores.
        binding = ActivityListaRestaurantesBinding.inflate(layoutInflater)
        setContentView(binding.root) // Usa la vista raíz del binding

        // En este punto, Hilt ya ha inyectado 'restauranteRepository'

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupFab()

        cargarRestaurantes() // Usará la instancia inyectada
    }

    private fun setupRecyclerView() {
        // Asegúrate que RestauranteAdapter exista y funcione como esperas
        restauranteAdapter = RestauranteAdapter(
            // NO pasar 'this' aquí
            restaurantes, // <--- 1er Argumento: La lista
            onItemClick = { restaurante -> // <--- 2do Argumento: Lambda Item Click
                navegarADetallesRestaurante(restaurante)
            },
            onReservarClick = { restaurante -> // <--- 3er Argumento: Lambda Reservar Click
                navegarAReserva(restaurante)
            }
        )

        // El resto del código para configurar el RecyclerView está bien aquí
        binding.recyclerViewRestaurantes.apply {
            layoutManager = LinearLayoutManager(this@ListaRestaurantesActivity)
            adapter = restauranteAdapter
        }
    }

    private fun setupFab() {
        binding.fabAgregarRestaurantes.setOnClickListener {
            // Llama al método en la instancia inyectada
            restauranteRepository.poblarRestaurantesFirebase()
            Toast.makeText(this, "Añadiendo restaurantes de prueba...", Toast.LENGTH_SHORT).show()

            binding.fabAgregarRestaurantes.postDelayed({
                cargarRestaurantes()
            }, 3000) // Considera usar Coroutines/ViewModel para manejar esto mejor
        }
    }

    private fun cargarRestaurantes() {
        mostrarCargando(true)

        // Llama al método en la instancia inyectada
        restauranteRepository.obtenerRestaurantes { listaRestaurantes ->
            // Es más seguro usar view?.post o runOnUiThread si esto pudiera
            // llamarse cuando la vista no está totalmente lista, pero dentro
            // del callback de Firebase llamado desde onCreate suele ser seguro.
            runOnUiThread {
                mostrarCargando(false)
                if (listaRestaurantes.isNotEmpty()) {
                    restaurantes.clear()
                    restaurantes.addAll(listaRestaurantes)
                    // Verifica que restauranteAdapter no sea null aquí
                    if (::restauranteAdapter.isInitialized) {
                        restauranteAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this, "No hay restaurantes disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        // Accede a las vistas mediante el binding
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.recyclerViewRestaurantes.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    // --- Métodos de Navegación (sin cambios) ---
    private fun navegarADetallesRestaurante(restaurante: Restaurante) {
        Toast.makeText(this, "Ver detalles de: ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
        // val intent = Intent(this, DetalleRestauranteActivity::class.java)
        // intent.putExtra("RESTAURANTE_ID", restaurante.id)
        // startActivity(intent)
    }

    private fun navegarAReserva(restaurante: Restaurante) {
        Toast.makeText(this, "Reservar en: ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
        // Implementa la navegación real
    }
}