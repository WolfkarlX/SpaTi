package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SpaPrepayment (
    var id: String = "",
    var spaId: String = "",
    val description: String = "",
    val percentage: Double = 0.0,
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    var updatedAt: Date = Date(),
) : Parcelable