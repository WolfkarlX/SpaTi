package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Note
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.util.UiState


interface SpaAuthRepository {
    fun registerSpa(email: String, password: String, spa: Spa, result: (UiState<String>) -> Unit)
    fun updateSpaInfo(spa: Spa, result: (UiState<String>) -> Unit)
    fun loginSpa(email: String, password: String, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (Spa?) -> Unit)
    fun getSession(result: (Spa?) -> Unit)
}