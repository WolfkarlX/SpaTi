package com.example.spaTi.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class SpaAuthRepositoryImpl(
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : SpaAuthRepository {

    override fun registerSpa(
        email: String,
        password: String,
        spa: Spa, result: (UiState<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    spa.id = it.result.user?.uid ?: ""
                    updateSpaInfo(spa) { state ->
                        when(state) {
                            is UiState.Success -> {
                                result.invoke(UiState.Success("Verification email sent to ${spa.email}"))
                            }
                            is UiState.Failure -> {
                                result.invoke(UiState.Failure(state.error))
                            }
                            else -> {}
                        }
                    }
                }else{
                    try {
                        throw it.exception ?: java.lang.Exception("Invalid authentication")
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        result.invoke(UiState.Failure("Authentication failed, Password should be at least 6 characters"))
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        result.invoke(UiState.Failure("Authentication failed, Invalid email entered"))
                    } catch (e: FirebaseAuthUserCollisionException) {
                        result.invoke(UiState.Failure("Authentication failed, Email already registered."))
                    } catch (e: Exception) {
                        result.invoke(UiState.Failure(e.message))
                    }
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun updateSpaInfo(spa: Spa, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.SPA).document(spa.id)
        document
            .set(spa)
            .addOnSuccessListener {
                result.invoke(UiState.Success("User has been update successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun loginSpa(
        email: String,
        password: String,
        result: (UiState<String>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storeSession(id = task.result.user?.uid ?: ""){
                        if (it == null){
                            result.invoke(UiState.Failure("Failed to store local session"))
                        }else{
                            result.invoke(UiState.Success("Login successfully!"))
                        }
                    }
                }
            }.addOnFailureListener {
                result.invoke(UiState.Failure("Authentication failed, Check email and password"))
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
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION, gson.toJson(spa)).apply()
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

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}