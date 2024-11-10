package com.example.spaTi.ui.appointments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.data.repository.AppointmentRepository
import com.example.spaTi.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor (
    val repository: AppointmentRepository
) : ViewModel() {
    private val _appointments = MutableLiveData<UiState<List<Appointment>>>()
    val appointments: LiveData<UiState<List<Appointment>>>
        get() = _appointments

    private val _appointmentsBySpa = MutableLiveData<UiState<List<Appointment>>>()
    val appointmentsBySpa: LiveData<UiState<List<Appointment>>>
        get() = _appointmentsBySpa

    private val _addAppointment = MutableLiveData<UiState<Pair<Appointment, String>>>()
    val addAppointment: LiveData<UiState<Pair<Appointment, String>>>
        get() = _addAppointment

    private val _getAppointmentsByDate = MutableLiveData<UiState<List<Appointment>>>()
    val getAppointmentsByDate: LiveData<UiState<List<Appointment>>>
        get() = _getAppointmentsByDate

    private val _getAppointmentByMonth = MutableLiveData<UiState<Map<LocalDate, List<Appointment>>>>()
    val getAppointmentByMonth: LiveData<UiState<Map<LocalDate, List<Appointment>>>>
        get() = _getAppointmentByMonth


    fun getAppointments() {
        _appointments.value = UiState.Loading
        repository.getAppointments { _appointments.value = it }
    }

    fun getAppointmentsBySpa() {
        _appointmentsBySpa.value = UiState.Loading
        repository.getAppointmentBySpa { _appointmentsBySpa.value = it }
    }

    fun addAppointment(appointment: Appointment) {
        _addAppointment.value = UiState.Loading
        repository.addAppointment(appointment) { _addAppointment.value = it }
    }

    fun resetAddAppointmentState() {
        _addAppointment.value = UiState.Loading
    }

    fun getAppointmentsByDate(spaId: String, date: LocalDate) {
        _getAppointmentsByDate.value = UiState.Loading
        repository.getAppointmentsByDate(spaId, date) { _getAppointmentsByDate.value = it }
    }

    fun getAppointmentByMonth(spaId: String, yearMonth: YearMonth) {
        _getAppointmentByMonth.value = UiState.Loading
        repository.getAppointmentByMonth(spaId, yearMonth) { _getAppointmentByMonth.value = it }
    }

}