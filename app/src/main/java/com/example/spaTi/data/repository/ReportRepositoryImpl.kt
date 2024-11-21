package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.Note
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportRepositoryImpl(
    val database: FirebaseFirestore
) : ReportRepository {

    override fun createReportfromSpa(Report: Report, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.REPORTS_SPA).document()
        Report.id = document.id

        document.set(Report)
            .addOnSuccessListener {
                // Once the report is added, increment the user's report counter
                IncrementReportUserCounter(Report.userId) { incrementResult ->
                    when (incrementResult) {
                        is UiState.Success -> {
                            result.invoke(UiState.Success("Report added successfully"))
                        }
                        is UiState.Failure -> {
                            result.invoke(UiState.Failure("Report added but failed to increment user counter: ${incrementResult.error}"))
                        }
                        else -> {}
                    }
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.localizedMessage ?: "Failed to add the report"
                    )
                )
            }
    }


    override fun deleteReportFromSpa(report: Report, result: (UiState<String>) -> Unit) {
        // Query the document in the REPORTS_SPA collection using userId and spaId
        val query = database.collection(FireStoreCollection.REPORTS_SPA)
            .whereEqualTo("userId", report.userId)
            .whereEqualTo("spaId", report.spaId)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming only one report matches the query
                    querySnapshot.documents.first().reference.delete()
                        .addOnSuccessListener {
                            // Once the report is deleted, decrement the user's report counter
                            DecrementReportUserCounter(report.userId) { decrementResult ->
                                when (decrementResult) {
                                    is UiState.Success -> {
                                        result.invoke(UiState.Success("Report deleted successfully"))
                                    }
                                    is UiState.Failure -> {
                                        result.invoke(UiState.Failure("Report deleted but failed to decrement user counter: ${decrementResult.error}"))
                                    }
                                    else -> {}
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            result.invoke(
                                UiState.Failure(
                                    exception.localizedMessage ?: "Failed to delete the report"
                                )
                            )
                        }
                } else {
                    result.invoke(
                        UiState.Failure("No matching report found for the given userId and spaId")
                    )
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.localizedMessage ?: "Error fetching the report"
                    )
                )
            }
    }

    override fun IncrementReportUserCounter(userId: String, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.USER).document(userId)

        document.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val currentCount = try {
                        snapshot.getString("reports")?.toLong() ?: 0L
                    } catch (e: NumberFormatException) {
                        0L
                    }
                    val updatedCount = currentCount + 1

                    document.update("reports", updatedCount.toString())
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Report counter incremented to $updatedCount successfully"))
                        }
                        .addOnFailureListener { exception ->
                            result.invoke(
                                UiState.Failure(
                                    exception.localizedMessage ?: "Failed to update report count"
                                )
                            )
                        }
                } else {
                    result.invoke(UiState.Failure("User not found or 'reports' field missing"))
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.localizedMessage ?: "Failed to fetch report data"
                    )
                )
            }
    }

    override fun DecrementReportUserCounter(userId: String, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.USER).document(userId)

        document.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val currentCount = try {
                        snapshot.getString("reports")?.toLong() ?: 0L
                    } catch (e: NumberFormatException) {
                        0L
                    }
                    val updatedCount = (currentCount - 1).coerceAtLeast(0) // Prevent negative values

                    document.update("reports", updatedCount.toString())
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Report counter decremented to $updatedCount successfully"))
                        }
                        .addOnFailureListener { exception ->
                            result.invoke(
                                UiState.Failure(
                                    exception.localizedMessage ?: "Failed to update report count"
                                )
                            )
                        }
                } else {
                    result.invoke(UiState.Failure("User not found or 'reports' field missing"))
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.localizedMessage ?: "Failed to fetch report data"
                    )
                )
            }
    }

}