package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState

interface SpaRepository {
    fun getSpas(result: (UiState<List<Spa>>) -> Unit)
//    fun addSpa(spa: Spa, result: (UiState<Pair<Spa, String>>) -> Unit)
//    fun updateSpa(spa: Spa, result: (UiState<String>) -> Unit)
//    fun deleteSpa(spa: Spa, result: (UiState<String>) -> Unit)

    fun getSpaById(id: String, result: (UiState<Spa?>) -> Unit)
//    fun searchSpas(query: String, result: (UiState<List<Spa>>) -> Unit)
}