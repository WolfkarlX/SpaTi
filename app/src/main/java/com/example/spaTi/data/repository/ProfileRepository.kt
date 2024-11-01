package com.example.spaTi.data.repository

import com.example.spaTi.data.models.User
import com.example.spaTi.util.UiState

interface ProfileRepository {
    fun updateUserInfo(user: User, result: (UiState<String>) -> Unit)
    fun forgotPassword(email: String, result: (UiState<String>) -> Unit)
    fun logout(result: () -> Unit)
    fun storeSession(id: String, result: (User?) -> Unit)
    fun getSession(result: (User?) -> Unit)
    fun syncSessionWithDatabase(result: (UiState<User>) -> Unit)
}
