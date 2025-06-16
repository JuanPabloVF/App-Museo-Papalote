package com.example.mipapalote.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileViewModel : ViewModel() {

    private val _userName = MutableStateFlow("Nuevo Usuario") // Nombre predeterminado
    val userName: StateFlow<String> get() = _userName

    private val _profileImageBase64 = MutableStateFlow("")
    val profileImageBase64: StateFlow<String> get() = _profileImageBase64

    init {
        fetchUserName() // Llama a la función para obtener el nombre al iniciar el ViewModel
    }

    private fun fetchUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance().collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    _userName.value = document.getString("name") ?: "Usuario"
                    _profileImageBase64.value = document.getString("profileImageBase64") ?: ""
                }
                .addOnFailureListener {
                    _userName.value = "Usuario"
                    _profileImageBase64.value = ""
                }
        }
    }


    // Función para recargar el nombre del usuario cuando sea necesario
    fun reloadUserName() {
        fetchUserName()
        }
}