package com.example.spaTi.data.repository

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

//    override fun searchSpas(query: String, result: (UiState<List<Spa>>) -> Unit) {
//        TODO("Not yet implemented")
//    }
}