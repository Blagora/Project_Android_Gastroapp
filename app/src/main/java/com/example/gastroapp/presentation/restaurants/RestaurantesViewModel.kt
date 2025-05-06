package com.example.gastroapp.presentation.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.Restaurante
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantesViewModel @Inject constructor(
    private val repository: RestauranteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantesUiState>(RestaurantesUiState.Loading)
    val uiState: StateFlow<RestaurantesUiState> = _uiState

    init {
        loadRestaurantes()
    }

    fun loadRestaurantes() {
        viewModelScope.launch {
            repository.refreshRestaurantes()
            repository.getRestaurantes()
                .catch { error ->
                    _uiState.value = RestaurantesUiState.Error(error.message ?: "Error desconocido")
                }
                .collect { restaurantes ->
                    _uiState.value = RestaurantesUiState.Success(restaurantes)
                }
        }
    }

    fun retry() {
        loadRestaurantes()
    }
}

sealed class RestaurantesUiState {
    object Loading : RestaurantesUiState()
    data class Success(val restaurantes: List<Restaurante>) : RestaurantesUiState()
    data class Error(val message: String) : RestaurantesUiState()
}