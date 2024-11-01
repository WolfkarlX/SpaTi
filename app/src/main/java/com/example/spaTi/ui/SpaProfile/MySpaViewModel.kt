package com.example.spaTi.ui.SpaProfile

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

    // New function to sync session with database
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

}
