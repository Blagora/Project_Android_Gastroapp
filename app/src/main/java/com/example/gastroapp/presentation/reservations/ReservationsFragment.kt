package com.example.gastroapp.presentation.reservations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gastroapp.databinding.FragmentReservationsBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationsFragment : Fragment() {

    private var _binding: FragmentReservationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // TODO: Configurar ViewPager2 con TabLayout
        // TODO: Configurar FAB para nueva reserva
    }

    private fun observeViewModel() {
        // TODO: Observar lista de reservas activas
        // TODO: Observar historial de reservas
        // TODO: Observar estado de carga
        // TODO: Observar errores
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 