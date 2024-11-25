package com.example.spaTi.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.spaTi.data.models.Note
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay


class ReportRepositoryImpl(
    val database: FirebaseFirestore,
    private val tagRepository: TagRepository
) : ReportRepository {

    override fun createReportfromSpa(report: Report, result: (UiState<String>) -> Unit) {
        val userDocRef = database.collection(FireStoreCollection.USER).document(report.userId)

        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Safely retrieve the reports field as a String and convert to Long
                    val reportsString = documentSnapshot.getString("reports") ?: "0"
                    val reports = reportsString.toLongOrNull() ?: 0

                    if (reports >= 2) {
                        // Disable user and delete all appointments
                        userDocRef.update("status", "disabled")
                            .addOnSuccessListener {
                                deleteAllAppointmentsOfUser(report.userId) { deleteResult ->
                                    when (deleteResult) {
                                        is UiState.Success -> {
                                            // Increment the report counter and create the report
                                            IncrementReportUserCounter(report.userId) { incrementResult ->
                                                when (incrementResult) {
                                                    is UiState.Success -> {
                                                        addReportFromSpa(report) { reportResult ->
                                                            when (reportResult) {
                                                                is UiState.Success -> {
                                                                    result.invoke(UiState.Success("User disabled, all appointments deleted, report created, and report count incremented successfully"))
                                                                }
                                                                is UiState.Failure -> {
                                                                    result.invoke(UiState.Failure("User disabled and appointments deleted, but failed to create report: ${reportResult.error}"))
                                                                }

                                                                else -> {}
                                                            }
                                                        }
                                                    }
                                                    is UiState.Failure -> {
                                                        result.invoke(UiState.Failure("User disabled and appointments deleted, but failed to increment report count: ${incrementResult.error}"))
                                                    }

                                                    else -> {}
                                                }
                                            }
                                        }
                                        is UiState.Failure -> {
                                            result.invoke(UiState.Failure("User disabled but failed to delete appointments: ${deleteResult.error}"))
                                        }

                                        else -> {}
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                result.invoke(UiState.Failure("Failed to disable the user: ${exception.localizedMessage}"))
                            }
                    } else {
                        // Add a new report and increment the report count
                        addReportFromSpa(report) { reportResult ->
                            when (reportResult) {
                                is UiState.Success -> {
                                    IncrementReportUserCounter(report.userId) { incrementResult ->
                                        when (incrementResult) {
                                            is UiState.Success -> {
                                                result.invoke(UiState.Success("Report added and user report count incremented successfully"))
                                            }
                                            is UiState.Failure -> {
                                                result.invoke(UiState.Failure("Report added but failed to increment user report count: ${incrementResult.error}"))
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                                is UiState.Failure -> {
                                    result.invoke(UiState.Failure("Failed to add the report: ${reportResult.error}"))
                                }

                                else -> {}
                            }
                        }
                    }
                } else {
                    result.invoke(UiState.Failure("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure("Error fetching user details: ${exception.localizedMessage}"))
            }
    }

    private fun addReportFromSpa(report: Report, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.REPORTS_SPA).document()
        report.id = document.id

        document.set(report)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Report created successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure("Failed to create the report: ${exception.localizedMessage}"))
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

    fun deleteAllAppointmentsOfUser(userId: String, result: (UiState<String>) -> Unit) {
        val appointmentsQuery = database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("userId", userId)

        appointmentsQuery.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = database.batch()
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        result.invoke(UiState.Success("All appointments of the user deleted successfully"))
                    }
                    .addOnFailureListener { exception ->
                        result.invoke(UiState.Failure(exception.localizedMessage ?: "Failed to delete appointments"))
                    }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage ?: "Error fetching appointments"))
            }
    }

    override fun reportSpaAction(report: Report, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.REPORTS_USER)
            .whereEqualTo("userId", report.userId)
            .whereEqualTo("spaId", report.spaId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No report exists, create a new one
                    createReportFromUser(report, result)
                } else {
                    // Report exists, delete it
                    val existingReport = querySnapshot.documents[0].toObject(Report::class.java)
                    existingReport?.let {
                        deleteReportFromUser(it, result)
                    } ?: result.invoke(UiState.Failure("Failed to parse existing report"))
                }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(
                    "Failed to check existing reports: ${it.localizedMessage}"
                ))
            }
    }

    override fun createReportFromUser(report: Report, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.REPORTS_USER).document()
        report.id = document.id

        document.set(report)
            .addOnSuccessListener {
                updateReportSpa(report.spaId) { updateResult ->
                    when (updateResult) {
                        is UiState.Success -> {
                            result.invoke(UiState.Success("Report created successfully"))
                        }
                        is UiState.Failure -> {
                            result.invoke(UiState.Failure("Report created but failed to update spa: ${updateResult.error}"))
                        }
                        else -> {}
                    }
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure("Failed to create the report: ${exception.localizedMessage}"))
            }
    }

    override fun deleteReportFromUser(report: Report, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.REPORTS_USER).document(report.id)
            .delete()
            .addOnSuccessListener {
                decrementReportSpa(report.spaId) { updateResult ->
                    when (updateResult) {
                        is UiState.Success -> {
                            result.invoke(UiState.Success("Report successfully deleted"))
                        }
                        is UiState.Failure -> {
                            result.invoke(UiState.Failure("Report deleted but failed to update spa: ${updateResult.error}"))
                        }
                        else -> {}
                    }
                }
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.message))
            }
    }

    private fun updateReportSpa(spaId: String, result: (UiState<String>) -> Unit) {
        val spaRef = database.collection(FireStoreCollection.SPA).document(spaId)

        // First, get all services and appointments for this spa
        val servicesTask = database.collection(FireStoreCollection.SERVICE)
            .whereEqualTo("spaId", spaId)
            .get()

        val appointmentsTask = database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("spaId", spaId)
            .get()

        Tasks.whenAllSuccess<Any>(servicesTask, appointmentsTask)
            .continueWithTask { task ->
                val serviceSnapshot = servicesTask.result
                val appointmentSnapshot = appointmentsTask.result

                database.runTransaction { transaction ->
                    // STEP 1: Perform ALL reads first
                    // Read the spa document
                    val spaSnapshot = transaction.get(spaRef)
                    val currentCount = spaSnapshot.getString("reports")?.toIntOrNull() ?: 0
                    val newCount = currentCount + 1

                    // Collect all tags that need to be read and updated
                    val tagsToUpdate = mutableMapOf<String, Int>()
                    val tagSnapshots = mutableMapOf<String, DocumentSnapshot>()

                    // Process services to collect unique tags
                    serviceSnapshot.documents.forEach { serviceDoc ->
                        val service = serviceDoc.toObject(Service::class.java)
                        service?.tags?.forEach { tagId ->
                            tagsToUpdate[tagId] = (tagsToUpdate[tagId] ?: 0) - 1
                        }
                    }

                    // Read all tag documents before any writes
                    tagsToUpdate.keys.forEach { tagId ->
                        val tagRef = database.collection(FireStoreCollection.TAG).document(tagId)
                        tagSnapshots[tagId] = transaction.get(tagRef)
                    }

                    // STEP 2: Perform ALL writes after completing all reads
                    // Update spa report count
                    transaction.update(spaRef, "reports", newCount.toString())

                    // If threshold reached, perform all the updates
                    if (newCount >= 3) {
                        // Update spa status to disabled
                        transaction.update(spaRef, "status", "disabled")

                        // Delete all services
                        serviceSnapshot.documents.forEach { serviceDoc ->
                            transaction.delete(serviceDoc.reference)
                        }

                        // Update all tag counts
                        tagsToUpdate.forEach { (tagId, countChange) ->
                            val tagSnapshot = tagSnapshots[tagId]!!
                            val currentTagCount = tagSnapshot.getLong("relatedCount") ?: 0
                            val newTagCount = (currentTagCount + countChange).coerceAtLeast(0)
                            val tagRef = database.collection(FireStoreCollection.TAG).document(tagId)
                            transaction.update(tagRef, "relatedCount", newTagCount)
                        }

                        // Delete all appointments
                        appointmentSnapshot.documents.forEach { appointmentDoc ->
                            transaction.delete(appointmentDoc.reference)
                        }
                    }
                }
            }
            .addOnSuccessListener {
                result.invoke(UiState.Success("Spa report count updated successfully"))
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure("Failed to update spa report count: ${e.localizedMessage}"))
                Log.e("ReportRepository", "Error updating report spa count", e)
            }
    }

    private fun decrementReportSpa(spaId: String, result: (UiState<String>) -> Unit) {
        val spaRef = database.collection(FireStoreCollection.SPA).document(spaId)

        database.runTransaction { transaction ->
            val snapshot = transaction.get(spaRef)
            val currentCount = snapshot.getString("reports")?.toIntOrNull() ?: 0
            val newCount = (currentCount - 1).coerceAtLeast(0)

            transaction.update(spaRef, "reports", newCount.toString())
        }.addOnSuccessListener {
            result.invoke(UiState.Success("Spa report count decremented successfully"))
        }.addOnFailureListener { e ->
            result.invoke(UiState.Failure("Failed to decrement spa report count: ${e.localizedMessage}"))
            Log.e("ReportRepository", "Error updating report spa count", e)
        }
    }
}