package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class AppointmentReceipt (
    var id: String = "",
    val appointmentId: String = "",
    val url: String = "",
    val uploadedAt: String = "",
    val fileType: String = "",
    @ServerTimestamp
    val createdAt: Date = Date(),
) : Parcelable