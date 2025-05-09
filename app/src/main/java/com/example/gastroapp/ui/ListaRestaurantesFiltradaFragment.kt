package com.example.gastroapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.gastroapp.adapter.RestauranteAdapter
import com.example.gastroapp.databinding.FragmentRestaurantsBinding
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.presentation.restaurants.RestaurantesUiState
import com.example.gastroapp.presentation.restaurants.RestaurantesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListaRestaurantesFiltradaFragment : Fragment() {

    private var _binding: FragmentRestaurantsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestaurantesViewModel by viewModels()
    private lateinit var restauranteAdapter: RestauranteAdapter
    private var listaCompletaRestaurantes: List<Restaurante> = emptyList()

    // Define un TAG constante para los logs de este Fragment
    companion object {
        private const val TAG = "RestaurantsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentRestaurantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        setupRecyclerView()
        observeUiState()
        setupFilters()
        Log.d(TAG, "Llamando a viewModel.loadRestaurantes()")
        viewModel.loadRestaurantes()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Configurando adapter y RecyclerView.")
        restauranteAdapter = RestauranteAdapter(
            restaurantes = emptyList(),
            onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
            onReservarClick = { restaurante -> navegarAReserva(restaurante) }
        )
        binding.restaurantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restauranteAdapter
        }
    }

    private fun observeUiState() {
        Log.d(TAG, "observeUiState: Iniciando observación de uiState.")
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Log.d(TAG, "observeUiState: Nuevo estado recibido -> ${state::class.java.simpleName}")
                    when (state) {
                        is RestaurantesUiState.Loading -> mostrarCargando(true)
                        is RestaurantesUiState.Success -> {
                            Log.d(TAG, "observeUiState: Success recibido con ${state.restaurantes.size} restaurantes.")
                            mostrarCargando(false)
                            listaCompletaRestaurantes = state.restaurantes
                            aplicarFiltrosYActualizarLista()
                        }
                        is RestaurantesUiState.Error -> {
                            Log.e(TAG, "observeUiState: Error recibido -> ${state.message}")
                            mostrarCargando(false)
                            mostrarError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupFilters() {
        Log.d(TAG, "setupFilters: Configurando listeners para búsqueda y chips.")
        binding.searchEditText.addTextChangedListener { editable ->
            Log.d(TAG,"Filtro: Texto de búsqueda cambiado -> ${editable.toString()}")
            aplicarFiltrosYActualizarLista()
        }

        binding.chipCerca.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG,"Filtro: Chip Cerca cambiado -> $isChecked")
            aplicarFiltrosYActualizarLista()
        }

        binding.chipMejorValorados.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG,"Filtro: Chip Mejor Valorados cambiado -> $isChecked")
            aplicarFiltrosYActualizarLista()
        }
    }

    private fun aplicarFiltrosYActualizarLista() {
        Log.d(TAG, "aplicarFiltrosYActualizarLista: Iniciando aplicación de filtros.")

        // Validación: Verifica si la lista base tiene datos
        if (listaCompletaRestaurantes.isEmpty()) {
            Log.d(TAG, "aplicarFiltrosYActualizarLista: listaCompletaRestaurantes está vacía. Actualizando adapter con lista vacía.")
            restauranteAdapter = RestauranteAdapter(
                restaurantes = emptyList(), // Asegura pasar lista vacía explícitamente
                onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
                onReservarClick = { restaurante -> navegarAReserva(restaurante) }
            )
            binding.restaurantsRecyclerView.adapter = restauranteAdapter
            return // Salir si no hay datos base para filtrar
        }

        var restaurantesFiltrados = listaCompletaRestaurantes
        Log.d(TAG, "aplicarFiltrosYActualizarLista: Tamaño lista completa = ${listaCompletaRestaurantes.size}")

        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            restaurantesFiltrados = restaurantesFiltrados.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.descripcion.contains(query, ignoreCase = true)
            }
            Log.d(TAG, "aplicarFiltrosYActualizarLista: Después de filtro texto, tamaño = ${restaurantesFiltrados.size}")
        }

        if (binding.chipCerca.isChecked) {
            Log.d(TAG, "aplicarFiltrosYActualizarLista: Chip Cerca activado - Lógica pendiente")
            // TODO: Implementar lógica de filtro por cercanía
        }

        if (binding.chipMejorValorados.isChecked) {
            restaurantesFiltrados = restaurantesFiltrados.sortedByDescending { it.calificacionPromedio }
            Log.d(TAG, "aplicarFiltrosYActualizarLista: Después de filtro Mejor Valorados, tamaño = ${restaurantesFiltrados.size}")
        }

        Log.d(TAG, "aplicarFiltrosYActualizarLista: Actualizando adapter con ${restaurantesFiltrados.size} restaurantes filtrados.")
        restauranteAdapter = RestauranteAdapter(
            restaurantes = restaurantesFiltrados,
            onItemClick = { restaurante -> navegarADetallesRestaurante(restaurante) },
            onReservarClick = { restaurante -> navegarAReserva(restaurante) }
        )
        binding.restaurantsRecyclerView.adapter = restauranteAdapter
    }

    private fun mostrarCargando(mostrar: Boolean) {
        Log.d(TAG, "mostrarCargando: $mostrar")
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.restaurantsRecyclerView.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun mostrarError(mensaje: String) {
        Log.e(TAG, "mostrarError: $mensaje")
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    private fun navegarADetallesRestaurante(restaurante: Restaurante) {
        Log.d(TAG, "navegarADetallesRestaurante: Click en ${restaurante.nombre}, ID: ${restaurante.id}")

        if (restaurante.id.isEmpty()) {
            Log.e(TAG, "Error: ID del restaurante está vacío, no se puede navegar.")
            Toast.makeText(context, "Error: No se pudo obtener el ID del restaurante", Toast.LENGTH_SHORT).show()
            return
        }

        // ESTA ES LA PARTE CRUCIAL DE LA NAVEGACIÓN
        try {
            val action = ListaRestaurantesFiltradaFragmentDirections
                .actionNavigationRestaurantsToRestaurantProfileFragment(
                    restaurantId = restaurante.id,
                    restaurantName = restaurante.nombre
                )
            findNavController().navigate(action)
            Log.d(TAG, "Navegación iniciada hacia el perfil del restaurante.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al intentar navegar: ", e)
            Toast.makeText(context, "Error al intentar abrir detalles: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navegarAReserva(restaurante: Restaurante) {
        Log.d(TAG, "navegarAReserva: ${restaurante.nombre}")
        Toast.makeText(requireContext(), "Reservar en: ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
}