package com.example.gastroapp.presentation.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gastroapp.databinding.FragmentRestaurantsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantsFragment : Fragment() {

    private var _binding: FragmentRestaurantsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RestaurantsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // TODO: Configurar RecyclerView
        // TODO: Configurar SearchView
        // TODO: Configurar filtros
    }

    private fun observeViewModel() {
        // TODO: Observar lista de restaurantes
        // TODO: Observar estado de carga
        // TODO: Observar errores
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 