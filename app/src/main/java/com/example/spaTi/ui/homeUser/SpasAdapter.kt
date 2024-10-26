package com.example.spaTi.ui.homeUser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Spa
import com.example.spaTi.databinding.ItemSpaUserHomeBinding

class SpasAdapter (
    val onItemClicked: (Int, Spa?) -> Unit
) : RecyclerView.Adapter<SpasAdapter.MyViewHolder>() {

    private var items: MutableList<Spa> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : SpasAdapter.MyViewHolder {
        val itemView =  ItemSpaUserHomeBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SpasAdapter.MyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(list: MutableList<Spa>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class MyViewHolder (
        val binding: ItemSpaUserHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Spa) {
//            binding.itemSpaImageBackground.background = // Set the Image
            binding.itemSpaName.text = item.spa_name
            binding.itemSpaLocation.text = item.location

            itemView.setOnClickListener { onItemClicked(adapterPosition, item) }
        }
    }
}