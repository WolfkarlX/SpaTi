package com.example.spaTi.ui.checkappointments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemCitaBinding
import com.example.spaTi.util.hide
import java.text.SimpleDateFormat

class AppointmentListingAdapter(
    val onItemClicked: (Int, Appointment, Int) -> Unit
) : RecyclerView.Adapter<AppointmentListingAdapter.MyViewHolder>() {

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

    fun removeItem(position: Int) {
        if (position >= 0 && position < list.size) {
            list.removeAt(position)
            notifyItemRemoved(position)
            if (list.isEmpty()) {
                updateList(arrayListOf(Appointment(userId = "null", spaId = "null", serviceId = "null")))
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(val binding: ItemCitaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Appointment){
            binding.tvUsuario.setText(item.userId)
            binding.tvReportes.setText(item.spaId)
            binding.tvServicio.setText(item.serviceId)
            binding.tvSexo.setText(item.id)
            binding.tvFechaHora.setText(item.date + ", " + item.dateTime + "hrs")

            binding.btnAceptarCita.setOnClickListener { onItemClicked.invoke(adapterPosition,item, 1)
            }

            binding.btnRechazarCita.setOnClickListener { onItemClicked.invoke(adapterPosition,item, 0)
            }
        }

        fun bindEmptyList(item: Appointment){
            binding.tvUsuario.textSize = 50F
            //binding.tvUsuarioLabel.setText("")
            binding.tvUsuario.setText("")
            binding.tvReportesLabel.setText("")
            binding.tvReportes.setText("")
            //binding.tvServicioLabel.setText("")
            binding.tvServicio.setText("")
            //binding.tvFechaHoraLabel.setText("")
            binding.tvFechaHora.setText("")
            binding.tvUsuario.setText("NO HAY SOLICITUDES DE CITAS")
            binding.btnAceptarCita.hide()
            binding.btnRechazarCita.hide()
        }
    }
}