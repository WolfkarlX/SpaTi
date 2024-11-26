package com.example.spaTi.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserFavorites(
    var id: String = "",
    val userId: String = "",
    var favoritesSpas: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    var updatedAt: Date = Date(),
)