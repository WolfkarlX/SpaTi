package com.example.spaTi.ui.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.repository.ServiceRepository
import com.example.spaTi.data.repository.SpaAuthRepository
import com.example.spaTi.util.UiState
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing service-related data and operations in the UI layer.
 *
 * This ViewModel communicates with the [ServiceRepository] to fetch, add, update, and delete services.
 * It exposes LiveData objects for observing the state of these operations in the UI.
 *
 * @param repository The [ServiceRepository] instance injected via Hilt for performing CRUD operations on services.
 */
@HiltViewModel
class ServiceViewModel @Inject constructor(
    val repository: ServiceRepository,
    val sparepository: SpaAuthRepository

): ViewModel() {

    // LiveData for observing the list of services and its state.
    private val _services = MutableLiveData<UiState<List<Service>>>()
    val service: LiveData<UiState<List<Service>>>
        get() = _services

    // LiveData for observing the result of adding a new service.
    private val _addService = MutableLiveData<UiState<Pair<Service, String>>>()
    val addService: LiveData<UiState<Pair<Service, String>>>
        get() = _addService

    // LiveData for observing the result of updating an existing service.
    private val _updateService = MutableLiveData<UiState<String>>()
    val updateService: LiveData<UiState<String>>
        get() = _updateService

    // LiveData for observing the result of deleting a service.
    private val _deleteService = MutableLiveData<UiState<String>>()
    val deleteService: LiveData<UiState<String>>
        get() = _deleteService


    /**
     * Fetches the list of services from the repository and updates the _services LiveData.
     * Sets the state to [UiState.Loading] while fetching and updates with success or failure results.
     */
    fun getServices() {
        _services.value = UiState.Loading
        repository.getServices { _services.value = it }
    }

    /**
     * Adds a new service using the repository and updates the _addService LiveData.
     * Sets the state to [UiState.Loading] while the operation is in progress and updates with the result.
     *
     * @param service The [Service] object to be added.
     */
    fun addService(service: Service) {
        val serviceId = FirebaseDatabase.getInstance().reference.child("service").push().key
        if (serviceId != null) {
            service.id = serviceId
        }
        sparepository.getSession {
            if (it != null) {
                service.spaId = it.id
            }
        }
        FirebaseDatabase.getInstance().reference.child("service").child(serviceId!!).setValue(service)
        _addService.value = UiState.Loading
        repository.addService(service) { _addService.value = it }
    }

    /**
     * Updates an existing service using the repository and updates the _updateService LiveData.
     * Sets the state to [UiState.Loading] while the operation is in progress and updates with the result.
     *
     * @param service The [Service] object to be updated.
     */
    fun updateService(service: Service) {
        _updateService.value = UiState.Loading
        repository.updateService(service) { _updateService.value = it }
    }

    /**
     * Deletes a service using the repository and updates the _deleteService LiveData.
     * Sets the state to [UiState.Loading] while the operation is in progress and updates with the result.
     *
     * @param service The [Service] object to be deleted.
     */
    fun deleteService(service: Service) {
        _deleteService.value = UiState.Loading
        repository.deleteService(service) { _deleteService.value = it }
    }
}