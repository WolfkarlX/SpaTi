package com.example.spaTi.data.repository

import android.content.SharedPreferences
import android.net.Uri
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson

class SpaProfileRepositoryImpl(
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : SpaProfileRepository {

    // Other existing functions...

    override fun syncSessionWithDatabase(result: (UiState<Spa>) -> Unit) {
        getSession { localSpa ->
            if (localSpa == null) {
                result.invoke(UiState.Failure("No local session found."))
                return@getSession
            }

            val document = database.collection(FireStoreCollection.SPA).document(localSpa.id)
            document.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val dbSpa = documentSnapshot.toObject(Spa::class.java)
                        val status = documentSnapshot.getString("status")
                        if (dbSpa == null) {
                            result.invoke(UiState.Failure("Failed to parse session data from database."))
                        } else if (status != null && status == "disabled") {
                            result.invoke(UiState.Failure("This user was disabled for multiples report."))
                        } else {
                            appPreferences.edit()
                                .putString(SharedPrefConstants.USER_SESSION, gson.toJson(dbSpa))
                                .apply()
                            result.invoke(UiState.Success(dbSpa))
                        }
                    } else {
                        result.invoke(UiState.Failure("Session does not exist."))
                    }
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure("Failed to retrieve session from database"))
                }
        }
    }

    override fun updateUserInfo(spa: Spa, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.SPA).document(spa.id)
        document
            .set(spa)
            .addOnSuccessListener {
                storeSession(spa.id) {
                    if (it == null) {
                        result.invoke(UiState.Failure("User updated successfully but session failed to store"))
                    } else {
                        result.invoke(UiState.Success("User updated successfully!"))
                    }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun forgotPassword(email: String, result: (UiState<String>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.invoke(UiState.Success("Email has been sent"))
                } else {
                    result.invoke(UiState.Failure(task.exception?.message))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure("Authentication failed, Check email"))
            }
    }

    override fun uploadProfileImage(spaId: String, imageUri: Uri, result: (UiState<String>) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_images/$spaId.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    result.invoke(UiState.Success(uri.toString())) // Enviamos la URL de la imagen
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure("Failed to upload image: ${exception.message}"))
            }
    }

    override fun logout(result: () -> Unit) {
        auth.signOut()
        appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, null).apply()
        result.invoke()
    }

    override fun storeSession(id: String, result: (Spa?) -> Unit) {
        database.collection(FireStoreCollection.SPA).document(id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val spa = it.result.toObject(Spa::class.java)
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, gson.toJson(spa)).apply()
                    result.invoke(spa)
                } else {
                    result.invoke(null)
                }
            }
            .addOnFailureListener {
                result.invoke(null)
            }
    }

    override fun getSession(result: (Spa?) -> Unit) {
        val userStr = appPreferences.getString(SharedPrefConstants.USER_SESSION, null)
        if (userStr == null) {
            result.invoke(null)
        } else {
            val spa = gson.fromJson(userStr, Spa::class.java)
            result.invoke(spa)
        }
    }
}
