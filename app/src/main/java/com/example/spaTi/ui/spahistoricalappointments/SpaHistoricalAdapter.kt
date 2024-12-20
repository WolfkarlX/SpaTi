import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.R
import com.example.spaTi.R.drawable.report_inactive
import com.example.spaTi.R.layout.item_cita_history
import com.example.spaTi.data.models.Appointment
import com.example.spaTi.databinding.ItemCitaHistoryBinding

class SpaHistoricalAdapter(
    val onItemClicked: (Int, Appointment, Int, Boolean, String) -> Unit
) : RecyclerView.Adapter<SpaHistoricalAdapter.MyViewHolder>() {

    private var list: MutableList<Map<String, Any>> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = ItemCitaHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class MyViewHolder(val binding: ItemCitaHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Map<String, Any>) {
            val appointment = item["appointment"] as Appointment
            val userWasReported:Boolean =  item["reportedBySpa"] as Boolean

            if (userWasReported) {
                binding.btnReport.setImageResource(R.drawable.report_active) // Replace with your drawable resource
            } else {
                binding.btnReport.setImageResource(report_inactive)// Replace with your default drawable
            }

            binding.tvUsuario.text = item["userName"] as? String
            binding.tvReportes.text = item["userReports"] as? String
            binding.tvServicio.text = item["serviceName"] as? String
            binding.tvFechaHora.text = "${appointment.date}, ${appointment.dateTime} hrs"
            binding.tvCorreo.text = item["userEmail"] as? String
            binding.tvtelefono.text = item["userCellphone"] as? String
            binding.tvSexo.text = item["userSex"] as? String
            binding.tvReportes.text = item["userReports"] as? String
            val haveReceive = (item["appointmentReceiptUrl"] as? String) == null
            binding.ivReceiptIcon.visibility = if (haveReceive) { View.GONE } else { View.VISIBLE }

            binding.btnReport.setOnClickListener { onItemClicked.invoke(adapterPosition, appointment, 1, userWasReported, "") }

            val receiptUri = (item["appointmentReceiptUrl"] as? String)
            val fileType = (item["appointmentReceiptType"] as? String)
            if (fileType != null && receiptUri != null && fileType == "img") {
                binding.ivReceiptIcon.setOnClickListener { onItemClicked.invoke(adapterPosition, appointment, 2, false, receiptUri) }
            } else if (fileType != null && receiptUri != null && fileType == "pdf") {
                binding.ivReceiptIcon.setOnClickListener { onItemClicked.invoke(adapterPosition, appointment, 3, false, receiptUri) }
            }
        }

        fun bindEmptyList(appointment: Appointment) {
            binding.tvUsuario.textSize = 50F
            //binding.tvUsuarioLabel.text = ""
            binding.tvUsuario.text = "NO HAY SOLICITUDES DE CITAS"
            binding.tvReportesLabel.text = ""
            binding.tvReportes.text = ""
            //binding.tvServicioLabel.text = ""
            binding.tvServicio.text = ""
            //binding.tvFechaHoraLabel.text = ""
            binding.tvFechaHora.text = ""
        }
    }
}
