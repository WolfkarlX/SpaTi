package com.example.spaTi.ui.tags

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Tag
import com.example.spaTi.databinding.ItemTagSelectedBinding

class TagsSelectedAdapter (
    val onItemClicked: (Int, Tag?) -> Unit,
    private val hideCloseButton: Boolean = false,
    private val customTextColor: Int? = null,
    private val customBgColor: Int? = null,
    private val customStrokeColor: Int? = null
) : RecyclerView.Adapter<TagsSelectedAdapter.MyViewHolder>() {

    private var items: MutableList<Tag> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : TagsSelectedAdapter.MyViewHolder {
        val itemView = ItemTagSelectedBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TagsSelectedAdapter.MyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItems(): List<Tag> {
//        Log.d("TAGSSELECTEDADAPTER", items.toString())
        return items.toList()
    }

    fun addItem(tag: Tag) {
        if (!isExists(tag)) {
//            Log.d("TAGSSELECTEDADAPTER addItem", tag.name)
            items.add(tag)
            notifyItemInserted(items.size - 1)
        }
    }

    fun updateItems(list: MutableList<Tag>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemChanged(position)
    }

    fun updateTagWithId(updatedTag: Tag) {
        val position = items.indexOfFirst { it.name == updatedTag.name && it.id.isEmpty() }
        if (position != -1) {
            items[position] = updatedTag
            notifyItemChanged(position)
        }
    }

    fun isExists(tag: Tag): Boolean {
        return items.any { it == tag }
    }

    fun clearItems() {
        items = arrayListOf()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        val binding: ItemTagSelectedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Tag) {
            binding.tagName.text = item.name

            if (hideCloseButton) {
                binding.tagDelete.visibility = android.view.View.GONE
            } else {
                binding.tagDelete.visibility = android.view.View.VISIBLE
            }

            customTextColor?.let {
                binding.tagName.setTextColor(it)
            }
            customBgColor?.let {
                binding.itemLayout.setCardBackgroundColor(it)
            }
            customStrokeColor?.let {
                binding.itemLayout.strokeColor = it
            }

            itemView.setOnClickListener { onItemClicked(adapterPosition, item) }
        }
    }
}