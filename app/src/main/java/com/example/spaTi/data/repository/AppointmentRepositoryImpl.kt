package com.example.spaTi.data.repository

import android.content.SharedPreferences
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.User
import com.example.spaTi.util.FireStoreCollection
import com.example.spaTi.util.SharedPrefConstants
import com.example.spaTi.util.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AppointmentRepositoryImpl (
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson
) : AppointmentRepository {
    override fun getAppointments(result: (UiState<List<Appointment>>) -> Unit) {
        TODO("Not yet implemented")
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
                result.invoke(UiState.Success(Pair(appointment, "Appointment has been created successfully")))
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
                    .filter { appointment -> appointment.spaId == spaId }
                    .groupBy { appointment -> LocalDate.parse(appointment.date) }

                result.invoke(UiState.Success(appointmentsByDate))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage))
            }
    }
}