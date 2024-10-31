package com.example.spaTi.ui.SpaProfile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.data.repository.ProfileRepository
import com.example.spaTi.data.repository.SpaProfileRepository
import com.example.spaTi.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MySpaViewModel @Inject constructor(
    val repository: SpaProfileRepository
): ViewModel() {

    private val _session = MutableLiveData<UiState<Spa?>>()
    val session: LiveData<UiState<Spa?>>
        get() = _session

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>>
        get() = _forgotPassword

    private val _editUser = MutableLiveData<UiState<String>>()
    val editUser: LiveData<UiState<String>>
        get() = _editUser

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email){
            _forgotPassword.value = it
        }
    }

    fun editUser(spa: Spa){
        _editUser.value = UiState.Loading
        repository.updateUserInfo(spa){
            _editUser.value = it
        }
    }

    fun logout(result: () -> Unit){
        repository.logout(result)
    }

    fun getSession(){
        _session.value = UiState.Loading
        repository.getSession { spa ->
            if (spa != null) {
                _session.value = UiState.Success(spa) // Set success state with user data
            } else {
                _session.value = UiState.Failure("No active session found.") // Set failure state
            }
        }
    }
}