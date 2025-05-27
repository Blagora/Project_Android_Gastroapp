package com.example.gastroapp.presentation.restaurants

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.domain.repository.RestauranteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RestaurantesUiState {
    object Loading : RestaurantesUiState()
    data class Success(val restaurantes: List<Restaurante>) : RestaurantesUiState()
    data class Error(val message: String) : RestaurantesUiState()
}

sealed class RestauranteDetailUiState {
    object Idle : RestauranteDetailUiState()
    object Loading : RestauranteDetailUiState()
    data class Success(val restaurante: Restaurante) : RestauranteDetailUiState()
    data class Error(val message: String) : RestauranteDetailUiState()
}

@HiltViewModel
class RestaurantesViewModel @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantesUiState>(RestaurantesUiState.Loading)
    val uiState: StateFlow<RestaurantesUiState> = _uiState

    private val _restauranteDetailUiState = MutableStateFlow<RestauranteDetailUiState>(RestauranteDetailUiState.Idle)
    val restauranteDetailUiState: StateFlow<RestauranteDetailUiState> = _restauranteDetailUiState.asStateFlow()

    private var initialLoadAttempted = false

    init {
        observeLocalRestaurantes()
    }

    private fun observeLocalRestaurantes() {
        viewModelScope.launch {
            restauranteRepository.getRestaurantes()
                .catch { e ->
                    Log.e("ViewModelHome", "Error observando DB local: ${e.message}", e)
                    if (_uiState.value !is RestaurantesUiState.Success) {
                        _uiState.value = RestaurantesUiState.Error("Error al acceder a datos locales: ${e.localizedMessage ?: "Error desconocido"}")
                    }
                }
                .collect { restaurantes ->
                    Log.d("ViewModelHome", "Restaurantes desde Room: ${restaurantes.size}") // LOG AÑADIDO
                    if (restaurantes.isNotEmpty()) {
                        _uiState.value = RestaurantesUiState.Success(restaurantes)
                    } else {
                        if (initialLoadAttempted) {
                            _uiState.value = RestaurantesUiState.Success(emptyList())
                        } else {
                            if (_uiState.value !is RestaurantesUiState.Error) {
                                _uiState.value = RestaurantesUiState.Loading
                            }
                        }
                    }
                }
        }
    }

    fun loadRestaurantes(forceRefresh: Boolean = false) {
        if (_uiState.value is RestaurantesUiState.Success && !forceRefresh && initialLoadAttempted) {
            Log.d("ViewModelHome", "loadRestaurantes: Datos ya presentes y no se fuerza refresh.") // LOG MODIFICADO PARA TAG CONSISTENTE
            return
        }

        if (_uiState.value !is RestaurantesUiState.Error) {
            _uiState.value = RestaurantesUiState.Loading
        }

        viewModelScope.launch {
            try {
                Log.d("ViewModelHome", "loadRestaurantes: Intentando refrescar desde el repositorio.") // LOG MODIFICADO
                restauranteRepository.refreshRestaurantes()
                initialLoadAttempted = true
                Log.d("ViewModelHome", "loadRestaurantes: Refresh completado.") // LOG MODIFICADO
            } catch (e: Exception) {
                Log.e("ViewModelHome", "loadRestaurantes: Error refrescando restaurantes desde la red.", e) // LOG MODIFICADO
                initialLoadAttempted = true

                val currentData = (_uiState.value as? RestaurantesUiState.Success)?.restaurantes
                if (currentData == null || currentData.isEmpty()) {
                    _uiState.value = RestaurantesUiState.Error("Error al obtener datos de la red: ${e.localizedMessage ?: "Error desconocido"}")
                }
            }
        }
    }

    fun loadRestauranteDetails(restauranteId: String) {
        if (restauranteId.isBlank()) {
            _restauranteDetailUiState.value = RestauranteDetailUiState.Error("ID de restaurante inválido.")
            return
        }

        _restauranteDetailUiState.value = RestauranteDetailUiState.Loading
        viewModelScope.launch {
            try {
                // Llama a la función del repositorio que devuelve el Restaurante con su menú
                val restauranteConMenu = restauranteRepository.getRestauranteConMenu(restauranteId)

                if (restauranteConMenu != null) {
                    _restauranteDetailUiState.value = RestauranteDetailUiState.Success(restauranteConMenu)
                    Log.d("RestaurantesViewModel", "Detalles de ${restauranteConMenu.nombre} cargados con ${restauranteConMenu.menu.size} platos.")
                } else {
                    _restauranteDetailUiState.value = RestauranteDetailUiState.Error("Restaurante no encontrado.")
                    Log.w("RestaurantesViewModel", "No se encontró restaurante con ID: $restauranteId")
                }
            } catch (e: Exception) {
                Log.e("RestaurantesViewModel", "Error al cargar detalles del restaurante $restauranteId", e)
                _restauranteDetailUiState.value = RestauranteDetailUiState.Error("Error al cargar detalles: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }

    fun clearRestauranteDetailState() {
        _restauranteDetailUiState.value = RestauranteDetailUiState.Idle
    }
}