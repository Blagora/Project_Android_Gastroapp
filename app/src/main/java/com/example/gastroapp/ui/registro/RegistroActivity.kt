package com.example.gastroapp.ui.registro

import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gastroapp.R
import com.example.gastroapp.databinding.ActivityRegistroBinding

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Agregar subrayado al texto de login
        binding.loginLink.paintFlags = binding.loginLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }

        binding.loginLink.setOnClickListener {
            finish() // Volver a la pantalla anterior (login)
        }
    }

    private fun validateAndRegister() {
        val name = binding.nameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        when {
            name.isEmpty() -> showError("Por favor ingrese su nombre")
            email.isEmpty() -> showError("Por favor ingrese su correo electrónico")
            !isValidEmail(email) -> showError("Por favor ingrese un correo electrónico válido")
            password.isEmpty() -> showError("Por favor ingrese una contraseña")
            password.length < 6 -> showError("La contraseña debe tener al menos 6 caracteres")
            password != confirmPassword -> showError("Las contraseñas no coinciden")
            else -> {
                // TODO: Implementar el registro con el repositorio
                showSuccess("Registro exitoso")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 