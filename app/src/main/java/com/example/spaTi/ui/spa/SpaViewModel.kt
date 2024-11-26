package com.example.spaTi.ui.spa

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.models.UserFavorites
import com.example.spaTi.data.repository.SpaRepository
import com.example.spaTi.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpaViewModel @Inject constructor(
    val repository: SpaRepository
) : ViewModel() {
    private val _spas = MutableLiveData<UiState<List<Spa>>>(UiState.Loading)
    val spas: LiveData<UiState<List<Spa>>>
        get() = _spas

    private val _getSpaById = MutableLiveData<UiState<Spa?>>()
    val getSpaById: LiveData<UiState<Spa?>>
        get() = _getSpaById

    private val _getServicesBySpaId = MutableLiveData<UiState<List<Service>>>(UiState.Loading)
    val getServicesBySpaId: LiveData<UiState<List<Service>>>
        get() = _getServicesBySpaId

    private val _actionToSpaFavorites = MutableLiveData<UiState<Pair<UserFavorites, String>>>(UiState.Loading)
    val actionToSpaFavorites: LiveData<UiState<Pair<UserFavorites, String>>>
        get() = _actionToSpaFavorites

    private val _getFavoritesSpas = MutableLiveData<UiState<List<Spa>>>(UiState.Loading)
    val getFavoritesSpas: LiveData<UiState<List<Spa>>>
        get() = _getFavoritesSpas

    fun getSpas() {
        _spas.value = UiState.Loading
        repository.getSpas { _spas.value = it }
    }

    fun getFavoritesSpas(user: User) {
        _getFavoritesSpas.value = UiState.Loading
        repository.getFavoritesSpas(user) { _getFavoritesSpas.value = it }
    }

    fun getSpaById(id: String) {
        _getSpaById.value = UiState.Loading
        repository.getSpaById(id) { uiState ->
            _getSpaById.value = uiState
            val spa = (uiState as? UiState.Success)?.data
            spa?.coordinates?.let { coordinates ->
                Log.d("SpaViewModel", "Coordenadas del spa: $coordinates")
            }
        }
    }

    fun getServicesBySpaId(id: String) {
        _getServicesBySpaId.value = UiState.Loading
        repository.getServicesBySpaId(id) { _getServicesBySpaId.value = it }
    }

    fun searchServicesOnSpa(spaId: String, query: String) {
        _getServicesBySpaId.value = UiState.Loading
        repository.searchServicesOnSpa(spaId, query) { _getServicesBySpaId.value = it }
    }

    fun actionToSpaFavorites(user: User, spa: Spa) {
        _actionToSpaFavorites.value = UiState.Loading
        repository.actionToSpaFavorites(user, spa) { _actionToSpaFavorites.value = it }
    }
}