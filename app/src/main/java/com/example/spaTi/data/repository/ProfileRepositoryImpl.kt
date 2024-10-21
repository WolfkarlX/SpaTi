package com.example.spaTi.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

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
                    if (it == null){
                        result.invoke(UiState.Failure("User updated successfully but session failed to store"))
                    }else{
                        result.invoke(
                            UiState.Success("User updated successfully!")
                        )
                    }
                }
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
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
            }.addOnFailureListener {
                result.invoke(UiState.Failure("Authentication failed, Check email"))
            }
    }

    override fun logout(result: () -> Unit) {
        auth.signOut()
        appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,null).apply()
        result.invoke()
    }

    override fun storeSession(id: String, result: (User?) -> Unit) {
        database.collection(FireStoreCollection.USER).document(id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val user = it.result.toObject(User::class.java)
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,gson.toJson(user)).apply()
                    result.invoke(user)
                }else{
                    result.invoke(null)
                }
            }
            .addOnFailureListener {
                result.invoke(null)
            }
    }

    override fun getSession(result: (User?) -> Unit) {
        val user_str = appPreferences.getString(SharedPrefConstants.USER_SESSION,null)
        if (user_str == null){
            result.invoke(null)
        }else{
            val user = gson.fromJson(user_str,User::class.java)
            result.invoke(user)
        }
    }

}