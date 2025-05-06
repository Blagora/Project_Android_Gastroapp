package com.example.gastroapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gastroapp.R
import com.example.gastroapp.adapter.RestauranteAdapter
import com.example.gastroapp.databinding.ActivityListaRestaurantesBinding
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.presentation.restaurants.RestaurantesViewModel
import com.example.gastroapp.presentation.restaurants.RestaurantesUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListaRestaurantesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaRestaurantesBinding
    private lateinit var restauranteAdapter: RestauranteAdapter
    private val viewModel: RestaurantesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaRestaurantesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupFab()
        observeUiState()
    }

    private fun setupRecyclerView() {
        restauranteAdapter = RestauranteAdapter(
            restaurantes = emptyList(),
            onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
            onReservarClick = { restaurante -> navegarAReserva(restaurante) }
        )
        binding.recyclerViewRestaurantes.adapter = restauranteAdapter
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            viewModel.loadRestaurantes()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RestaurantesUiState.Loading -> mostrarCargando(true)
                        is RestaurantesUiState.Success -> {
                            mostrarCargando(false)
                            actualizarRestaurantes(state.restaurantes)
                        }
                        is RestaurantesUiState.Error -> {
                            mostrarCargando(false)
                            mostrarError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun actualizarRestaurantes(restaurantes: List<Restaurante>) {
        restauranteAdapter = RestauranteAdapter(
            restaurantes = restaurantes,
            onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
            onReservarClick = { restaurante -> navegarAReserva(restaurante) }
        )
        binding.recyclerViewRestaurantes.adapter = restauranteAdapter
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.recyclerViewRestaurantes.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun navegarADetallesRestaurante(restaurante: Restaurante) {
        Toast.makeText(this, getString(R.string.ver_detalles_de, restaurante.nombre), Toast.LENGTH_SHORT).show()
    }

    private fun navegarAReserva(restaurante: Restaurante) {
        Toast.makeText(this, getString(R.string.reservar_en, restaurante.nombre), Toast.LENGTH_SHORT).show()
    }
}