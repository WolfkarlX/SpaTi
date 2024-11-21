package com.example.spaTi.data.repository

import com.example.spaTi.data.models.Report
import com.example.spaTi.util.UiState

interface ReportRepository {
    fun createReportfromSpa(Report: Report,  result: (UiState<String>) -> Unit)
    fun deleteReportFromSpa(report: Report, result: (UiState<String>) -> Unit)
    fun IncrementReportUserCounter(userId: String, result: (UiState<String>) -> Unit)
    fun DecrementReportUserCounter(userId: String, result: (UiState<String>) -> Unit)
}