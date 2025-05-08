package com.example.gastroapp.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.gastroapp.data.remote.LoadDataActivity

object DataLoaderUtil {
    fun launchDataLoader(context: Context) {
        try {
            val intent = Intent(context, LoadDataActivity::class.java)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al iniciar la carga de datos: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }
}