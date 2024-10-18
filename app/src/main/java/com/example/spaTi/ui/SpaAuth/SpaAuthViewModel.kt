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


    fun registerSpa(
        email: String,
        password: String,
        spa:Spa,
    ) {
        _register.value = UiState.Loading
        repository.registerSpa(
            email = email,
            password = password,
            spa = spa,
        ) { _register.value = it }
    }

    fun login(
        email: String,
        password: String,
    ) {
        _login.value = UiState.Loading
        Log.d("XDDD", "SPA viewmodel")
        Log.d("XDDD", email)
        Log.d("XDDD", password)
        repository.loginSpa(
            email,
            password
        ){
            _login.value = it
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