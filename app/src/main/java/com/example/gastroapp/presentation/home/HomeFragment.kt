package com.example.gastroapp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.R
import com.example.gastroapp.adapter.RestauranteAdapter
import com.example.gastroapp.databinding.FragmentHomeBinding
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.presentation.restaurants.RestaurantesViewModel
import com.example.gastroapp.presentation.restaurants.RestaurantesUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RestaurantesViewModel by viewModels()
    private lateinit var restauranteAdapter: RestauranteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        viewModel.loadRestaurantes()
    }

    private fun setupListeners() {
        binding.cardRestaurants.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_mapaRestaurantesFragment)
        }
    }

    private fun setupRecyclerView() {
        restauranteAdapter = RestauranteAdapter(
            restaurantes = emptyList(),
            onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
            onReservarClick = { restaurante -> navegarAReserva(restaurante) }
        )
        binding.recyclerViewRestaurantes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restauranteAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
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
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    private fun navegarADetallesRestaurante(restaurante: Restaurante) {
        // Implementar navegación
        Toast.makeText(requireContext(), "Ver detalles de: ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
    }

    private fun navegarAReserva(restaurante: Restaurante) {
        // Implementar navegación
        Toast.makeText(requireContext(), "Reservar en: ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}