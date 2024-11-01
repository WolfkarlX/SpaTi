package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState

interface SpaProfileRepository {
    fun updateUserInfo(spa: Spa, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (Spa?) -> Unit)
    fun getSession(result: (Spa?) -> Unit)
}
