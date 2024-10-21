package com.example.spaTi.ui.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.R
import com.example.spaTi.data.models.Service
import com.example.spaTi.databinding.ItemServiceBinding

/**
 * ServicesAdapter is a RecyclerView adapter that displays a list of services
 * and handles click events on the service items. It allows for updating
 * the displayed list and managing individual service items.
 *
 * Key features:
 * - Binds service data to the view holder.
 * - Supports an "Add New Service" option.
 * - Handles item clicks to navigate to service detail or create a new service.
 *
 * Lifecycle methods:
 * - [onCreateViewHolder]: Inflates each item’s layout.
 * - [onBindViewHolder]: Binds each service to the view or shows an option to add a new service.
 * - [getItemCount]: Returns the number of items plus one for the "add new" option.
 * - [updateList]: Updates the list of services when data changes.
 * - [removeItem]: Removes an item from the list and updates the display.
 * Inner Class:
 * - [MyViewHolder]: Manages individual item views.
 *   - [MyViewHolder.bind]: Binds a service’s data to the item view.
 *   - [MyViewHolder.bindAddNew]: Configures the view for the "add new service" option.
 */
class ServicesAdapter (
    val onItemClicked: (Int, Service?) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.MyViewHolder>() {

    private var items: MutableList<Service> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServicesAdapter.MyViewHolder {
        val itemView = ItemServiceBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServicesAdapter.MyViewHolder, position: Int) {
        if ( position < items.size) {
            holder.bind(items[position])
        } else {
            holder.bindAddNew()
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    fun updateList(list: MutableList<Service>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemChanged(position)
    }

    inner class MyViewHolder(val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Service) {
            binding.name.text = item.name
            binding.itemLayout.setCardBackgroundColor(binding.root.context.getColor(R.color.white))
            binding.name.setTextColor(binding.root.context.getColor(R.color.verde))
            // You can set an image for the service here
            binding.itemLayout.setOnClickListener { onItemClicked.invoke(adapterPosition, item) }
        }

        fun bindAddNew() {
            binding.name.text = "Agregar Servicio"
            binding.itemLayout.setCardBackgroundColor(binding.root.context.getColor(R.color.verde))
            binding.name.setTextColor(binding.root.context.getColor(R.color.white))
            // Set a plus icon or any other indicator for adding a new service
            binding.itemLayout.setOnClickListener { onItemClicked.invoke(adapterPosition, null) }
        }
    }
}