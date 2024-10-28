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
 * @property tags A list of [Tag]s ids that are related to. Default is an String list.
 * @property createdAt The timestamp for the service creation. Uses Firestore's server timestamp annotation.
 * @property updatedAt The timestamp for the service update. Uses Firestore's server timestamp annotation.
 */
@Parcelize
data class Service (
    var id: String = "",
    var spaId: String = "",
    val employeeId: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val price: Double = 0.0,
    val tags: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    var updatedAt: Date = Date(),
) : Parcelable