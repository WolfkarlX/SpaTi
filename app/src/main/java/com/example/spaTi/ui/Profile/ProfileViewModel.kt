package com.example.spaTi.ui.Profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.ProfileRepository
import com.example.spaTi.util.UiState
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
    val updateProfilePicture: LiveData<UiState<String>>
        get() = _updateProfilePicture

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email) {
            _forgotPassword.value = it
        }
    }

    fun editUser(user: User) {
        _editUser.value = UiState.Loading
        repository.updateUserInfo(user) {
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
        repository.getSession { user ->
            if (user != null) {
                _session.value = UiState.Success(user)
            } else {
                _session.value = UiState.Failure("No active session found.")
            }
        }
    }
    fun updateProfilePicture(newImageUrl: String, userId: String) {
        _updateProfilePicture.value = UiState.Loading
        repository.updateProfilePicture(newImageUrl, userId) { result ->
            _updateProfilePicture.value = result
        }
    }
}