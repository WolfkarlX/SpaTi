package com.example.spaTi.data.repository

import android.net.Uri
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState

interface SpaProfileRepository {
    fun updateUserInfo(spa: Spa, result: (UiState<String>) -> Unit)
    fun uploadProfileImage(spaId: String, imageUri: Uri, result: (UiState<String>) -> Unit) // Nuevo método
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (Spa?) -> Unit)
    fun getSession(result: (Spa?) -> Unit)
    fun syncSessionWithDatabase(result: (UiState<Spa>) -> Unit)
}
