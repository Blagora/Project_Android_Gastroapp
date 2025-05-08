package com.example.gastroapp.data.remote

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.gastroapp.util.DataLoaderUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoadDataActivity : AppCompatActivity() {
    private val TAG = "LoadDataActivity"
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Iniciando LoadDataActivity")
        Toast.makeText(this, "Iniciando carga de datos...", Toast.LENGTH_SHORT).show()
        DataLoaderUtil.launchDataLoader(this)
        verifyFirebaseConnection()
    }

    private fun verifyFirebaseConnection() {
        Log.d(TAG, "Verificando conexión a Firebase...")

        db.collection("test").document("test_connection")
            .set(mapOf("timestamp" to System.currentTimeMillis()))
            .addOnSuccessListener {
                Log.d(TAG, "Conexión a Firebase verificada con éxito")
                startDataLoading()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al conectar con Firebase", e)
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                dumpFirebaseErrors(e)
                finish()
            }
    }

    private fun startDataLoading() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando carga de restaurantes...")
                val restaurantesCollection = db.collection("restaurantes")

                // Verificar si ya existen restaurantes para evitar duplicados
                val existingDocs = restaurantesCollection.limit(1).get().await()
                if (!existingDocs.isEmpty) {
                    Log.d(TAG, "Ya existen restaurantes. Verificando si es necesario recargar...")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoadDataActivity,
                            "Ya existen restaurantes en la base de datos.",
                            Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@launch
                }

                // Cargar datos
                val success = FirebaseRestaurantRepository.loadSampleData()

                withContext(Dispatchers.Main) {
                    if (success > 0) {
                        Toast.makeText(this@LoadDataActivity,
                            "¡Datos cargados exitosamente! ($success restaurantes)",
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoadDataActivity,
                            "No se pudieron cargar los datos (0 restaurantes)",
                            Toast.LENGTH_LONG).show()
                    }
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar los datos", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoadDataActivity,
                        "Error al cargar datos: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    dumpFirebaseErrors(e)
                    finish()
                }
            }
        }
    }

    private fun dumpFirebaseErrors(e: Exception) {
        Log.e(TAG, "------------------------")
        Log.e(TAG, "DETALLES DEL ERROR:")
        Log.e(TAG, "Mensaje: ${e.message}")
        Log.e(TAG, "Causa: ${e.cause}")
        Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
        e.printStackTrace()
        Log.e(TAG, "------------------------")

        // Verificar reglas de seguridad
        db.collection("restaurantes").document("test")
            .delete()
            .addOnFailureListener { deleteError ->
                Log.e(TAG, "Error al intentar eliminar documento de prueba: ${deleteError.message}")
                Log.e(TAG, "Posible problema con reglas de seguridad de Firestore")
            }
    }


}
