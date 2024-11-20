package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.Note
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportRepositoryImpl(
    val database: FirebaseFirestore
) : ReportRepository {

    override fun createReportfromSpa(Report: Report, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.REPORTS_SPA).document()
        Report.id = document.id
        document
            .set(Report)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Report added successfully")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }
}