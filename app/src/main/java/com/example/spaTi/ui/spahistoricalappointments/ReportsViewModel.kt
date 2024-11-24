package com.example.spaTi.ui.spahistoricalappointments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spaTi.data.models.Report
import com.example.spaTi.data.models.Spa
import com.example.spaTi.data.models.User
import com.example.spaTi.data.repository.AuthRepository
import com.example.spaTi.data.repository.ReportRepository
import com.example.spaTi.util.UiState
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    val repository: ReportRepository
): ViewModel() {

    private val _createReport = MutableLiveData<UiState<String>>()
    val createReport: LiveData<UiState<String>>
        get() = _createReport

    fun createReportfromSpa(Report: Report, UserWasReported: Boolean) {
        if(!UserWasReported){
            _createReport.value = UiState.Loading
            repository.createReportfromSpa(Report) { result ->
                _createReport.value = result
            }
        }else{
            _createReport.value = UiState.Loading
            repository.deleteReportFromSpa(Report) { result ->
                _createReport.value = result
            }
        }
    }
}