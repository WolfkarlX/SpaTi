package com.example.spaTi.data.repository

import android.util.Log
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.SpaPrepayment
import com.example.spaTi.data.models.User
import com.example.spaTi.data.models.UserFavorites
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.UiState
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import java.util.Date

class SpaRepositoryImpl (
    val database: FirebaseFirestore
) : SpaRepository {

    override fun getSpas(result: (UiState<List<Spa>>) -> Unit) {
        database.collection(FireStoreCollection.SPA)
            .get()
            .addOnSuccessListener {
                val spas = arrayListOf<Spa>()
                for (document in it) {
                    val status = document.getString("status")
                    if (status == null || status != "disabled") {
                        val spa = document.toObject(Spa::class.java)
                        spas.add(spa)
                    }
                }
                result.invoke(UiState.Success(spas))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

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

    override fun actionToSpaFavorites(user: User, spa: Spa, result: (UiState<Pair<UserFavorites, String>>) -> Unit) {
        // First, check if the user already has a favorites document
        database.collection(FireStoreCollection.USER_FAVORITES)
            .whereEqualTo("userId", user.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No existing favorites document, create a new one
                    addSpaFavorites(user, spa, result)
                } else {
                    // Existing favorites document found, update it
                    val existingDocument = querySnapshot.documents[0]
                    updateSpaFavorites(existingDocument, spa, result)
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun addSpaFavorites(
        user: User,
        spa: Spa,
        result: (UiState<Pair<UserFavorites, String>>) -> Unit
    ) {
        val document = database.collection(FireStoreCollection.USER_FAVORITES).document()
        val userFavorites = UserFavorites(
            id = document.id,
            userId = user.id,
            favoritesSpas = listOf(spa.id),
        )

        document
            .set(userFavorites)
            .addOnSuccessListener {
                result.invoke(UiState.Success(Pair(userFavorites, "Spa added to favorites")))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateSpaFavorites(
        existingDocument: DocumentSnapshot,
        spa: Spa,
        result: (UiState<Pair<UserFavorites, String>>) -> Unit
    ) {
        // Safely convert to UserFavorites and handle potential null cases
        val userFavorites = existingDocument.toObject(UserFavorites::class.java)

        // If userFavorites is null, invoke failure
        if (userFavorites == null) {
            result.invoke(UiState.Failure("Could not retrieve user favorites"))
            return
        }

        // Create a mutable list of current favorite spas
        val currentFavoriteSpas = userFavorites.favoritesSpas.toMutableList()


        if (currentFavoriteSpas.contains(spa.id)) {
            // Remove spa from favorites
            currentFavoriteSpas.remove(spa.id)
        } else {
            // Add spa to favorites
            currentFavoriteSpas.add(spa.id)
        }
        userFavorites.favoritesSpas = currentFavoriteSpas

        // Prepare update data
        val updateData = mapOf(
            "favoritesSpas" to currentFavoriteSpas,
            "updatedAt" to Date()
        )

        database.collection(FireStoreCollection.USER_FAVORITES)
            .document(userFavorites.id)
            .update(updateData)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(Pair(userFavorites, "Spa added to favorites"))
                )
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getFavoritesSpas(user: User, result: (UiState<List<Spa>>) -> Unit) {
        database.collection(FireStoreCollection.USER_FAVORITES)
            .whereEqualTo("userId", user.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userFavorites = querySnapshot.documents[0].toObject(UserFavorites::class.java)

                    // If there are favorite spa IDs
                    if (userFavorites?.favoritesSpas?.isNotEmpty() == true) {
                        database.collection(FireStoreCollection.SPA)
                            .whereIn("id", userFavorites.favoritesSpas)
                            .get()
                            .addOnSuccessListener { spaSnapshot ->
                                val favoriteSpasList = spaSnapshot.toObjects(Spa::class.java)
                                result.invoke(UiState.Success(favoriteSpasList))
                            }
                            .addOnFailureListener {
                                result.invoke(UiState.Failure(it.localizedMessage))
                            }
                    } else {
                        // No favorite spas found
                        result.invoke(UiState.Success(emptyList()))
                    }
                } else {
                    // No user favorites document found
                    result.invoke(UiState.Success(emptyList()))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }
}