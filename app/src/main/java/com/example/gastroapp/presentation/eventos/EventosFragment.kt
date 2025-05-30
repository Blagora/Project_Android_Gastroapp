package com.example.gastroapp.presentation.eventos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.adapter.EventoAdapter
import com.example.gastroapp.databinding.FragmentEventsBinding
import com.example.gastroapp.model.Evento
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventosFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val eventoViewModel: EventoViewModel by viewModels()

    private lateinit var eventosEnCursoAdapter: EventoAdapter
    private lateinit var eventosProximosAdapter: EventoAdapter

    companion object {
        private const val TAG = "EventosFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false) // USA EL NOMBRE CORRECTO
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()

        binding.searchViewEventos?.queryHint = "Buscar eventos..."

        eventoViewModel.cargarListaEventos()
    }

    private fun setupRecyclerViews() {
        eventosEnCursoAdapter = EventoAdapter { evento ->
            navigateToDetail(evento)
        }
        binding.recyclerViewEventosEnCurso?.apply { // Ejemplo de ID
            layoutManager = LinearLayoutManager(context)
            adapter = eventosEnCursoAdapter
            isNestedScrollingEnabled = false
        }

        eventosProximosAdapter = EventoAdapter { evento ->
            navigateToDetail(evento)
        }
        binding.recyclerViewEventosProximos?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventosProximosAdapter
            isNestedScrollingEnabled = false
        }
    }

        private fun navigateToDetail(evento: Evento) {
            Log.d(TAG, "Navegando a detalle del evento con ID: ${evento.id}, Nombre: ${evento.nombreEvento}") // <-- AÑADE ESTE LOG
            val action = EventosFragmentDirections.actionEventosFragmentToDetalleEventoFragment(evento.id)
            findNavController().navigate(action)
        }

    private fun observeViewModel() {
        eventoViewModel.listUiState.observe(viewLifecycleOwner) { state -> // Usar listUiState
            when (state) {
                is EventosListUiState.Loading -> { // Usar EventosListUiState.Loading
                    binding.progressBarEventos?.visibility = View.VISIBLE
                    Log.d(TAG, "Cargando eventos...")
                }
                is EventosListUiState.Success -> { // Usar EventosListUiState.Success
                    binding.progressBarEventos?.visibility = View.GONE
                    Log.d(TAG, "Eventos cargados. En curso: ${state.enCurso.size}, Próximos: ${state.proximos.size}")

                    binding.textViewEnCursoLabel?.visibility = if (state.enCurso.isNotEmpty()) View.VISIBLE else View.GONE
                    eventosEnCursoAdapter.submitList(state.enCurso)

                    binding.textViewProximosLabel?.visibility = if (state.proximos.isNotEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerViewEventosProximos?.visibility = if (state.proximos.isNotEmpty()) View.VISIBLE else View.GONE
                    eventosProximosAdapter.submitList(state.proximos)
                }
                is EventosListUiState.Error -> { // Usar EventosListUiState.Error
                    binding.progressBarEventos?.visibility = View.GONE
                    Log.e(TAG, "Error: ${state.message}")
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                // No necesitas 'else' aquí porque EventosListUiState es una sealed class y has cubierto todos los casos.
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewEventosEnCurso?.adapter = null
        binding.recyclerViewEventosProximos?.adapter = null
        _binding = null
    }
}