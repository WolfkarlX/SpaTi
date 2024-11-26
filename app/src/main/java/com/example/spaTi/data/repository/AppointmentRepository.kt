package com.example.spaTi.data.repository

import android.net.Uri
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

interface AppointmentRepository {
    fun getAppointments(result: (UiState<List<Appointment>>) -> Unit)
    fun addAppointment(appointment: Appointment, result: (UiState<Pair<Appointment, String>>) -> Unit)
    fun getAppointmentBySpa(result: (UiState<List<Map<String, Any>>>) -> Unit)
    fun getAppointmentsByDate(spaId: String, date: LocalDate, result: (UiState<List<Appointment>>) -> Unit)

    // Spa Appointments
    fun getAppointmentsByDateOnAppointmentsSchedule(spaId: String, date: LocalDate, result: (UiState<List<Map<String, Any>>>) -> Unit)
    fun getAppointmentByMonth(spaId: String, yearMonth: YearMonth, result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit)
    fun getAppointmentByMonthOnAppointmentsSchedule(spaId: String, yearMonth: YearMonth, result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit)
    fun setAppointmentDeclined(appointmentid: String, result: (UiState<String>) -> Unit)
    fun setAppointmentAccepted(appointmentid: String, result: (UiState<String>) -> Unit)
    fun checkPendingAppointments(spaId: String, result: (UiState<String>) -> Unit)
    fun getAppointmentsHistory(spaId: String, date: LocalDate, time: LocalTime, result: (UiState<List<Map<String, Any>>>) -> Unit)

    // User Appointments
    fun setAppointmentCanceled(appointmentId: String, result: (UiState<String>) -> Unit)
    fun getAppointmentByMonthAndUser(userId: String, yearMonth: YearMonth, result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit)
    fun getAppointmentsByDateAndUser(userId: String, date: LocalDate, result: (UiState<List<Map<String, Any>>>) -> Unit)
    fun getAppointmentsUserHistory(userId: String, result: (UiState<List<Map<String, Any>>>) -> Unit)

    fun addAppointmentWithReceipt(appointment: Appointment, receiptUri: Uri, fileType: String, result: (UiState<Pair<Appointment, String>>) -> Unit)
    fun uploadReceiptImg(appointment: Appointment, uri: Uri, result: (UiState<String>) -> Unit)
    fun uploadReceiptPdf(appointment: Appointment, uri: Uri, result: (UiState<String>) -> Unit)
}