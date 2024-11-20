package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FirebaseFirestore

class SpaRepositoryImpl (
    val database: FirebaseFirestore
) : SpaRepository {

    override fun getSpas(result: (UiState<List<Spa>>) -> Unit) {
        database.collection(FireStoreCollection.SPA)
            .get()
            .addOnSuccessListener {
                val spas = arrayListOf<Spa>()
                for (document in it) {
                    val spa = document.toObject(Spa::class.java)
                    spas.add(spa)
                }
                result.invoke(UiState.Success(spas))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

//    override fun addSpa(spa: Spa, result: (UiState<Pair<Spa, String>>) -> Unit) {
//        TODO("Not yet implemented")
//    }
//
//    override fun updateSpa(spa: Spa, result: (UiState<String>) -> Unit) {
//        TODO("Not yet implemented")
//    }
//
//    override fun deleteSpa(spa: Spa, result: (UiState<String>) -> Unit) {
//        TODO("Not yet implemented")
//    }

    override fun getSpaById(id: String, result: (UiState<Spa?>) -> Unit) {
        database.collection(FireStoreCollection.SPA)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val spa = documentSnapshot.toObject(Spa::class.java)
                    result.invoke(UiState.Success(spa))
                } else {
                    result.invoke(UiState.Success(null))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getServicesBySpaId(id: String, result: (UiState<List<Service>>) -> Unit) {
        database.collection(FireStoreCollection.SERVICE)
            .whereEqualTo("spaId", id)
            .get()
            .addOnSuccessListener { snapshot ->
                val services = snapshot.documents.mapNotNull { document ->
                    document.toObject(Service::class.java)
                }
                result(UiState.Success(services))
            }
            .addOnFailureListener {
                result(UiState.Failure(it.localizedMessage))
            }
    }

    override fun searchSpas(id: String, query: String, result: (UiState<List<Spa>>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun searchServicesOnSpa(
        spaId: String,
        query: String,
        result: (UiState<List<Service>>) -> Unit
    ) {
        database.collection(FireStoreCollection.SERVICE)
            .orderBy("name")
            .whereEqualTo("spaId", spaId)
            .startAt(query)
            .endAt(query + '\uf8ff')
            .get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.toObjects(Service::class.java)
                result.invoke(UiState.Success(services))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }
}