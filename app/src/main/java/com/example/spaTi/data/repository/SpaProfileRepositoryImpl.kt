package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class SpaProfileRepositoryImpl(
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : SpaProfileRepository {


    override fun updateUserInfo(spa: Spa, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.SPA).document(spa.id)
        document
            .set(spa)
            .addOnSuccessListener {
                storeSession(spa.id) {
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

    override fun storeSession(id: String, result: (Spa?) -> Unit) {
        database.collection(FireStoreCollection.SPA).document(id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val spa = it.result.toObject(Spa::class.java)
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,gson.toJson(spa)).apply()
                    result.invoke(spa)
                }else{
                    result.invoke(null)
                }
            }
            .addOnFailureListener {
                result.invoke(null)
            }
    }

    override fun getSession(result: (Spa?) -> Unit) {
        val user_str = appPreferences.getString(SharedPrefConstants.USER_SESSION,null)
        if (user_str == null){
            result.invoke(null)
        }else{
            val spa = gson.fromJson(user_str,Spa::class.java)
            result.invoke(spa)
        }
    }

}