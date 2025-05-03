package com.example.gastroapp.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gastroapp.R
import com.example.gastroapp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Intent
import com.example.gastroapp.ui.ListaRestaurantesActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configurar la barra de acción con la navegación
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_restaurants,
                R.id.navigation_events,
                R.id.navigation_reservations,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar el BottomNavigationView con la navegación
        binding.bottomNavigationView.setupWithNavController(navController)

        FirebaseApp.initializeApp(this)

        val db = Firebase.firestore

        db.collection("restaurantes")
            .get()
            .addOnSuccessListener { result ->
                Log.d("FirestoreTest", "Documentos encontrados: ${result.size()}")
                for (document in result) {
                    Log.d("FirestoreTest", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreTest", "Error al leer documentos", exception)
            }
    }
} 