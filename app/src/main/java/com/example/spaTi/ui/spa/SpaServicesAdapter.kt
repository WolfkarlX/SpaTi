package com.example.spaTi.ui.spa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Service
import com.example.spaTi.databinding.ItemBookServiceBinding


class SpaServicesAdapter (
    val onItemClicked: (Int, Service?) -> Unit
) : RecyclerView.Adapter<SpaServicesAdapter.MyViewHolder>() {

    private var items: MutableList<Service> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpaServicesAdapter.MyViewHolder {
        val itemView = ItemBookServiceBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SpaServicesAdapter.MyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(list: MutableList<Service>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class MyViewHolder (
        val binding: ItemBookServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: Service) {
            binding.itemBookServiceName.text = item.name
            binding.itemBookServicePrice.text = String.format("%.2f MXN", item.price)
        }
    }
}