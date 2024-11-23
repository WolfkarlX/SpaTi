package com.example.spaTi.ui.homeUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemCitaAgendaBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserAppointmentsAdapter (
    val onItemClicked: (Int, Appointment, Int) -> Unit
) : RecyclerView.Adapter<UserAppointmentsAdapter.MyViewHolder>() {
    private var list: MutableList<Map<String, Any>> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = ItemCitaAgendaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class MyViewHolder(val binding: ItemCitaAgendaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Map<String, Any>) {
            val appointment = item["appointment"] as Appointment

            binding.noAppointments.visibility = View.INVISIBLE

            binding.btnRechazarCita.visibility = View.VISIBLE
            binding.btnGoToService.visibility = View.VISIBLE

            binding.tvUsuarioLabel.text = "Estado de Cita:"
            binding.tvUsuario.text = appointment.status

            binding.tvReportesLabel.text = "Spa:"
            binding.tvReportes.text = item["spaName"] as? String

            binding.tvServicioLabel.text = "Servicio:"
            binding.tvServicio.text = item["serviceName"] as? String

            binding.tvFechaHoraLabel.text = "Fecha:"
            binding.tvFechaHora.text = if (appointment.date.isNotEmpty()) {
                LocalDate.parse(appointment.date).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
            } else {
                "Unknown Service"
            }

            binding.tvCorreoLabel.text = "Hora:"
            binding.tvCorreo.text = "${appointment.dateTime} - ${item["serviceDurationMinutes"]}"

            binding.tvtelefonoLabel.text = "Correo:"
            binding.tvtelefono.text = item["spaEmail"] as? String

            binding.tvsexoLabel.text = "Telefono:"
            binding.tvsexo.text = item["spaCellphone"] as? String

            binding.btnRechazarCita.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 1) }
            binding.btnGoToService.setOnClickListener { onItemClicked.invoke(adapterPosition,appointment, 2) }
        }

        fun bindEmptyList(appointment: Appointment) {
            binding.noAppointments.text = "No hay citas para este dia"
            binding.noAppointments.visibility = View.VISIBLE

            binding.tvUsuarioLabel.text = ""
            binding.tvUsuario.text = ""

            binding.tvReportesLabel.text = ""
            binding.tvReportes.text = ""

            binding.tvServicioLabel.text = ""
            binding.tvServicio.text = ""

            binding.tvFechaHoraLabel.text = ""
            binding.tvFechaHora.text = ""

            binding.tvCorreoLabel.text = ""
            binding.tvCorreo.text = ""

            binding.tvtelefonoLabel.text = ""
            binding.tvtelefono.text = ""

            binding.tvsexoLabel.text = ""
            binding.tvsexo.text = ""

            binding.btnRechazarCita.visibility = View.GONE
        }
    }
}
