package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Service
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Implementation of the [ServiceRepository] interface that uses Firestore as the data source.
 * This class provides methods to perform CRUD operations on [Service] objects in Firestore.
 */
class ServiceRepositoryImpl ( // There is a meaning in naming <Entity>RepositoryImp instead of <Entity>RepositoryImpl? @Alang315
    val database: FirebaseFirestore,
    val sparepository: SpaAuthRepository,
    private val tagRepository: TagRepository
) : ServiceRepository {

    /**
     * Retrieves all services from the Firestore collection, ordered by date in descending order.
     *
     * @param result A callback function that returns a [UiState] containing a list of [Service] objects if successful,
     * or an error message if the operation fails.
     */
    override fun getServices(result: (UiState<List<Service>>) -> Unit) {
        var id: String? = null
        sparepository.getSession {
            if (it != null) {
                id=it.id
            }
        }
        database.collection(FireStoreCollection.SERVICE)
            .whereEqualTo("spaId",id)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                val services = arrayListOf<Service>()
                for (document in it) {
                    val service = document.toObject(Service::class.java)
                    services.add(service)
                }
                result.invoke(
                    UiState.Success(services)
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    /**
     * Adds a new service to the Firestore collection. Generates a new document ID for the service.
     *
     * @param service The [Service] object to be added to Firestore.
     * @param result A callback function that returns a [UiState] containing a pair of the added [Service]
     * and a success message if the operation is successful, or an error message if it fails.
     */
    override fun addService(service: Service, result: (UiState<Pair<Service, String>>) -> Unit) {
        val document = database.collection(FireStoreCollection.SERVICE).document()
        service.id = document.id
        document
            .set(service)
            .addOnSuccessListener {
                updateTagCounts(emptyList(), service.tags)
                result.invoke(
                    UiState.Success(Pair(
                        service,
                        "Service has been created successfully"
                    ))
                )
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    /**
     * Updates an existing service in the Firestore collection based on the service's ID.
     *
     * @param service The [Service] object containing the updated data.
     * @param result A callback function that returns a [UiState] containing a success message if the operation
     * is successful, or an error message if it fails.
     */
    override fun updateService(service: Service, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.SERVICE).document(service.id)
        document
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val oldService = documentSnapshot.toObject(Service::class.java)
                document
                    .set(service)
                    .addOnSuccessListener {
                        updateTagCounts(oldService?.tags ?: emptyList(), service.tags)
                        result.invoke(UiState.Success("Service has been update successfully"))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    /**
     * Deletes an existing service from the Firestore collection based on the service's ID.
     *
     * @param service The [Service] object to be deleted.
     * @param result A callback function that returns a [UiState] containing a success message if the operation
     * is successful, or an error message if it fails.
     */
    override fun deleteService(service: Service, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.SERVICE).document(service.id)
            .delete()
            .addOnSuccessListener {
                updateTagCounts(service.tags, emptyList())
                result.invoke(UiState.Success("Service successfully deleted!"))
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message))
            }
    }

    override fun getServiceById(id: String, result: (UiState<Service?>) -> Unit) {
        database.collection(FireStoreCollection.SERVICE)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val service = documentSnapshot.toObject(Service::class.java)
                    result.invoke(UiState.Success(service))
                } else {
                    result.invoke(UiState.Success(null))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    private fun updateTagCounts(oldTags: List<String>, newTags: List<String>) {
        val tagsToDecrement = oldTags.filter { !newTags.contains(it) }
        val tagsToIncrement = newTags.filter { !oldTags.contains(it) }

        tagsToDecrement.forEach { tagId ->
            tagRepository.updateTagCount(tagId, -1)
        }

        tagsToIncrement.forEach { tagId ->
            tagRepository.updateTagCount(tagId, 1)
        }
    }
}