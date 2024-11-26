package com.example.spaTi.ui.checkappointments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemCitaBinding
import com.example.spaTi.util.hide

class AppointmentListingAdapter(
    private val onItemClicked: (Int, Map<String, Any>, Int, String) -> Unit
) : RecyclerView.Adapter<AppointmentListingAdapter.MyViewHolder>() {

    private var list: List<Map<String, Any>> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCitaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        val appointment = item["appointment"] as? Appointment
        if (appointment == null || (appointment.userId == "null" && appointment.spaId == "null" && appointment.serviceId == "null")) {
            holder.bindEmptyList()
        } else {
            holder.bind(item)
        }
    }

    fun updateList(newList: List<Map<String, Any>>) {
        if (newList.isEmpty()) {
            this.list = listOf(
                mapOf(
                    "appointment" to Appointment(userId = "null", spaId = "null", serviceId = "null")
                )
            )
            notifyDataSetChanged()
        } else {
            this.list = newList
            notifyDataSetChanged()
        }
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < list.size) {
            list = list.toMutableList().apply {
                removeAt(position)
            }
            notifyItemRemoved(position)
            if (list.isEmpty()) {
                updateList(listOf(mapOf("appointment" to Appointment(userId = "null", spaId = "null", serviceId = "null"))))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(private val binding: ItemCitaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Map<String, Any>) {
            val appointment = item["appointment"] as Appointment
            binding.tvUsuario.text = item["userName"] as? String ?: "Unknown"
            binding.tvReportes.text = item["userReports"] as? String ?: "No Reports"
            binding.tvServicio.text = item["serviceName"] as? String ?: "Unknown Service"
            binding.tvSexo.text = item["userSex"] as? String ?: "Unknown Sex"
            binding.tvFechaHora.text = "${appointment.date}, ${appointment.dateTime}hrs"
            val haveReceive = (item["appointmentReceiptUrl"] as? String) == null
            binding.ivReceiptIcon.visibility = if (haveReceive) { View.GONE } else { View.VISIBLE }

            binding.btnRechazarCita.setOnClickListener { onItemClicked.invoke(adapterPosition, item, 0, "") }
            binding.btnAceptarCita.setOnClickListener { onItemClicked.invoke(adapterPosition, item, 1, "") }

            val receiptUri = (item["appointmentReceiptUrl"] as? String)
            val fileType = (item["appointmentReceiptType"] as? String)
            if (fileType != null && receiptUri != null && fileType == "img") {
                binding.ivReceiptIcon.setOnClickListener { onItemClicked.invoke(adapterPosition, item, 2, receiptUri) }
            } else if (fileType != null && receiptUri != null && fileType == "pdf") {
                binding.ivReceiptIcon.setOnClickListener { onItemClicked.invoke(adapterPosition, item, 3, receiptUri) }
            }
        }

        fun bindEmptyList() {
            binding.apply {

                tvUsuario.textSize = 30F
                tvUsuario.text = "NO HAY SOLICITUDES DE CITAS"
                tvReportesLabel.text = ""
                tvReportes.text = ""
                tvServicio.text = ""
                tvSexo.text = ""
                tvFechaHora.text = ""
                btnAceptarCita.hide()
                btnRechazarCita.hide()
                userIcon.hide()
                reportIcon.hide()
                sexIcon.hide()
                serviceIcon.hide()
                dateIcon.hide()
            }
        }
    }
}
