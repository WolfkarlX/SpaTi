package com.example.spaTi.ui.homeUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.R
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemAppointmentsAcceptedBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserAppointmentsAdapter (
    val onItemClicked: (Int, Appointment, Int, String) -> Unit
) : RecyclerView.Adapter<UserAppointmentsAdapter.MyViewHolder>() {
    private var list: MutableList<Map<String, Any>> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = ItemAppointmentsAcceptedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        val appointment = item["appointment"] as Appointment
        if (appointment.userId == "null" && appointment.spaId == "null" && appointment.serviceId == "null") {
            holder.bindEmptyList(appointment)
        } else {
            holder.bind(item)
        }
    }

    fun updateList(newList: List<Map<String, Any>>) {
        if (newList.isEmpty()) {
            this.list = arrayListOf(
                mapOf("appointment" to Appointment(userId = "null", spaId = "null", serviceId = "null"))
            )
        } else {
            this.list = newList.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < list.size) {
            list.removeAt(position)
            notifyItemRemoved(position)
            if (list.isEmpty()) {
                updateList(listOf(mapOf("appointment" to Appointment(userId = "null", spaId = "null", serviceId = "null"))))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun clearAppointments() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: ItemAppointmentsAcceptedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Map<String, Any>) {
            val appointment = item["appointment"] as Appointment

            val isHistoryTab = when (item["reportedByUser"]) {
                null -> null
                else -> item["reportedByUser"] as Boolean
            }

            if (isHistoryTab == null) {
                // Tab is "In progress"
                binding.itemAppointmentNoAppointments.visibility = View.GONE
                binding.itemAppointmentUserCard.visibility = View.VISIBLE
                binding.btnCancelAppointment.visibility = View.VISIBLE
                binding.btnGoToService.visibility = View.VISIBLE
            } else {
                // Tab is "History"
                binding.itemAppointmentNoAppointments.visibility = View.GONE
                binding.btnCancelAppointment.visibility = View.GONE
                binding.btnGoToService.visibility = View.GONE
                binding.itemAppointmentUserCard.visibility = View.VISIBLE
                binding.btnReport.visibility = View.VISIBLE
                binding.btnReportLabel.visibility = View.VISIBLE

                binding.itemAppointmentReportIcon.visibility = View.VISIBLE
                binding.itemAppointmentReportLabel.visibility = View.VISIBLE
                binding.itemAppointmentReportText.visibility = View.VISIBLE
                binding.itemAppointmentReportText.text = item["spaReports"] as? String

                if (isHistoryTab == true) {
                    binding.btnReport.setImageResource(R.drawable.report_active)
                } else {
                    binding.btnReport.setImageResource(R.drawable.report_inactive)
                }
            }


            // make visible icon views
            binding.itemAppointmentStatusIcon.visibility = View.VISIBLE
            binding.itemAppointmentServiceIcon.visibility = View.VISIBLE
            binding.itemAppointmentDateIcon.visibility = View.VISIBLE
            binding.itemAppointmentTimeIcon.visibility = View.VISIBLE
            binding.itemAppointmentEmailIcon.visibility = View.VISIBLE
            binding.itemAppointmentPhoneIcon.visibility = View.VISIBLE
            val haveReceive = (item["appointmentReceiptUrl"] as? String) == null
            binding.itemAppointmentUserCardReceipt.visibility = if (haveReceive) { View.GONE } else { View.VISIBLE }

            // remove visible views
            binding.itemAppointmentStatusLabel.visibility = View.VISIBLE
            binding.itemAppointmentServiceLabel.visibility = View.VISIBLE
            binding.itemAppointmentDateLabel.visibility = View.VISIBLE
            binding.itemAppointmentTimeLabel.visibility = View.VISIBLE
            binding.itemAppointmentEmailLabel.visibility = View.VISIBLE
            binding.itemAppointmentPhoneLabel.visibility = View.VISIBLE

            binding.itemAppointmentUserCardTxt.text = item["spaName"] as? String
            binding.itemAppointmentServiceText.text = item["serviceName"] as? String
            binding.itemAppointmentDateText.text = if (appointment.date.isNotEmpty()) {
                LocalDate.parse(appointment.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
            } else {
                "Unknown Service"
            }
            binding.itemAppointmentTimeText.text = "${appointment.dateTime} - ${item["serviceDurationMinutes"]}"
            binding.itemAppointmentEmailText.text = item["spaEmail"] as? String
            binding.itemAppointmentPhoneText.text = item["spaCellphone"] as? String
            binding.itemAppointmentStatusText.text = appointment.status

            binding.btnCancelAppointment.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 1, "") }
            binding.btnGoToService.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 2, "") }

            if (isHistoryTab == false) { // report spa
                binding.btnReport.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 3, "") }
            } else if (isHistoryTab == true)  { // spa already reported, so remove it
                binding.btnReport.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 4, "") }
            }

            val receiptUri = (item["appointmentReceiptUrl"] as? String)
            val fileType = (item["appointmentReceiptType"] as? String)
            if (fileType != null && receiptUri != null && fileType == "img") {
                binding.itemAppointmentUserCardReceipt.setOnClickListener { onItemClicked.invoke(adapterPosition, appointment, 5, receiptUri) }
            } else if (fileType != null && receiptUri != null && fileType == "pdf") {
                binding.itemAppointmentUserCardReceipt.setOnClickListener { onItemClicked.invoke(adapterPosition, appointment, 6, receiptUri) }
            }
        }

        fun bindEmptyList(appointment: Appointment) {
            binding.itemAppointmentNoAppointments.visibility = View.VISIBLE
            binding.itemAppointmentUserCard.visibility = View.GONE

            binding.btnCancelAppointment.visibility = View.GONE
            binding.btnGoToService.visibility = View.GONE
            binding.btnReport.visibility = View.GONE

            // remove icon views
            binding.itemAppointmentStatusIcon.visibility = View.GONE
            binding.itemAppointmentServiceIcon.visibility = View.GONE
            binding.itemAppointmentDateIcon.visibility = View.GONE
            binding.itemAppointmentTimeIcon.visibility = View.GONE
            binding.itemAppointmentEmailIcon.visibility = View.GONE
            binding.itemAppointmentPhoneIcon.visibility = View.GONE
            binding.itemAppointmentUserCardReceipt.visibility = View.GONE

            // remove label views
            binding.itemAppointmentStatusLabel.visibility = View.GONE
            binding.itemAppointmentReportLabel.visibility = View.GONE
            binding.itemAppointmentServiceLabel.visibility = View.GONE
            binding.itemAppointmentDateLabel.visibility = View.GONE
            binding.itemAppointmentTimeLabel.visibility = View.GONE
            binding.itemAppointmentEmailLabel.visibility = View.GONE
            binding.itemAppointmentPhoneLabel.visibility = View.GONE
            binding.btnReportLabel.visibility = View.GONE

            // remove text views
            binding.itemAppointmentStatusText.text = ""
            binding.itemAppointmentReportText.text = ""
            binding.itemAppointmentServiceText.text = ""
            binding.itemAppointmentDateText.text = ""
            binding.itemAppointmentTimeText.text = ""
            binding.itemAppointmentEmailText.text = ""
            binding.itemAppointmentPhoneText.text = ""
        }
    }
}
