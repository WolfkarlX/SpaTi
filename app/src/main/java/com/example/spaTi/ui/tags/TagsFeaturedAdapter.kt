package com.example.spaTi.ui.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Tag
import com.example.spaTi.databinding.ItemTagFeaturedBinding

class TagsFeaturedAdapter (
    val onItemClicked: (Int, Tag?) -> Unit
) : RecyclerView.Adapter<TagsFeaturedAdapter.MyViewHolder>() {

    private var items: MutableList<Tag> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : TagsFeaturedAdapter.MyViewHolder {
        val itemView = ItemTagFeaturedBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TagsFeaturedAdapter.MyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(list: MutableList<Tag>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemChanged(position)
    }

    inner class MyViewHolder(
        val binding: ItemTagFeaturedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Tag) {
            binding.tagName.text = item.name
            binding.relatedCountName.text = "usado en " + item.relatedCount.toString() + " servicios"

            itemView.setOnClickListener { onItemClicked(adapterPosition, item) }
        }
    }
}