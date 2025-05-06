package com.example.gastroapp.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    
    private val _userData = MutableLiveData<UserProfileData>()
    val userData: LiveData<UserProfileData> = _userData

    init {
        loadMockUserData()
    }

    private fun loadMockUserData() {
        _userData.value = UserProfileData(
            name = "Juan Camilo Londoño Suárez",
            email = "juan.camilo@email.com",
            phone = "300 123 4567",
            address = "CR 10 # 10 - 10 Bogotá D.C",
            badges = listOf("Catador innato", "Reseñador premium"),
            favoriteRestaurants = 0,
            reviews = 0,
            reservations = 0
        )
    }

    fun updateProfileImage(imageUri: String) {
        // TODO: Implementar actualización de imagen de perfil
    }

    fun logout() {
        // TODO: Implementar lógica de cierre de sesión
    }
}

data class UserProfileData(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val badges: List<String>,
    val favoriteRestaurants: Int,
    val reviews: Int,
    val reservations: Int
)
