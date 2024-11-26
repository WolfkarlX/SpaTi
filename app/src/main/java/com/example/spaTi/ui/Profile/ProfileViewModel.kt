package com.example.spaTi.ui.Profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.ProfileRepository
import com.example.spaTi.util.UiState
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: ProfileRepository
) : ViewModel() {

    private val _session = MutableLiveData<UiState<User>>()
    val session: LiveData<UiState<User>>
        get() = _session

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>>
        get() = _forgotPassword

    private val _editUser = MutableLiveData<UiState<String>>()
    val editUser: LiveData<UiState<String>>
        get() = _editUser

    private val _updateProfilePicture = MutableLiveData<UiState<String>>()
    val updateProfilePicture: LiveData<UiState<String>> get() = _updateProfilePicture

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> get() = _profileImageUrl

    // Función para manejar el olvido de la contraseña
    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email) {
            _forgotPassword.value = it
        }
    }

    // Función para editar los detalles del usuario
    fun editUser(user: User) {
        _editUser.value = UiState.Loading
        repository.updateUserInfo(user) {
            _editUser.value = it
        }
    }

    // Función para cerrar sesión
    fun logout(result: () -> Unit) {
        repository.logout(result)
    }

    fun syncSessionWithDatabase() {
        _session.value = UiState.Loading
        repository.syncSessionWithDatabase { result ->
            _session.value = result
        }
    }

    // Actualiza la URL de la imagen de perfil en Firebase
    fun updateProfileImageUrl(userId: String, imageUrl: String) {
        // Lógica para actualizar la URL en la base de datos de Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("users/$userId")

        databaseRef.child("profileImageUrl").setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si la actualización fue exitosa
                Log.d("ProfileViewModel", "URL actualizada con éxito")
            } else {
                // Si hubo un error
                Log.e("ProfileViewModel", "Error al actualizar la URL")
            }
        }
    }

    // Obtiene la sesión actual del usuario
    fun getSession() {
        _session.value = UiState.Loading
        repository.getSession { user ->
            if (user != null) {
                _session.value = UiState.Success(user)
            } else {
                _session.value = UiState.Failure("No active session found.")
            }
        }
    }

    // Actualiza la imagen de perfil en la base de datos y en la UI
    fun updateProfilePicture(newImageUrl: String, userId: String) {
        _updateProfilePicture.value = UiState.Loading
        repository.updateProfilePicture(newImageUrl, userId) { result ->
            _updateProfilePicture.value = result
            _profileImageUrl.value = newImageUrl
        }
    }
}
