package com.example.spaTi.ui.appointments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.R
import com.example.spaTi.databinding.ItemAppointmentScheduleBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleAdapter (
    val onItemClicked: (Int, String, Boolean, String?) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.MyViewHolder>() {
    private var timeSlots: MutableList<TimeSlot> = mutableListOf()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var occupiedRanges: List<Pair<LocalTime, LocalTime>> = listOf()
    private var spaEndTime: LocalTime? = null
    private var serviceDuration: Int = 0

    data class TimeSlot(
        val time: String,
        val isAvailable: Boolean = true
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScheduleAdapter.MyViewHolder {
        val itemView = ItemAppointmentScheduleBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    fun clearTimeSlots() {
        timeSlots.clear()
        notifyDataSetChanged()
    }

    fun setTimeRange(
        startTime: String,
        endTime: String,
        serviceDurationMinutes: Int,
        occupiedSlots: List<Pair<String, Int>>
    ) {
        timeSlots.clear()
        serviceDuration = serviceDurationMinutes
        spaEndTime = LocalTime.parse(endTime.trim(), timeFormatter)

        val start = LocalTime.parse(startTime.trim(), timeFormatter)
        val end = LocalTime.parse(endTime.trim(), timeFormatter)

        var currentTime = start
        if (currentTime.minute % 30 != 0) {
            currentTime = currentTime.plusMinutes(30L - (currentTime.minute % 30))
        }

        var adjustedEnd = end
        if (adjustedEnd.minute % 30 != 0) {
            adjustedEnd = adjustedEnd.minusMinutes(adjustedEnd.minute % 30L)
        }

        occupiedRanges = occupiedSlots.map { (time, duration) ->
            val slotStart = LocalTime.parse(time)
            val slotEnd = slotStart.plusMinutes(duration.toLong())
            slotStart to slotEnd
        }

        while (currentTime < adjustedEnd) {
            val slotEndTime = currentTime.plusMinutes(30)
            val isAvailable = occupiedRanges.none { (occupiedStart, occupiedEnd) ->
                currentTime < occupiedEnd && slotEndTime > occupiedStart
            }

            timeSlots.add(
                TimeSlot(
                    time = currentTime.format(timeFormatter),
                    isAvailable = isAvailable
                )
            )

            currentTime = slotEndTime
        }

        notifyDataSetChanged()
    }

    private fun validateTimeSlot(selectedTime: LocalTime): Pair<Boolean, String?> {
        // Check if service would extend beyond spa closing time
        val serviceEndTime = selectedTime.plusMinutes(serviceDuration.toLong())
        if (serviceEndTime > spaEndTime) {
            return Pair(false, "Service duration would extend beyond spa closing time")
        }

        // Check if service would overlap with any occupied slots
        val hasOverlap = occupiedRanges.any { (occupiedStart, occupiedEnd) ->
            selectedTime < occupiedEnd && serviceEndTime > occupiedStart
        }

        return if (hasOverlap) {
            Pair(false, "Selected time overlaps with an existing appointment")
        } else {
            Pair(true, null)
        }
    }

    inner class MyViewHolder(val binding: ItemAppointmentScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timeSlot: TimeSlot) {
            binding.itemAppointmentText.text = timeSlot.time

            // Set background color based on availability
            binding.itemAppointmentText.setBackgroundColor(
                if (timeSlot.isAvailable)
                    ContextCompat.getColor(itemView.context, R.color.date_time_available)
                else
                    ContextCompat.getColor(itemView.context, R.color.date_time_unavailable)
            )

            // Only allow clicks on available slots
            if (timeSlot.isAvailable) {
                itemView.setOnClickListener {
                    val selectedTime = LocalTime.parse(timeSlot.time, timeFormatter)
                    val (isValid, errorMessage) = validateTimeSlot(selectedTime)
                    onItemClicked(adapterPosition, timeSlot.time, isValid, errorMessage)
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }
}