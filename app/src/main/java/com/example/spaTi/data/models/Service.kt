package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Data class representing a service in the application. Implements Parcelable for easy passing between activities.
 *
 * @property id The unique identifier for the service. Default is an empty string.
 * @property spaId The identifier of the associated spa. Default is an empty string. NOT IMPLEMENTED YET
 * @property name The name of the service. Default is an empty string.
 * @property durationMinutes The duration of the service in minutes. Default is 0.
 * @property price The price of the service in double precision. Default is 0.0.
 * @property categories A list of categories that this service belongs to. Default is an empty mutable list. NOT IMPLEMENTED YET
 * @property date The timestamp for the service creation or update. Uses Firestore's server timestamp annotation.
 */
@Parcelize
class Service (
    var id: String = "",
    val spaId: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val price: Double = 0.0,
    val categories: MutableList<String> = arrayListOf(),
    @ServerTimestamp
    val date: Date = Date(),
) : Parcelable