package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Spa (
    var id: String = "",
    val spa_name: String = "",
    val location: String = "",
    val coordinates: String = "",
    val email: String = "",
    val cellphone: String = "",
    val description: String = "",
    val type: String = "",
    val inTime: String = "",
    val outTime: String = "",
    val reports: String = "",
    var profileImageUrl: String? = null,
    val prepayment: Boolean = false,
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    var updatedAt: Date = Date(),
) : Parcelable