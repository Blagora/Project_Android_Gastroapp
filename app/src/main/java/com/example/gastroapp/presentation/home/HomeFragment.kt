package com.example.gastroapp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // Para navegar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.R
import com.example.gastroapp.adapter.RestauranteAdapter
import com.example.gastroapp.databinding.FragmentHomeBinding // Asegúrate que el nombre coincida con tu XML
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.Restaurante
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var restauranteRepository: RestauranteRepository

    private lateinit var restauranteAdapter: RestauranteAdapter
    private val restaurantes = mutableListOf<Restaurante>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        cargarRestaurantes()
    }

    private fun setupRecyclerView() {
        restauranteAdapter = RestauranteAdapter(
            restaurantes, // 1er argumento: La lista
            onItemClick = { restaurante -> // 2do argumento: Lambda Item Click
                navegarADetallesRestaurante(restaurante)
            },
            onReservarClick = { restaurante -> // 3er argumento: Lambda Reservar Click
                navegarAReserva(restaurante)
            }
        )
        binding.recyclerViewRestaurantes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restauranteAdapter
            // Podrías necesitar isNestedScrollingEnabled = false si hay problemas de scroll
            // isNestedScrollingEnabled = false
        }
    }

    private fun setupFab() {
        // Asegúrate de que el ID 'fabAgregarRestaurantes' existe en fragment_home.xml
        binding.fabAgregarRestaurantes.setOnClickListener {
            restauranteRepository.poblarRestaurantesFirebase()
            Toast.makeText(requireContext(), R.string.anadiendo_restaurantes, Toast.LENGTH_SHORT).show()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(3000)
                if (isAdded) {
                    cargarRestaurantes()
                }
            }
        }
    }

    private fun cargarRestaurantes() {
        mostrarCargando(true)
        restauranteRepository.obtenerRestaurantes { listaRestaurantes ->
            activity?.runOnUiThread {
                if (isAdded && _binding != null) {
                    mostrarCargando(false)
                    if (listaRestaurantes.isNotEmpty()) {
                        restaurantes.clear()
                        restaurantes.addAll(listaRestaurantes)
                        restauranteAdapter.notifyDataSetChanged()
                    } else {
                        restaurantes.clear() // Limpia la lista si no hay resultados
                        restauranteAdapter.notifyDataSetChanged() // Notifica al adapter
                        Toast.makeText(requireContext(), R.string.no_hay_restaurantes, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        // Asegúrate de que los IDs 'progressBar' y 'recyclerViewRestaurantes' existen en fragment_home.xml
        if (_binding != null) { // Comprobación extra
            binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
            binding.recyclerViewRestaurantes.visibility = if (mostrar) View.GONE else View.VISIBLE
        }
    }

    private fun navegarADetallesRestaurante(restaurante: Restaurante) {
        Toast.makeText(requireContext(), getString(R.string.ver_detalles_de, restaurante.nombre), Toast.LENGTH_SHORT).show()
        // Aquí va tu lógica de navegación real usando findNavController()
        // Ejemplo:
        // val action = HomeFragmentDirections.actionHomeFragmentToDetalleRestauranteFragment(restaurante.id)
        // findNavController().navigate(action)
    }

    private fun navegarAReserva(restaurante: Restaurante) {
        Toast.makeText(requireContext(), getString(R.string.reservar_en, restaurante.nombre), Toast.LENGTH_SHORT).show()
        // Aquí va tu lógica de navegación real usando findNavController()
        // Ejemplo:
        // val action = HomeFragmentDirections.actionHomeFragmentToReservaFragment(restaurante.id)
        // findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Importante para evitar memory leaks
    }
}