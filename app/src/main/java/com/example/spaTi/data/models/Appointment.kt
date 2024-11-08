package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Appointment (
    var id: String = "",
    var spaId: String = "",
    var userId: String = "",
    var serviceId: String = "",
    val date: String = "",
    val dateTime: String = "",
    val status: String = "",
    @ServerTimestamp
    val createdAt: Date = Date(),
) : Parcelable