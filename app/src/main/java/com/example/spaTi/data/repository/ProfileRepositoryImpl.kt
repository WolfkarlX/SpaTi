package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import javax.inject.Inject

class ProfileRepositoryImpl(
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : ProfileRepository {

    override fun updateUserInfo(user: User, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.USER).document(user.id)
        document
            .set(user)
            .addOnSuccessListener {
                storeSession(user.id) {
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

    override fun logout(result: () -> Unit) {
        auth.signOut()
        appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, null).apply()
        result.invoke()
    }

    override fun storeSession(id: String, result: (User?) -> Unit) {
        database.collection(FireStoreCollection.USER).document(id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.toObject(User::class.java)
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, gson.toJson(user)).apply()
                    result.invoke(user)
                } else {
                    result.invoke(null)
                }
            }
            .addOnFailureListener {
                result.invoke(null)
            }
    }

    override fun getSession(result: (User?) -> Unit) {
        val userStr = appPreferences.getString(SharedPrefConstants.USER_SESSION, null)
        if (userStr == null) {
            result.invoke(null)
        } else {
            val user = gson.fromJson(userStr, User::class.java)
            result.invoke(user)
        }
    }

    override fun syncSessionWithDatabase(result: (UiState<User>) -> Unit) {
        getSession { localUser ->
            if (localUser == null) {
                result.invoke(UiState.Failure("No local session found."))
                return@getSession
            }

            val document = database.collection(FireStoreCollection.USER).document(localUser.id)
            document.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val dbUser = documentSnapshot.toObject(User::class.java)
                        if (dbUser != null) {
                            appPreferences.edit()
                                .putString(SharedPrefConstants.USER_SESSION, gson.toJson(dbUser))
                                .apply()

                            result.invoke(UiState.Success(dbUser))
                        } else {
                            result.invoke(UiState.Failure("Failed to parse session data from database."))
                        }
                    } else {
                        result.invoke(UiState.Failure("Session does not exist."))
                    }
                }
                .addOnFailureListener {
                    result.invoke(UiState.Failure("Failed to retrieve session from database: ${it.localizedMessage}"))
                }
        }
    }

    override fun updateProfilePicture(newImageUrl: String, userId: String, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.USER).document(userId)
        document.update(
            "profileImageUrl",
            newImageUrl
        )
            .addOnSuccessListener {
                result.invoke(UiState.Success("Profile picture updated successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.localizedMessage ?: "Error updating profile picture"
                    )
                )
            }
    }
}
