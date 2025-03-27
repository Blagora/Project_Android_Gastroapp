package com.example.gastroapp.presentation.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gastroapp.databinding.FragmentEventsBinding
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
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // TODO: Configurar RecyclerView de eventos
        // TODO: Configurar calendario para filtrar por fecha
        // TODO: Configurar filtros por tipo de evento
    }

    private fun observeViewModel() {
        // TODO: Observar lista de eventos
        // TODO: Observar estado de carga
        // TODO: Observar errores
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 