package com.example.gastroapp.ui.mapa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
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

    companion object {
        private const val BOGOTA_LAT = 4.6097
        private const val BOGOTA_LON = -74.0817
        private const val DEFAULT_ZOOM = 15.0
        private const val MIN_ZOOM = 11.0
        private const val MAX_ZOOM = 19.0
        private const val BOGOTA_BOUNDS_N = 4.836
        private const val BOGOTA_BOUNDS_S = 4.471
        private const val BOGOTA_BOUNDS_E = -74.009
        private const val BOGOTA_BOUNDS_W = -74.217
    }

    private lateinit var currentLocationMarker: Marker
    private lateinit var cacheManager: CacheManager

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
        // Configuración inicial de OSMDroid
        Configuration.getInstance().apply {
            userAgentValue = requireContext().packageName
            osmdroidTileCache = File(requireContext().cacheDir, "tiles").apply {
                if (!exists()) mkdirs()
            }
            osmdroidBasePath = requireContext().cacheDir
            // Aumentar el caché y optimizar la configuración
            tileFileSystemCacheMaxBytes = 1024L * 1024L * 1024L // 1GB de caché
            tileDownloadThreads = 12 // Más hilos para descarga paralela
        }

        map = binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            setUseDataConnection(true)
        }
        
        // Configurar límites de scroll para Bogotá
        val boundingBox = BoundingBox(
            BOGOTA_BOUNDS_N + 0.1,
            BOGOTA_BOUNDS_E + 0.1,
            BOGOTA_BOUNDS_S - 0.1,
            BOGOTA_BOUNDS_W - 0.1
        )
        
        try {
            map.setScrollableAreaLimitDouble(boundingBox)
            map.minZoomLevel = MIN_ZOOM
            map.maxZoomLevel = MAX_ZOOM

            // Centrar el mapa en Bogotá
            val bogotaPoint = GeoPoint(BOGOTA_LAT, BOGOTA_LON)
            map.controller.apply {
                setZoom(DEFAULT_ZOOM)
                setCenter(bogotaPoint)
            }

            // Inicializar el CacheManager
            cacheManager = CacheManager(map)
            
            // Pre-descargar tiles de Bogotá en segundo plano
            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error al pre-cargar mapa: ${throwable.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) {
                preloadBogotaTiles()
            }
        } catch (e: Exception) {
            Log.e("MapaRestaurantesFragment", "Error al configurar el mapa", e)
            Toast.makeText(
                requireContext(),
                "Error al configurar el mapa: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }

        // Configurar marcador de ubicación actual
        setupLocationMarker()
    }

    private fun setupLocationMarker() {
        currentLocationMarker = Marker(map).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_mylocation)
            title = "Mi ubicación"
            setVisible(false)
            map.overlays.add(this)
        }
    }

    private suspend fun preloadBogotaTiles() {
        try {
            val initialBoundingBox = BoundingBox(
                BOGOTA_LAT + 0.05,
                BOGOTA_LON + 0.05,
                BOGOTA_LAT - 0.05,
                BOGOTA_LON - 0.05
            )
            
            val priorityZoomLevels = listOf(14, 15, 16)
            for (zoom in priorityZoomLevels) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Precargando mapa ${(zoom - priorityZoomLevels.first() + 1) * 33}%",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                withContext(Dispatchers.Default) {
                    try {
                        downloadTilesWithoutDialog(initialBoundingBox, zoom)
                    } catch (e: Exception) {
                        Log.e("MapaRestaurantesFragment", "Error al precargar tiles del zoom $zoom", e)
                    }
                }
            }

            // Cargar el área completa en segundo plano
            val fullBoundingBox = BoundingBox(
                BOGOTA_BOUNDS_N,
                BOGOTA_BOUNDS_E,
                BOGOTA_BOUNDS_S,
                BOGOTA_BOUNDS_W
            )
            
            withContext(Dispatchers.Default) {
                for (zoom in 12..13) {
                    try {
                        downloadTilesWithoutDialog(fullBoundingBox, zoom)
                    } catch (e: Exception) {
                        Log.e("MapaRestaurantesFragment", "Error al precargar tiles completos del zoom $zoom", e)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Error al pre-cargar mapa: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("MapaRestaurantesFragment", "Error al precargar tiles", e)
            throw e
        }
    }

    private suspend fun downloadTilesWithoutDialog(boundingBox: BoundingBox, zoom: Int) {
        withContext(Dispatchers.IO) {
            try {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val callback = object : CacheManager.CacheManagerCallback {
                        override fun onTaskComplete() {
                            continuation.resume(Unit) {}
                        }

                        override fun onTaskFailed(errors: Int) {
                            continuation.resume(Unit) {}
                            Log.e("MapaRestaurantesFragment", "Error al descargar tiles: $errors errores")
                        }

                        override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                            Log.d("MapaRestaurantesFragment", "Progreso: $progress% zoom: $currentZoomLevel")
                        }

                        override fun downloadStarted() {
                            Log.d("MapaRestaurantesFragment", "Iniciando descarga de zoom $zoom")
                        }

                        override fun setPossibleTilesInArea(total: Int) {
                            Log.d("MapaRestaurantesFragment", "Tiles totales para zoom $zoom: $total")
                        }
                    }

                    cacheManager.downloadAreaAsync(
                        requireContext(),
                        boundingBox,
                        zoom,
                        zoom,
                        callback
                    )
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Caché completado para zoom $zoom",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("MapaRestaurantesFragment", "Error al descargar tiles para zoom $zoom", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error al descargar mapa para zoom $zoom",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
                    updateCurrentLocationMarker(it.latitude, it.longitude)
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

    private fun updateCurrentLocationMarker(latitude: Double, longitude: Double) {
        val position = GeoPoint(latitude, longitude)
        currentLocationMarker.position = position
        currentLocationMarker.setVisible(true)
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        // Verificar y actualizar caché si es necesario
        lifecycleScope.launch(Dispatchers.IO) {
            checkAndUpdateCache()
        }
    }

    private suspend fun checkAndUpdateCache() {
        withContext(Dispatchers.IO) {
            if (cacheManager.currentCacheUsage() < cacheManager.cacheCapacity() * 0.5) {
                // Si el caché está por debajo del 50%, actualizar tiles
                preloadBogotaTiles()
            }
        }
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