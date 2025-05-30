package com.example.gastroapp.presentation.eventos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroapp.model.Evento
import com.example.gastroapp.model.Restaurante
import com.example.gastroapp.domain.repository.EventoRepository
import com.example.gastroapp.domain.repository.RestauranteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EventoViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val restauranteRepository: RestauranteRepository
) : ViewModel() {

    private val _listUiState = MutableLiveData<EventosListUiState>(EventosListUiState.Loading)
    val listUiState: LiveData<EventosListUiState> = _listUiState

    private val _eventoConDetallesRestaurantes = MutableLiveData<Pair<Evento, List<Restaurante>>?>()
    val eventoConDetallesRestaurantes: LiveData<Pair<Evento, List<Restaurante>>?> = _eventoConDetallesRestaurantes

    private val _isLoadingDetalle = MutableLiveData<Boolean>()
    val isLoadingDetalle: LiveData<Boolean> = _isLoadingDetalle

    private val _errorDetalle = MutableLiveData<String?>()
    val errorDetalle: LiveData<String?> = _errorDetalle

    companion object {
        private const val TAG = "EventoViewModel"
    }

    init {
        cargarListaEventos() // Cargar la lista de eventos al inicializar el ViewModel
    }

    fun cargarListaEventos() {
        _listUiState.value = EventosListUiState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando carga de lista de eventos desde el repositorio...")
                val enCursoDeferred = async { eventoRepository.getEventosEnCurso() }
                val proximosDeferred = async { eventoRepository.getEventosProximos() }

                val enCurso = enCursoDeferred.await()
                val proximos = proximosDeferred.await()

                Log.d(TAG, "Eventos en curso cargados: ${enCurso.size}")
                Log.d(TAG, "Eventos próximos cargados: ${proximos.size}")

                _listUiState.value = EventosListUiState.Success(enCurso, proximos)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar lista de eventos", e)
                _listUiState.value = EventosListUiState.Error("Error al cargar eventos: ${e.message}")
            }
        }
    }

    fun cargarEventoConRestaurantes(eventoId: String) {
        _isLoadingDetalle.value = true
        _errorDetalle.value = null
        _eventoConDetallesRestaurantes.value = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando carga de evento con ID: $eventoId")
                val evento = eventoRepository.getEventoById(eventoId)

                if (evento != null) {
                    Log.d(TAG, "Evento '${evento.nombreEvento}' obtenido. IDs de restaurantes participantes: ${evento.restaurantesParticipantes}")
                    if (evento.restaurantesParticipantes.isNotEmpty()) {
                        val deferredRestaurantes = evento.restaurantesParticipantes.map { idRestaurante ->
                            Log.d(TAG, "Solicitando restaurante con ID: $idRestaurante")
                            async { restauranteRepository.getRestaurantById(idRestaurante) }
                        }
                        val restaurantes = deferredRestaurantes.awaitAll().filterNotNull()
                        Log.d(TAG, "Se obtuvieron ${restaurantes.size} objetos Restaurante participantes.")
                        _eventoConDetallesRestaurantes.postValue(Pair(evento, restaurantes))
                    } else {
                        Log.d(TAG, "El evento '${evento.nombreEvento}' no tiene restaurantes participantes listados.")
                        _eventoConDetallesRestaurantes.postValue(Pair(evento, emptyList()))
                    }
                } else {
                    Log.w(TAG, "Evento con ID: $eventoId no encontrado.")
                    _errorDetalle.postValue("Evento no encontrado.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar detalle del evento y restaurantes para evento ID: $eventoId", e)
                _errorDetalle.postValue("Error al cargar detalles: ${e.message}")
            } finally {
                _isLoadingDetalle.value = false
            }
        }
    }
}