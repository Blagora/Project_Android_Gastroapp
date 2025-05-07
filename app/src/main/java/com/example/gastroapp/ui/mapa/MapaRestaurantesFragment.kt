package com.example.gastroapp.ui.mapa

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
import androidx.lifecycle.lifecycleScope
import com.example.gastroapp.data.remote.OverpassService
import com.example.gastroapp.databinding.FragmentMapaRestaurantesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import javax.inject.Inject

@AndroidEntryPoint
class MapaRestaurantesFragment : Fragment() {
    private var _binding: FragmentMapaRestaurantesBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1

    @Inject
    lateinit var overpassService: OverpassService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapaRestaurantesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupLocationClient()
        setupListeners()
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true)
        
        val mapController = map.controller
        mapController.setZoom(15.0)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setupListeners() {
        binding.btnLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    map.controller.animateTo(geoPoint)
                    showConfirmationDialog { confirmed ->
                        if (confirmed) {
                            findRestaurants(it.latitude, it.longitude)
                        }
                    }
                }
            }
        }
    }

    private fun findRestaurants(lat: Double, lon: Double) {
        val bbox = "${lat - 0.01},${lon - 0.01},${lat + 0.01},${lon + 0.01}"
        
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Toast.makeText(requireContext(), "Buscando restaurantes cercanos...", Toast.LENGTH_SHORT).show()
                
                val response = withContext(Dispatchers.IO) {
                    overpassService.buscarRestaurantes(bbox)
                }
                
                processRestaurants(response)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al buscar restaurantes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processRestaurants(response: String) {
        try {
            val json = JSONObject(response)
            val elements = json.getJSONArray("elements")
            var restaurantesEncontrados = 0
            map.overlays.clear()

            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                if (element.has("lat") && element.has("lon") && element.has("tags")) {
                    val lat = element.getDouble("lat")
                    val lon = element.getDouble("lon")
                    val tags = element.getJSONObject("tags")
                    val name = tags.optString("name", "Restaurante sin nombre")
                    val cuisine = tags.optString("cuisine", "")
                    
                    val marker = Marker(map)
                    marker.position = GeoPoint(lat, lon)
                    marker.title = name
                    marker.snippet = if (cuisine.isNotEmpty()) {
                        "Tipo de cocina: $cuisine"
                    } else {
                        "Restaurante"
                    }
                    
                    map.overlays.add(marker)
                    restaurantesEncontrados++
                }
            }
            
            map.invalidate()
            
            val mensaje = when {
                restaurantesEncontrados > 0 -> "Se encontraron $restaurantesEncontrados restaurantes"
                else -> "No se encontraron restaurantes en esta área"
            }
            
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al procesar datos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(onResult: (Boolean) -> Unit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Buscar restaurantes cercanos")
            .setMessage("¿Desea buscar restaurantes cerca de su ubicación actual?")
            .setPositiveButton("Sí") { _, _ -> onResult(true) }
            .setNegativeButton("No") { _, _ -> onResult(false) }
            .show()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(
                        context,
                        "Se requiere permiso de ubicación para mostrar restaurantes cercanos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}