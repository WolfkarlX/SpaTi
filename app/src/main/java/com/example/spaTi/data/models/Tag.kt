package com.example.spaTi.data.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Data class representing a tag in a tag on the application. Implements Parcelable for easy passing between activities.
 *
 * @property id The unique identifier for the tag. Default is an empty string.
 * @property name The name of the tag. Default is an empty string.
 * @property createdAt The timestamp for the tag creation. Uses Firestore's server timestamp annotation.
 */
@Parcelize
class Tag (
    var id: String = "",
    val name: String = "",
    val relatedCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date = Date(),
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}