package com.example.spaTi.ui.appointments

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.spaTi.R
import java.time.LocalDate

class CalendarDayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dateText: TextView
    private var container: ConstraintLayout
    private var date: LocalDate? = null
    private var isDisabled = false
    private var onDateClickListener: ((LocalDate) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_day, this, true)
        dateText = findViewById(R.id.dateText)
        container = findViewById(R.id.container)

        isClickable = true
        isFocusable = true
        container.setBackgroundResource(R.drawable.calendar_view_day_background)

        setOnClickListener {
            if (!isDisabled) {
                date?.let { onDateClickListener?.invoke(it) }
            }
        }
    }

    fun setDate(date: LocalDate) {
        this.date = date
        dateText.text = date.dayOfMonth.toString()
    }

    fun markBusy() {
        if (!isDisabled) {
            dateText.setTextAppearance(R.style.CustomTextFontFamilyBold)
            dateText.setTextColor(ContextCompat.getColor(context, R.color.busy_day_txt))
            dateText.paint.strokeWidth = 4f // Adjust thickness
            dateText.paint.style = Paint.Style.STROKE
        }
    }

    fun markEmpty() {
        if (!isDisabled) {
            dateText.setTextAppearance(R.style.CustomTextFontFamilyBold)
            dateText.setTextColor(ContextCompat.getColor(context, R.color.empty_day_txt))
            dateText.paint.strokeWidth = 4f // Adjust thickness
            dateText.paint.style = Paint.Style.STROKE
        }
    }

    fun therearefewAppointments(){
        dateText.setTextAppearance(R.style.CustomTextFontFamilyBold)
        dateText.setTextColor(ContextCompat.getColor(context, R.color.colorOnlyOneAppointment))
        dateText.paint.strokeWidth = 4f // Adjust thickness
        dateText.paint.style = Paint.Style.STROKE
    }

    fun markDisable() {
        isDisabled = true
        isClickable = false
        dateText.alpha = 0.3f
        dateText.setTextColor(ContextCompat.getColor(context, R.color.disable_day_txt))
    }

    fun markSelected() {
        if (!isDisabled) {
            dateText.setTextAppearance(R.style.CustomTextFontFamilyBold)
            container.setBackgroundResource(R.drawable.calendar_view_day_background_selected)
        }
    }

    fun reset() {
        container.setBackgroundResource(R.drawable.calendar_view_day_background)
    }

    fun setOnDateClickListener(listener: (LocalDate) -> Unit) {
        onDateClickListener = listener
    }
}