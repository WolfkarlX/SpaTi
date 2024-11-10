package com.example.spaTi.ui.checkappointments

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemCitaBinding
import com.example.spaTi.ui.appointments.AppointmentViewModel
import com.example.spaTi.util.hide
import java.text.SimpleDateFormat

class AppointmentListingAdapter(
    val onItemClicked: (Int, Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentListingAdapter.MyViewHolder>() {

    val sdf = SimpleDateFormat("dd MMM yyyy")
    private var list: MutableList<Appointment> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = ItemCitaBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        if (item.userId == "null" && item.spaId == "null" && item.serviceId == "null") {
            holder.bindEmptyList(item)
        } else {
            holder.bind(item)
        }
    }

    fun updateList(list: MutableList<Appointment>) {
        if (list.isEmpty()) {
            this.list = arrayListOf(Appointment(userId = "null", spaId = "null", serviceId = "null"))
            notifyDataSetChanged()
        } else {
            this.list = list
            notifyDataSetChanged()
        }
    }

    fun removeItem(position: Int){
        list.removeAt(position)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(val binding: ItemCitaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Appointment){
            binding.tvUsuario.setText(item.userId)
            binding.tvReportes.setText(item.spaId)
            binding.tvServicio.setText(item.serviceId)
            binding.tvFechaHora.setText(item.date + ", " + item.dateTime + "hrs")

            //binding.itemLayout.setOnClickListener { onItemClicked.invoke(adapterPosition,item) }
        }

        fun bindEmptyList(item: Appointment){
            binding.tvUsuario.textSize = 50F
            binding.tvUsuarioLabel.setText("")
            binding.tvUsuario.setText("")
            binding.tvReportesLabel.setText("")
            binding.tvReportes.setText("")
            binding.tvServicioLabel.setText("")
            binding.tvServicio.setText("")
            binding.tvFechaHoraLabel.setText("")
            binding.tvFechaHora.setText("")
            binding.tvUsuario.setText("NO HAY SOLICITUDES DE CITAS")
            binding.btnAceptarCita.hide()
            binding.btnRechazarCita.hide()
        }
    }
}