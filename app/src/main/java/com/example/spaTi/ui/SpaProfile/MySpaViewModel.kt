package com.example.spaTi.ui.SpaProfile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.repository.SpaProfileRepository
import com.example.spaTi.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MySpaViewModel @Inject constructor(
    val repository: SpaProfileRepository
) : ViewModel() {

    private val _session = MutableLiveData<UiState<Spa>>()
    val session: LiveData<UiState<Spa>>
        get() = _session

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>>
        get() = _forgotPassword

    private val _editUser = MutableLiveData<UiState<String>>()
    val editUser: LiveData<UiState<String>>
        get() = _editUser

    private val _updateProfileImage = MutableLiveData<UiState<String>>()
    val updateProfileImage: LiveData<UiState<String>>
        get() = _updateProfileImage

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email) {
            _forgotPassword.value = it
        }
    }

    fun editUser(spa: Spa) {
        _editUser.value = UiState.Loading
        repository.updateUserInfo(spa) {
            _editUser.value = it
        }
    }

    fun logout(result: () -> Unit) {
        repository.logout(result)
    }

    fun syncSessionWithDatabase() {
        _session.value = UiState.Loading
        repository.syncSessionWithDatabase { result ->
            _session.value = result
        }
    }

    fun getSession() {
        _session.value = UiState.Loading
        repository.getSession { result ->
            if (result != null) {
                _session.value = UiState.Success(result)
            } else {
                _session.value = UiState.Failure("No session found")
            }
        }
    }

    // Nueva función para subir imagen de perfil
    fun updateProfileImage(spaId: String, imageUri: Uri) {
        _updateProfileImage.value = UiState.Loading
        repository.uploadProfileImage(spaId, imageUri) { result ->
            if (result is UiState.Success) {
                // Obtener el spa desde el repositorio usando spaId
                repository.getSession { spa ->
                    if (spa != null) {
                        // Actualizar la URL de la imagen en el objeto Spa
                        spa.profileImageUrl = result.data // Asume que tienes un campo 'profileImageUrl' en tu modelo 'Spa'

                        // Ahora actualiza el perfil con la nueva URL de la imagen
                        repository.updateUserInfo(spa) { updateResult ->
                            _updateProfileImage.value = updateResult
                        }
                    } else {
                        _updateProfileImage.value = UiState.Failure("Failed to retrieve spa session")
                    }
                }
            } else {
                _updateProfileImage.value = UiState.Failure("Error uploading image")
            }
        }
    }
}