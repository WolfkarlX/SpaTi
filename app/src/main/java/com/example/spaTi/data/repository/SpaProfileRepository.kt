package com.example.spaTi.data.repository

import android.net.Uri
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.SpaPrepayment
import com.example.spaTi.util.UiState

interface SpaProfileRepository {
    fun updateUserInfo(spa: Spa, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun uploadProfileImage(spaId: String, imageUri: Uri, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (Spa?) -> Unit)
    fun getSession(result: (Spa?) -> Unit)
    fun syncSessionWithDatabase(result: (UiState<Spa>) -> Unit)

    fun actionPrepay(spaId: String, spaPrepayment: SpaPrepayment, result: (UiState<Pair<SpaPrepayment, String>>) -> Unit)
    fun addPrepay(spaPrepayment: SpaPrepayment, result: (UiState<Pair<SpaPrepayment, String>>) -> Unit)
    fun updatePrepay(spaPrepayment: SpaPrepayment, result: (UiState<String>) -> Unit)
    fun getPrepayment(spaId: String, result: (UiState<SpaPrepayment?>) -> Unit)
}
