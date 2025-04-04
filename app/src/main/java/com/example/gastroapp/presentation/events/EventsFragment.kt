package com.example.gastroapp.presentation.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastroapp.R
import com.example.gastroapp.databinding.FragmentEventsBinding
import com.example.gastroapp.presentation.events.adapters.EventAdapter
import com.example.gastroapp.presentation.events.models.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()
    
    private val LOCATION_PERMISSION_REQUEST = 1001
    private lateinit var currentEventsAdapter: EventAdapter
    private lateinit var upcomingEventsAdapter: EventAdapter

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
        setupClickListeners()
        checkLocationPermission()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        currentEventsAdapter = EventAdapter { event -> onEventClick(event) }
        upcomingEventsAdapter = EventAdapter { event -> onEventClick(event) }

        binding.currentEventsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = currentEventsAdapter
        }

        binding.upcomingEventsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = upcomingEventsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.searchEditText?.setOnClickListener {
            // Implementar búsqueda
            Toast.makeText(context, "Búsqueda de eventos", Toast.LENGTH_SHORT).show()
        }

        binding.filterIcon?.setOnClickListener {
            // Implementar filtros
            Toast.makeText(context, "Filtrar eventos", Toast.LENGTH_SHORT).show()
        }

        binding.notificationIcon?.setOnClickListener {
            // Implementar notificaciones
            Toast.makeText(context, "Notificaciones", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            // Ya tenemos permiso, obtener ubicación
            viewModel.getCurrentLocation()
        }
    }

    private fun observeViewModel() {
        viewModel.currentEvents.observe(viewLifecycleOwner) { events ->
            currentEventsAdapter.submitList(events)
        }

        viewModel.upcomingEvents.observe(viewLifecycleOwner) { events ->
            upcomingEventsAdapter.submitList(events)
        }
    }

    private fun onEventClick(event: Event) {
        // Implementar navegación al detalle del evento
        Toast.makeText(context, "Evento seleccionado: ${event.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.getCurrentLocation()
                } else {
                    Toast.makeText(
                        context,
                        "Se requiere permiso de ubicación para mostrar eventos cercanos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 