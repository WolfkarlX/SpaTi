package com.example.spaTi.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.util.UiState
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val repository: AuthRepository
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


    fun register(
        email: String,
        password: String,
        user:User,
    ) {
        _register.value = UiState.Loading
        repository.registerUser(
            email = email,
            password = password,
            user = user
        ) { result ->
            if (result is UiState.Success) {
                // El registro fue exitoso, enviar correo de verificación
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
                    Log.d("AuthViewModel", "Verification email sent to ${user.email}")
                    _register.value = UiState.Success("Registration successful. Verification email sent to ${user.email}")
                } else {
                    Log.e("AuthViewModel", "Failed to send verification email", task.exception)
                    _register.value = UiState.Failure("Registration successful but failed to send verification email.")
                }
            }
    }
    fun login(
        email: String,
        password: String
    ) {
        _login.value = UiState.Loading
        Log.d("XDDD", "auth viewmodel")
        Log.d("XDDD", email)
        Log.d("XDDD", password)
        repository.loginUser(
            email,
            password
        ){ result ->
            if (result is UiState.Success) {
                val firebaseUser = repository.getCurrentUser()
                // Comprobar si el correo está verificado
                if (firebaseUser?.isEmailVerified == true) {
                    _login.value = result
                } else {
                    _login.value = UiState.Failure("Please verify your email before logging in.")
                    // Cerrar sesión si el email no está verificado
                }
            }else
            {
                _login.value = result
            }
        }

    }

    fun forgotPassword(email: String) {
        _forgotPassword.value = UiState.Loading
        repository.forgotPassword(email){
            _forgotPassword.value = it
        }
    }

    fun getSession(result: (User?) -> Unit) {
        repository.getSession(result)
    }

    fun logout(result: () -> Unit) {
        repository.logout(result)
    }
}