package com.example.gastroapp.ui.login

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gastroapp.databinding.ActivityLoginBinding
import com.example.gastroapp.ui.registro.RegistroActivity
import com.example.gastroapp.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Agregar subrayado a los enlaces
        binding.registerLink.paintFlags = binding.registerLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.forgotPasswordLink.paintFlags = binding.forgotPasswordLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.forgotPasswordLink.setOnClickListener {
            showMessage("Funcionalidad en desarrollo")
        }
    }

    private fun validateAndLogin() {
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        when {
            username.isEmpty() -> showError("Por favor ingrese su nombre")
            password.isEmpty() -> showError("Por favor ingrese su contraseña")
            else -> {
                // Permitir acceso con cualquier credencial
                loginSuccess()
            }
        }
    }

    private fun loginSuccess() {
        // Navegar a la pantalla principal
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Cerrar la pantalla de login para que no se pueda volver atrás
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 