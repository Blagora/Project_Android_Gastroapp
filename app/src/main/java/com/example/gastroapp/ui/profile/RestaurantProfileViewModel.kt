package com.example.gastroapp.ui.profile

import androidx.lifecycle.*
import com.example.gastroapp.domain.repository.RestauranteRepository
import com.example.gastroapp.model.Restaurante
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log // Asegúrate de tener el import de Log si no está

sealed class RestaurantProfileUiState {
    object Loading : RestaurantProfileUiState()
    data class Success(val restaurante: Restaurante) : RestaurantProfileUiState()
    data class Error(val message: String) : RestaurantProfileUiState()
    object NotFound : RestaurantProfileUiState()
}

@HiltViewModel
class RestaurantProfileViewModel @Inject constructor(
    private val restauranteRepository: RestauranteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantProfileUiState>(RestaurantProfileUiState.Loading)
    val uiState: StateFlow<RestaurantProfileUiState> = _uiState

    private val restaurantId: String? = savedStateHandle["restaurantId"]

    init {
        loadRestaurantDetails()
        loadRestaurantDetailsWithMenu()
    }
    fun loadRestaurantDetailsWithMenu() {
        if (restaurantId == null) {
            Log.e("ViewModelProfile", "Error: ID de restaurante es null en SavedStateHandle.")
            _uiState.value = RestaurantProfileUiState.Error("ID de restaurante no proporcionado.")
            return
        }

        _uiState.value = RestaurantProfileUiState.Loading
        Log.d("ViewModelProfile", "Cargando detalles CON MENÚ para ID: $restaurantId")
        viewModelScope.launch {
            // Llama a la función del repositorio que obtiene el restaurante Y su menú
            val restauranteConSuMenu = restauranteRepository.getRestauranteConMenu(restaurantId)

            if (restauranteConSuMenu != null) {
                Log.d("ViewModelProfile", "Restaurante con menú encontrado: ${restauranteConSuMenu.nombre}")
                // El objeto 'restauranteConSuMenu' ya debería tener su lista 'menu' poblada
                // gracias a la lógica en RestauranteRepository.getRestauranteConMenu()
                Log.d("ViewModelProfile", "Menú: ${restauranteConSuMenu.menu.joinToString { it.nombrePlato }}")
                _uiState.value = RestaurantProfileUiState.Success(restauranteConSuMenu)
            } else {
                Log.w("ViewModelProfile", "Restaurante con menú no encontrado para ID: $restaurantId")
                _uiState.value = RestaurantProfileUiState.NotFound
            }
        }
    }
    private fun loadRestaurantDetails() {
        if (restaurantId == null) {
            Log.e("ViewModelProfile", "Error: ID de restaurante es null.") // Añadido Log
            _uiState.value = RestaurantProfileUiState.Error("ID de restaurante no proporcionado.")
            return
        }

        _uiState.value = RestaurantProfileUiState.Loading
        Log.d("ViewModelProfile", "Cargando detalles para ID: $restaurantId") // Añadido Log
        viewModelScope.launch {
            // Usa !! porque ya hemos comprobado que no es null arriba
            val restaurante = restauranteRepository.getRestaurantById(restaurantId!!) // <-- CORRECCIÓN AQUÍ
            if (restaurante != null) {
                Log.d("ViewModelProfile", "Restaurante encontrado: ${restaurante.nombre}") // Añadido Log
                _uiState.value = RestaurantProfileUiState.Success(restaurante)
            } else {
                Log.w("ViewModelProfile", "Restaurante no encontrado en repositorio para ID: $restaurantId") // Añadido Log
                _uiState.value = RestaurantProfileUiState.NotFound
            }
        }
    }

    fun toggleFavorite() { }
}