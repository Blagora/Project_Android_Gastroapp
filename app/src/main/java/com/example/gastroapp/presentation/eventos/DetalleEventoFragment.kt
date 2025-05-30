package com.example.gastroapp.presentation.eventos

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.gastroapp.R
import com.example.gastroapp.databinding.FragmentDetalleEventoBinding
import com.example.gastroapp.model.Evento
import com.example.gastroapp.presentation.eventos.DetalleEventoFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class DetalleEventoFragment : Fragment() {

    private var _binding: FragmentDetalleEventoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventoViewModel by viewModels()
    private val args: DetalleEventoFragmentArgs by navArgs()

    companion object {
        private const val TAG = "DetalleEventoFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventoId = args.eventoId
        Log.d(TAG, "Cargando detalle para evento ID: $eventoId")

        if (eventoId.isNotEmpty()) {
            viewModel.cargarEventoConRestaurantes(eventoId)
        } else {
            Log.e(TAG, "eventoId está vacío.")
            Toast.makeText(context, "Error: No se especificó el evento.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack() // Regresar si no hay ID
        }
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manejar el clic en el botón de "atrás" de la Toolbar
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun observeViewModel() {

        viewModel.eventoConDetallesRestaurantes.observe(viewLifecycleOwner) { pair ->
            // pair es de tipo Pair<Evento, List<Restaurante>>?
            binding.progressBarDetalleEvento.visibility = View.GONE
            val evento = pair?.first // Extraer el objeto Evento del Pair
            if (evento != null) {
                Log.d(TAG, "Evento recibido: ${evento.nombreEvento}")
                populateUi(evento) // populateUi espera un Evento
            } else {
                // Esto se activa si pair es null o pair.first es null
                Log.w(TAG, "Evento no encontrado o error al cargar.")
                Toast.makeText(context, "No se pudo cargar la información del evento.", Toast.LENGTH_LONG).show()
            }
        }
        viewModel.isLoadingDetalle.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetalleEvento.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.errorDetalle.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                // Opcionalmente, navegar hacia atrás
                // findNavController().popBackStack()
            }
        }
    }

    private fun populateUi(evento: Evento) {
        // (activity as? AppCompatActivity)?.supportActionBar?.title = evento.nombreEvento // Actualizar título de la Toolbar

        binding.textViewDetalleNombreEvento.text = evento.nombreEvento
        binding.textViewDetalleDescripcion.text = evento.descripcionEvento

        // Formateo de fechas
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "CO"))
        dateFormat.timeZone = TimeZone.getDefault() // O la zona horaria específica de los timestamps

        val fechaInicioStr = evento.fechaInicio?.toDate()?.let { dateFormat.format(it) } ?: "Fecha no especificada"
        val fechaFinStr = evento.fechaFin?.toDate()?.let { dateFormat.format(it) } ?: "Fecha no especificada"

        if (evento.fechaInicio != null && evento.fechaFin != null && evento.fechaInicio == evento.fechaFin) {
            binding.textViewDetalleFechas.text = "Fecha: $fechaInicioStr"
        } else if (evento.fechaInicio != null && evento.fechaFin != null) {
            binding.textViewDetalleFechas.text = "Del $fechaInicioStr\nhasta $fechaFinStr"
        } else if (evento.fechaInicio != null) {
            binding.textViewDetalleFechas.text = "Inicia: $fechaInicioStr"
        } else {
            binding.textViewDetalleFechas.text = "Fechas no disponibles"
        }


        // Formateo del precio (asumiendo que es String "19.000" o similar)
        if (evento.precioEvento.isNotEmpty() && evento.precioEvento.lowercase(Locale.ROOT) != "0" && evento.precioEvento.lowercase(Locale.ROOT) != "gratis") {
            binding.textViewDetallePrecio.text = "Precio: ${evento.precioEvento} COP"
            binding.textViewDetallePrecio.visibility = View.VISIBLE
        } else if (evento.precioEvento.lowercase(Locale.ROOT) == "gratis") {
            binding.textViewDetallePrecio.text = "Precio: Gratis"
            binding.textViewDetallePrecio.visibility = View.VISIBLE
        }
        else {
            binding.textViewDetallePrecio.visibility = View.GONE // Ocultar si es 0 o vacío
        }

        Glide.with(this)
            .load(evento.imagenUrl.ifEmpty { null }) // Carga null si la URL está vacía
            .placeholder(R.drawable.placeholder_restaurante)
            .error(R.drawable.placeholder_restaurante)
            .centerCrop()
            .into(binding.imageViewDetalleEventoHeader)

        if (!evento.linkInformacion.isNullOrEmpty()) {
            binding.buttonMasInformacion.visibility = View.VISIBLE
            binding.buttonMasInformacion.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(evento.linkInformacion))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "No se encontró aplicación para abrir el enlace: ${evento.linkInformacion}", e)
                    Toast.makeText(context, "No se pudo abrir el enlace. No hay aplicación disponible.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error al intentar abrir el enlace: ${evento.linkInformacion}", e)
                    Toast.makeText(context, "Error al abrir el enlace.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.buttonMasInformacion.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}