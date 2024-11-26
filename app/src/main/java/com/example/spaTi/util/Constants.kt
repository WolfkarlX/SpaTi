package com.example.spaTi.util

import com.example.spaTi.data.models.Appointment

object FireStoreCollection{
    val NOTE = "note"
    val USER = "user"
    val SERVICE = "service"
    val SPA = "spa"
    val SPA_PREPAYMENT = "spa_prepayment"
    val TAG = "tag"
    val APPOINTMENT = "appointment"
    val APPOINTMENT_RECEIPT = "appointment_receipt"
    val REPORTS_SPA = "reports_spa"
    val REPORTS_USER = "reports_user"
}

object FireStoreDocumentField {
    val DATE = "date"
    val DATETIME = "dateTime"
}

object SharedPrefConstants {
    val LOCAL_SHARED_PREF = "local_shared_pref"
    val USER_SESSION = "user_session"
}