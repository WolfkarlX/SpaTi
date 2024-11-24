package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.FireStoreDocumentField
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class AppointmentRepositoryImpl (
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : AppointmentRepository {

    override fun getAppointments(result: (UiState<List<Appointment>>) -> Unit) {
        database.collection(FireStoreCollection.APPOINTMENT)
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.ASCENDING)
            .orderBy(FireStoreDocumentField.DATETIME, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { appointmentDocs ->
                val appointments = arrayListOf<Appointment>()
                val tasks = mutableListOf<Task<DocumentSnapshot>>()

                for (document in appointmentDocs) {
                    val appointment = document.toObject(Appointment::class.java)
                    appointments.add(appointment)

                    // Fetch user data based on userId
                    val userTask = database.collection(FireStoreCollection.USER)
                        .document(appointment.userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                appointment.userId = userDoc.getString("first_name") ?: "Unknown"
                                appointment.spaId = userDoc.getString("reports") ?: "No reports"
                            }
                        }
                    tasks.add(userTask)

                    // Fetch service data based on serviceId
                    val serviceTask = database.collection(FireStoreCollection.SERVICE)
                        .document(appointment.serviceId)
                        .get()
                        .addOnSuccessListener { serviceDoc ->
                            if (serviceDoc.exists()) {
                                appointment.serviceId =
                                    serviceDoc.getString("name") ?: "Unknown Service"
                            }
                        }
                    tasks.add(serviceTask)
                }

                // Wait for all user and service data retrieval tasks to complete
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(appointments))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentBySpa(result: (UiState<List<Map<String, Any>>>) -> Unit) {
        val userStr = appPreferences.getString(SharedPrefConstants.USER_SESSION, null)
        val currentUser = userStr?.let { gson.fromJson(it, User::class.java) }

        if (currentUser == null) {
            result.invoke(UiState.Failure("User session not found"))
            return
        }

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("spaId", currentUser.id)
            .whereEqualTo("status", "pending")
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.ASCENDING)
            .orderBy(FireStoreDocumentField.DATETIME, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { appointmentDocs ->
                val appointmentsWithExtras = arrayListOf<Map<String, Any>>()
                val tasks = mutableListOf<Task<*>>()

                for (document in appointmentDocs) {
                    val appointment = document.toObject(Appointment::class.java)
                    val appointmentData = mutableMapOf<String, Any>(
                        "appointment" to appointment
                    )

                    // Fetch user data based on userId
                    val userTask = database.collection(FireStoreCollection.USER)
                        .document(appointment.userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                appointmentData["userName"] = userDoc.getString("first_name") ?: "Unknown"
                                appointmentData["userReports"] = userDoc.getString("reports") ?: "No reports"
                                appointmentData["userSex"] = userDoc.getString("sex") ?: "No sex"
                            }
                        }
                    tasks.add(userTask)

                    // Fetch service data based on serviceId
                    val serviceTask = database.collection(FireStoreCollection.SERVICE)
                        .document(appointment.serviceId)
                        .get()
                        .addOnSuccessListener { serviceDoc ->
                            if (serviceDoc.exists()) {
                                appointmentData["serviceName"] = serviceDoc.getString("name") ?: "Unknown Service"
                                appointmentData["serviceDescription"] = serviceDoc.getString("description") ?: "No Description"
                            }
                        }
                    tasks.add(serviceTask)

                    // Add the map with the appointment and additional data to the list
                    appointmentsWithExtras.add(appointmentData)
                }

                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(appointmentsWithExtras))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }


    override fun addAppointment(
        appointment: Appointment,
        result: (UiState<Pair<Appointment, String>>) -> Unit
    ) {
        val userStr = appPreferences.getString(SharedPrefConstants.USER_SESSION, null)
        var user: User? = null
        if (userStr == null) {
            result.invoke(UiState.Failure("Not session found"))
        } else {
            user = gson.fromJson(userStr, User::class.java)
        }

        val document = database.collection(FireStoreCollection.APPOINTMENT).document()
        appointment.id = document.id
        appointment.userId = user!!.id
        document
            .set(appointment)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(
                        Pair(
                            appointment,
                            "Appointment has been created successfully"
                        )
                    )
                )
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentsHistory(
        spaId: String,
        date: LocalDate,
        time: LocalTime,
        result: (UiState<List<Map<String, Any>>>) -> Unit
    ) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))

        val appointmentsWithExtras = arrayListOf<Map<String, Any>>()

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("spaId", spaId)
            .whereEqualTo("status", "accepted")
            .whereLessThan("date", dateString)
            .whereLessThanOrEqualTo("dateTime", timeString)
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.ASCENDING)
            .orderBy(FireStoreDocumentField.DATETIME, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { appointmentDocs ->
                val tasks = mutableListOf<Task<*>>()

                for (document in appointmentDocs) {
                    val appointment = document.toObject(Appointment::class.java)
                    val appointmentData = mutableMapOf<String, Any>(
                        "appointment" to appointment
                    )

                    // Fetch user data
                    val userTask = database.collection(FireStoreCollection.USER)
                        .document(appointment.userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                appointmentData["userName"] = userDoc.getString("first_name") ?: "Unknown"
                                appointmentData["userEmail"] = userDoc.getString("email") ?: "No Email"
                                appointmentData["userCellphone"] = userDoc.getString("cellphone") ?: "No Cellphone"
                                appointmentData["userSex"] = userDoc.getString("sex") ?: "No sex"
                                appointmentData["userReports"] = userDoc.getString("reports") ?: "No Reports"
                            }
                        }
                    tasks.add(userTask)

                    // Fetch service data
                    val serviceTask = database.collection(FireStoreCollection.SERVICE)
                        .document(appointment.serviceId)
                        .get()
                        .addOnSuccessListener { serviceDoc ->
                            if (serviceDoc.exists()) {
                                appointmentData["serviceName"] = serviceDoc.getString("name") ?: "Unknown Service"
                                appointmentData["serviceDescription"] = serviceDoc.getString("description") ?: "No Description"
                            }
                        }
                    tasks.add(serviceTask)

                    val reportsTask = database.collection(FireStoreCollection.REPORTS_SPA)
                        .whereEqualTo("spaId", spaId)
                        .whereEqualTo("userId", appointment.userId)
                        .get()
                        .continueWith { task ->
                            if (task.isSuccessful) {
                                val reportDocs = task.result
                                if (reportDocs != null && !reportDocs.isEmpty) {
                                    // Report exists, extract details
                                    val report = reportDocs.documents.first()
                                    appointmentData["reportedBySpa"] = true
                                    appointmentData["reportReason"] = report.getString("Reason") ?: "No reason provided"
                                } else {
                                    appointmentData["reportedBySpa"] = false
                                }
                            } else {
                                appointmentData["reportedBySpa"] = false // Default in case of failure
                            }
                            null // Return Void to match Task<Void>
                        }
                    tasks.add(reportsTask)
                    // Add the map with the appointment and additional data to the list
                    appointmentsWithExtras.add(appointmentData)
                }

                // Wait for all tasks to complete
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(appointmentsWithExtras))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentsByDate(
        spaId: String,
        date: LocalDate,
        result: (UiState<List<Appointment>>) -> Unit
    ) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("date", dateString)
            .whereEqualTo("spaId", spaId)
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                }
                result.invoke((UiState.Success(appointments)))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentsByDateOnAppointmentsSchedule(
        spaId: String,
        date: LocalDate,
        result: (UiState<List<Map<String, Any>>>) -> Unit // Modify result type to List of Maps
    ) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val appointmentsWithExtras = arrayListOf<Map<String, Any>>() // List of maps to hold additional data

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("spaId", spaId)
            .whereEqualTo("status", "accepted")
            .whereEqualTo("date", dateString)
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.ASCENDING)
            .orderBy(FireStoreDocumentField.DATETIME, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { appointmentDocs ->
                val tasks = mutableListOf<Task<DocumentSnapshot>>()

                for (document in appointmentDocs) {
                    val appointment = document.toObject(Appointment::class.java)
                    val appointmentData = mutableMapOf<String, Any>(
                        "appointment" to appointment // Store the appointment object in the map
                    )

                    // Fetch user data based on userId
                    val userTask = database.collection(FireStoreCollection.USER)
                        .document(appointment.userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                // Add additional user data to the map
                                appointmentData["userName"] = userDoc.getString("first_name") ?: "Unknown"
                                appointmentData["userReports"] = userDoc.getString("reports") ?: "No reports"
                                appointmentData["userEmail"] = userDoc.getString("email") ?: "No Email"
                                appointmentData["userCellphone"] = userDoc.getString("cellphone") ?: "No Cellphone"
                                appointmentData["userSex"] = userDoc.getString("sex") ?: "No sex"
                            }
                        }
                    tasks.add(userTask)

                    // Fetch service data based on serviceId
                    val serviceTask = database.collection(FireStoreCollection.SERVICE)
                        .document(appointment.serviceId)
                        .get()
                        .addOnSuccessListener { serviceDoc ->
                            if (serviceDoc.exists()) {
                                // Add additional service data to the map
                                appointmentData["serviceName"] = serviceDoc.getString("name") ?: "Unknown Service"
                                appointmentData["serviceDescription"] = serviceDoc.getString("description") ?: "No Description"
                            }
                        }
                    tasks.add(serviceTask)

                    // Add the map with the appointment and additional data to the list
                    appointmentsWithExtras.add(appointmentData)
                }

                // Wait until all user and service tasks complete
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(appointmentsWithExtras))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }




    override fun getAppointmentByMonth(
        spaId: String,
        yearMonth: YearMonth,
        result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit
    ) {
        val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsByDate = documents
                    .mapNotNull { it.toObject(Appointment::class.java) }
                    .filter { appointment -> appointment.spaId == spaId && appointment.status in listOf("accepted", "pending") }
                    .groupBy { appointment -> LocalDate.parse(appointment.date) }

                result.invoke(UiState.Success(appointmentsByDate))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentByMonthOnAppointmentsSchedule(
        spaId: String,
        yearMonth: YearMonth,
        result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit
    ) {
        val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsByDate = documents
                    .mapNotNull { it.toObject(Appointment::class.java) }
                    .filter { appointment -> appointment.spaId == spaId }
                    .groupBy { appointment -> LocalDate.parse(appointment.date) }

                result.invoke(UiState.Success(appointmentsByDate))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun setAppointmentDeclined(appointmentId: String, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.APPOINTMENT)
            .document(appointmentId)
            .update("status", "declined")
            .addOnSuccessListener {
                result.invoke(UiState.Success("Appointment declined successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun setAppointmentAccepted(appointmentId: String, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.APPOINTMENT)
            .document(appointmentId)
            .update("status", "accepted")
            .addOnSuccessListener {
                result.invoke(UiState.Success("Appointment accepted successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun setAppointmentCanceled(appointmentId: String, result: (UiState<String>) -> Unit) {
        database.collection(FireStoreCollection.APPOINTMENT)
            .document(appointmentId)
            .update("status", "canceled")
            .addOnSuccessListener {
                result.invoke(UiState.Success("Appointment canceled successfully"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun checkPendingAppointments(spaId: String, result: (UiState<String>) -> Unit) {
        val now = System.currentTimeMillis()
        val oneDayInMillis = TimeUnit.HOURS.toMillis(24)

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("spaId", spaId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    result.invoke(UiState.Success("No pending appointments found."))
                }

                val batch = database.batch()
                var anyUpdates = false

                for (document in querySnapshot.documents) {
                    val appointment = document.toObject<Appointment>()
                    appointment?.let {
                        val createdAt = it.createdAt
                        val status = it.status

                        if (createdAt != null) {
                            val createdAtMillis = createdAt.time

                            // Only update if status is "pending" and 24 hours have passed
                            if (status == "pending" && (now - createdAtMillis) >= oneDayInMillis) {
                                val appointmentRef = database.collection("appointment").document(document.id)
                                batch.update(appointmentRef, "status", "out of time")
                                anyUpdates = true
                            }
                        } else {
                            result.invoke(UiState.Failure("Couldnt get the appointments"))
                        }
                    }
                }

                // Commit the batch update if there are documents to update
                if (anyUpdates) {
                    batch.commit()
                        .addOnSuccessListener {
                            result.invoke(UiState.Success("Pending appointments checked and updated"))
                        }
                        .addOnFailureListener { e ->
                            result.invoke(UiState.Failure(e.localizedMessage))
                        }
                } else {
                    result.invoke(UiState.Success("No updates needed for pending appointments"))
                }
            }
            .addOnFailureListener { e ->
                result.invoke(UiState.Failure(e.localizedMessage))
            }
    }

    override fun getAppointmentByMonthAndUser(
        userId: String,
        yearMonth: YearMonth,
        result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit
    ) {
        val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsByDate = documents
                    .mapNotNull { it.toObject(Appointment::class.java) }
                    .filter { appointment -> appointment.userId == userId && appointment.status in listOf("accepted", "pending") }
                    .groupBy { appointment -> LocalDate.parse(appointment.date) }

                result.invoke(UiState.Success(appointmentsByDate))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }

    override fun getAppointmentsByDateAndUser(
        userId: String,
        date: LocalDate,
        result: (UiState<List<Map<String, Any>>>) -> Unit // Modify result type to List of Maps
    ) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val appointmentsWithExtras = arrayListOf<Map<String, Any>>() // List of maps to hold additional data

        database.collection(FireStoreCollection.APPOINTMENT)
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf("accepted", "pending"))
            .whereEqualTo("date", dateString)
            .orderBy(FireStoreDocumentField.DATE, Query.Direction.ASCENDING)
            .orderBy(FireStoreDocumentField.DATETIME, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { appointmentDocs ->
                val tasks = mutableListOf<Task<DocumentSnapshot>>()

                for (document in appointmentDocs) {
                    val appointment = document.toObject(Appointment::class.java)
                    val appointmentData = mutableMapOf<String, Any>(
                        "appointment" to appointment // Store the appointment object in the map
                    )

                    // Fetch user data based on userId
                    val spaTask = database.collection(FireStoreCollection.SPA)
                        .document(appointment.spaId)
                        .get()
                        .addOnSuccessListener { spaDoc ->
                            if (spaDoc.exists()) {
                                // Add additional user data to the map
                                appointmentData["spaName"] = spaDoc.getString("spa_name") ?: "Unknown"
                                appointmentData["spaEmail"] = spaDoc.getString("email") ?: "No Email"
                                appointmentData["spaCellphone"] = spaDoc.getString("cellphone") ?: "No Cellphone"
                            }
                        }
                    tasks.add(spaTask)

                    // Fetch service data based on serviceId
                    val serviceTask = database.collection(FireStoreCollection.SERVICE)
                        .document(appointment.serviceId)
                        .get()
                        .addOnSuccessListener { serviceDoc ->
                            if (serviceDoc.exists()) {
                                val dateEnd = LocalTime.parse(appointment.dateTime, DateTimeFormatter.ofPattern("HH:mm"))
                                        .plusMinutes(serviceDoc.get("durationMinutes").toString().toLong())
                                // Add additional service data to the map
                                appointmentData["serviceName"] = serviceDoc.getString("name") ?: "Unknown Service"
                                appointmentData["serviceDurationMinutes"] = if (serviceDoc.get("durationMinutes") != null) {
                                    dateEnd.toString() // Convert LocalTime to a String if needed
                                } else {
                                    "Unknown Service"
                                }

                            }
                        }
                    tasks.add(serviceTask)

                    // Add the map with the appointment and additional data to the list
                    appointmentsWithExtras.add(appointmentData)
                }

                // Wait until all user and service tasks complete
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        result.invoke(UiState.Success(appointmentsWithExtras))
                    }
                    .addOnFailureListener {
                        result.invoke(UiState.Failure(it.localizedMessage))
                    }
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }
}