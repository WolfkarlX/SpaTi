package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState

/**
 * Interface representing the repository for managing services.
 * This interface defines the contract for CRUD (Create, Read, Update, Delete) operations
 * on [Service] objects, facilitating database interactions. The repository functions
 * asynchronously using callback functions that return different types of UiState
 * to indicate the state of the operation (loading, success, or failure).
 *
 * @see [ServiceRepositoryImpl] For the implementation of this interface.
 */
interface ServiceRepository {
    fun getServices(result: (UiState<List<Service>>) -> Unit)
    fun addService(service: Service, result: (UiState<Pair<Service, String>>) -> Unit)
    fun updateService(service: Service, result: (UiState<String>) -> Unit)
    fun deleteService(service: Service, result: (UiState<String>) -> Unit)

    fun getServiceById(id: String, result: (UiState<Service?>) -> Unit)
    fun getServicesByTagId(id: String, result: (UiState<List<Service>>) -> Unit)
    fun getServicesWithSpaByTagId(id: String, result: (UiState<List<Pair<Service, Spa>>>) -> Unit)
}