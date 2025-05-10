package com.example.gastroapp

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class GastroApp : Application(), ProviderInstallListener {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        checkAndInstallGooglePlayServices()
        setupFirestoreReconnect()
        FirebaseFirestore.setLoggingEnabled(true)
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            // Habilitar logs solo en debug
            if (BuildConfig.DEBUG) {
                FirebaseFirestore.setLoggingEnabled(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAndInstallGooglePlayServices() {
        // Verificar Google Play Services en el hilo principal
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(this)

        if (resultCode == ConnectionResult.SUCCESS) {
            // Si Google Play Services está disponible, proceder con la instalación del proveedor
            applicationScope.launch(Dispatchers.Main) {
                try {
                    ProviderInstaller.installIfNeededAsync(this@GastroApp, this@GastroApp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (availability.isUserResolvableError(resultCode)) {
            // Si hay un error que el usuario puede resolver, mostrar el diálogo
            availability.showErrorNotification(this, resultCode)
        } else {
            // Error no resoluble
            Toast.makeText(
                this,
                "Esta aplicación requiere Google Play Services",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupFirestoreReconnect() {
        FirebaseFirestore.getInstance().addSnapshotsInSyncListener {
            Toast.makeText(
                this,
                "Firestore reconectado exitosamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onProviderInstalled() {
        // Provider instalado correctamente
        applicationScope.launch(Dispatchers.Main) {
            Toast.makeText(
                this@GastroApp,
                "Servicios de Google Play actualizados correctamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onProviderInstallFailed(errorCode: Int, intent: Intent?) {
        val availability = GoogleApiAvailability.getInstance()
        if (availability.isUserResolvableError(errorCode)) {
            availability.showErrorNotification(this@GastroApp, errorCode)
        } else {
            Toast.makeText(
                this,
                "Error crítico: Google Play Services no está disponible.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
