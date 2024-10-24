package com.example.spaTi.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.data.repository.SpaAuthRepository
import com.example.spaTi.util.UiState
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpaAuthViewModel @Inject constructor(
    val repository: SpaAuthRepository
): ViewModel() {

    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register

    private val _login = MutableLiveData<UiState<String>>()
    val login: LiveData<UiState<String>>
        get() = _login

    private val _forgotPassword = MutableLiveData<UiState<String>>()
    val forgotPassword: LiveData<UiState<String>>
        get() = _forgotPassword


    fun registerSpa(email: String, password: String, spa: Spa) {
        _register.value = UiState.Loading
        repository.registerSpa(email = email, password = password, spa = spa) { result ->
            if (result is UiState.Success) {
                val firebaseUser = repository.getCurrentUser()
                firebaseUser?.let {
                    sendEmailVerification(it)
                }?: run {
                    _register.value = UiState.Failure("Failed to get current user.")
                }
            }

            _register.value = result
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _register.value = UiState.Success("Registration successful. Verification email sent to ${user.email}")
                } else {
                    Log.e("SpaAuthViewModel", "Failed to send verification email", task.exception)
                    _register.value = UiState.Failure("Registration successful but failed to send verification email.")
                }
            }
    }

    fun login(email: String, password: String) {
        _login.value = UiState.Loading
        repository.loginSpa(email, password){ state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    _login.value = UiState.Failure(state.error)
                }
                is UiState.Success -> {
                    val firebaseUser = repository.getCurrentUser()
                    if (firebaseUser?.isEmailVerified == true) {
                        _login.value = UiState.Success(state.data)
                    } else {
                        repository.logout {}
                        _login.value = UiState.Success("Please verify your email before logging in.")
                    }
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email){
            _forgotPassword.value = it
        }
    }

    fun logout(result: () -> Unit){
        repository.logout(result)
    }

    fun getSession(result: (Spa?) -> Unit){
        repository.getSession(result)
    }
}