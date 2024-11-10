package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.models.Spa
import com.example.spaTi.util.UiState
import java.time.LocalDate
import java.time.YearMonth

interface AppointmentRepository {
    fun getAppointments(result: (UiState<List<Appointment>>) -> Unit)
    fun addAppointment(appointment: Appointment, result: (UiState<Pair<Appointment, String>>) -> Unit)
//    fun updateAppointment(appointment: Appointment, result: (UiState<String>) -> Unit)
//    fun deleteAppointment(appointment: Appointment, result: (UiState<String>) -> Unit)
    fun getAppointmentBySpa(result: (UiState<List<Appointment>>) -> Unit)
    fun getAppointmentsByDate(spaId: String, date: LocalDate, result: (UiState<List<Appointment>>) -> Unit)
    fun getAppointmentByMonth(spaId: String, yearMonth: YearMonth, result: (UiState<Map<LocalDate, List<Appointment>>>) -> Unit)
}