package com.example.spaTi.data.models


data class User(
    var id: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val job_title: String = "",
    val email: String = "",
    //Spa User
    val spa_name_label: String = "",
    val locatio_spa_label: String = "",
    val email_spa_label: String = "",
    val pass_spa_label: String = "",
)