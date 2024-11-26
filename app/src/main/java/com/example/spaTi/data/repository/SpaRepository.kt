package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.models.UserFavorites
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot

interface SpaRepository {
    fun getSpas(result: (UiState<List<Spa>>) -> Unit)
//    fun addSpa(spa: Spa, result: (UiState<Pair<Spa, String>>) -> Unit)
//    fun updateSpa(spa: Spa, result: (UiState<String>) -> Unit)
//    fun deleteSpa(spa: Spa, result: (UiState<String>) -> Unit)

    fun getSpaById(id: String, result: (UiState<Spa?>) -> Unit)
    fun getServicesBySpaId(id: String, result: (UiState<List<Service>>) -> Unit)
    fun searchSpas(id: String, query: String, result: (UiState<List<Spa>>) -> Unit)
    fun searchServicesOnSpa(spaId: String, query: String, result: (UiState<List<Service>>) -> Unit)

    fun actionToSpaFavorites(user: User, spa: Spa, result: (UiState<Pair<UserFavorites, String>>) -> Unit)
    fun addSpaFavorites(user: User, spa: Spa, result: (UiState<Pair<UserFavorites, String>>) -> Unit)
    fun updateSpaFavorites(existingDocument: DocumentSnapshot, spa: Spa, result: (UiState<Pair<UserFavorites, String>>) -> Unit)
    fun getFavoritesSpas(user: User, result: (UiState<List<Spa>>) -> Unit)
}