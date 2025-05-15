package com.example.gastroapp.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gastroapp.R
import com.example.gastroapp.databinding.FragmentRestaurantProfileBinding
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class RestaurantProfileFragment : Fragment() {

    private var _binding: FragmentRestaurantProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RestaurantProfileViewModel by viewModels()
    private val args: RestaurantProfileFragmentArgs by navArgs()

    private lateinit var galleryAdapter: GalleryAdapter
    private var currentRestaurante: Restaurante? = null

    companion object {
        private const val TAG = "RestaurantProfileFrag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentRestaurantProfileBinding.inflate(inflater, container, false)
        setupToolbar()
        // Habilitar manejo de menú para el botón de atrás de la toolbar
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated - Restaurant ID: ${args.restaurantId}")

        setupGalleryRecyclerView()
        setupMapButton() // Configura el listener del botón de mapa
        observeUiState()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.collapsingToolbar.title = " "
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupGalleryRecyclerView() {
        galleryAdapter = GalleryAdapter(emptyList())
        binding.galleryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = galleryAdapter
        }
        Log.d(TAG, "Galería RecyclerView configurada")
    }

    private fun setupMapButton() {
        binding.btnOpenMap.setOnClickListener {
            currentRestaurante?.ubicacion?.let { geoPoint ->
                openExternalMap(geoPoint.latitude, geoPoint.longitude, currentRestaurante?.nombre ?: "Restaurante")
            } ?: run {
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Intento de abrir mapa sin ubicación válida.")
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Log.d(TAG, "Nuevo estado de perfil recibido: ${state::class.java.simpleName}")
                    binding.progressBarProfile?.visibility = View.GONE // Usa el ID que le diste al ProgressBar
                    when (state) {
                        is RestaurantProfileUiState.Loading -> {
                            Log.d(TAG, "Cargando perfil...")
                            binding.progressBarProfile?.visibility = View.VISIBLE
                        }
                        is RestaurantProfileUiState.Success -> {
                            Log.d(TAG, "Perfil cargado: ${state.restaurante.nombre}")
                            currentRestaurante = state.restaurante // Guarda el restaurante
                            populateUi(state.restaurante)
                            // Ya no se llama a updateMapLocation
                        }
                        is RestaurantProfileUiState.Error -> {
                            Log.e(TAG, "Error cargando perfil: ${state.message}")
                            Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                        is RestaurantProfileUiState.NotFound -> {
                            Log.w(TAG, "Restaurante no encontrado con ID: ${args.restaurantId}")
                            Toast.makeText(context, "Restaurante no encontrado", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun populateUi(restaurante: Restaurante) {
        binding.collapsingToolbar.title = restaurante.nombre
        binding.txtNombre.text = restaurante.nombre
        binding.txtCategoria.text = restaurante.categorias.joinToString(", ")
        binding.ratingBarIndicator.rating = restaurante.calificacionPromedio
        binding.txtRatingValue.text = String.format(Locale.US, "(%.1f)", restaurante.calificacionPromedio)
        binding.txtDescripcion.text = restaurante.descripcion
        binding.txtDireccion.text = restaurante.direccion

        val horarioHoy = restaurante.horario[getCurrentDay()]
        if (horarioHoy != null && horarioHoy.inicio.isNotEmpty() && horarioHoy.fin.isNotEmpty()) {
            binding.txtHorarioHoy.text = getString(R.string.horario_abierto_hoy, horarioHoy.inicio, horarioHoy.fin)
        } else {
            binding.txtHorarioHoy.text = getString(R.string.horario_cerrado_hoy)
        }

        if (restaurante.galeriaImagenes.isNotEmpty()) {
            Glide.with(this)
                .load(restaurante.galeriaImagenes[0])
                .placeholder(R.drawable.placeholder_restaurante)
                .error(R.drawable.placeholder_restaurante)
                .into(binding.imgRestauranteHeader)
            galleryAdapter.updateData(restaurante.galeriaImagenes)
        } else {
            binding.imgRestauranteHeader.setImageResource(R.drawable.placeholder_restaurante)
            galleryAdapter.updateData(emptyList())
        }

        binding.btnOpenMap.isEnabled = restaurante.ubicacion != null

        binding.btnReservarProfile.setOnClickListener {
            Toast.makeText(context, "Reservar en ${restaurante.nombre}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "lunes"
            Calendar.TUESDAY -> "martes"
            Calendar.WEDNESDAY -> "miercoles"
            Calendar.THURSDAY -> "jueves"
            Calendar.FRIDAY -> "viernes"
            Calendar.SATURDAY -> "sabado"
            Calendar.SUNDAY -> "domingo"
            else -> "lunes"
        }
    }
    private fun openExternalMap(latitude: Double, longitude: Double, label: String) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            Log.d(TAG, "Abriendo mapa externo para: $label en $latitude,$longitude")
            val chooser = Intent.createChooser(mapIntent, "Abrir ubicación con")
            startActivity(chooser)
        } else {
            Log.w(TAG, "No se encontró aplicación de mapas para abrir la ubicación.")
            Toast.makeText(context, "No se encontró aplicación de mapas", Toast.LENGTH_LONG).show()
        }
    }


    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        _binding = null
        super.onDestroyView()
    }

    class GalleryAdapter(private var images: List<String>) : RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
            return ImageViewHolder(view)
        }
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.bind(images[position])
        }
        override fun getItemCount(): Int = images.size
        fun updateData(newImages: List<String>) {
            images = newImages
            notifyDataSetChanged()
        }
        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.galleryImageView)
            fun bind(imageUrl: String) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_restaurante)
                    .error(R.drawable.placeholder_restaurante)
                    .centerCrop()
                    .into(imageView)
            }
        }
    }
}