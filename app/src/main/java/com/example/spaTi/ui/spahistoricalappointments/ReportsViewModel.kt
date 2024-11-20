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

    /*UPDATE HERE TO CHECK IF THE ITEM YOU CLICKED HAS A REPORT FROM YOU
   ,AND IF IT IS, DISCOUNT BY 1 THE USERS REPORTS COUNT, AND IF NOT, ADD BY 1
   THE USERS REPORTS COUNT, YOU CAN DO IT BY CALLING 2 DIFFERENT METHODS OR FUNCTIONS
   ONE TO ADD ONE AND ADDING A REPORT ON THE DB AND
   ANOTHER TO DISCOUNT ONE AND DELETE THE REPORT YOU MADE
   TO CHECK IF THIS ITEM IS WAS CLICKED YOU CAN GET IT FROM THE ITEM INFORMATION ON THE FRAGMENT
     */
    fun createReportfromSpa(Report: Report) {
        _createReport.value = UiState.Loading
        repository.createReportfromSpa(Report) { result ->
            _createReport.value = result
        }
    }
}