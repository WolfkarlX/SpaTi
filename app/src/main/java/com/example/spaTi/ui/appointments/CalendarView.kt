package com.example.spaTi.ui.appointments

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.spaTi.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private val dayViews = mutableMapOf<LocalDate, CalendarDayView>()
    private var currentMonth = YearMonth.now()
    private var monthTextView: TextView? = null
    private val today = LocalDate.now()
    private var onDateClickListener: ((LocalDate) -> Unit)? = null

    init {
        rowCount = 7
        columnCount = 7
        setupDayHeaders()
        setupCalendar()
    }
    private fun setupDayHeaders() {
        val days = listOf("L", "M", "M", "J", "V", "S", "D")
        days.forEachIndexed { index, day ->
            val headerView = TextView(context).apply {
                text = day
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.disable_day_txt))
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 8)
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(index, 1f)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.calendar_row_spacing)
                rowSpec = GridLayout.spec(0)
            }

            addView(headerView, params)
        }
    }

    private fun setupCalendar() {
        for (i in childCount - 1 downTo 7) {
            removeViewAt(i)
        }
        dayViews.clear()

        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()

        // Update month text
        updateMonthText()

        // Calculate starting position based on the first day of month
        val firstDayOffset = firstDayOfMonth.dayOfWeek.value - 1

        // Add empty views for offset
        repeat(firstDayOffset) { index ->
            val emptyView = View(context)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(index, 1f)
                rowSpec = GridLayout.spec(1) // First row after headers
            }
            addView(emptyView, params)
        }

        var currentRow = 1
        var currentCol = firstDayOffset

        for (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
            val currentDate = currentMonth.atDay(dayOfMonth)
            val dayView = CalendarDayView(context)
            dayView.setDate(currentDate)

            dayView.setOnDateClickListener { date ->
                onDateClickListener?.invoke(date)
            }

            if (currentDate.isBefore(today)) {
                dayView.markDisable()
            }

            dayViews[currentDate] = dayView

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(currentCol, 1f)
                rowSpec = GridLayout.spec(currentRow)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.calendar_row_spacing)
            }

            addView(dayView, params)

            currentCol++
            if (currentCol == 7) {
                currentCol = 0
                currentRow++
            }
        }

        // Add empty views for remaining cells if needed
        val remainingCells = if (currentCol > 0) 7 - currentCol else 0
        repeat(remainingCells) { index ->
            val emptyView = View(context)
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(currentCol + index, 1f)
                rowSpec = GridLayout.spec(currentRow)
            }
            addView(emptyView, params)
        }
    }

    private fun updateMonthText() {
        if (monthTextView == null) {
            val rootView = parent as? ViewGroup
            monthTextView = rootView?.findViewById(R.id.appointmentCalendarMonth)
        }

        val monthAndYear = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}"
        monthTextView?.text = monthAndYear
    }

    fun markBusy(date: LocalDate) {
        dayViews[date]?.markBusy()
    }

    fun therearefeAppointments(date: LocalDate){
        dayViews[date]?.therearefewAppointments()
    }

    fun markDisable(date: LocalDate) {
        dayViews[date]?.markDisable()
    }

    fun markEmpty(date: LocalDate) {
        dayViews[date]?.markEmpty()
    }

    fun resetDate(date: LocalDate) {
        dayViews[date]?.reset()
    }

    fun markSelected(date: LocalDate) {
        dayViews[date]?.markSelected()
    }

    fun setMonth(yearMonth: YearMonth) {
        currentMonth = yearMonth
        setupCalendar()
    }

    fun setOnDateClickListener(listener: (LocalDate) -> Unit) {
        onDateClickListener = listener
    }
}