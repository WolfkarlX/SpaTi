package com.example.spaTi.data.models


data class User(
    var id: String = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val type: String? = "",
    val email: String? = "",
)