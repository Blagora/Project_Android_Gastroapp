package com.example.gastroapp.presentation.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.databinding.FragmentEventsBinding
import com.example.gastroapp.presentation.events.adapters.EventAdapter
import com.example.gastroapp.presentation.events.models.Event
import com.example.gastroapp.presentation.events.models.EventsData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Configurar RecyclerView para eventos actuales
        val currentEventsAdapter = EventAdapter(EventsData.currentEvents) { event ->
            onEventClicked(event)
        }
        
        _binding?.currentEventsRecyclerView?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = currentEventsAdapter
        }

        // Configurar RecyclerView para próximos eventos
        val upcomingEventsAdapter = EventAdapter(EventsData.upcomingEvents) { event ->
            onEventClicked(event)
        }
        
        _binding?.upcomingEventsRecyclerView?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = upcomingEventsAdapter
        }
    }

    private fun setupListeners() {
        _binding?.searchText?.setOnClickListener {
            // Abrir búsqueda de eventos
            Toast.makeText(requireContext(), "Búsqueda de eventos", Toast.LENGTH_SHORT).show()
        }

        _binding?.menuIcon?.setOnClickListener {
            // Abrir filtros
            Toast.makeText(requireContext(), "Filtros de eventos", Toast.LENGTH_SHORT).show()
        }

        _binding?.txtViewMore1?.setOnClickListener {
            // Ver más eventos en curso
            Toast.makeText(requireContext(), "Ver más eventos en curso", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentEvents.observe(viewLifecycleOwner) { events ->
            // Actualizar con datos del ViewModel si es necesario
        }

        viewModel.upcomingEvents.observe(viewLifecycleOwner) { events ->
            // Actualizar con datos del ViewModel si es necesario
        }
    }

    private fun onEventClicked(event: Event) {
        // Navegar a los detalles del evento
        Toast.makeText(
            requireContext(), 
            "Evento seleccionado: ${event.title}", 
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 