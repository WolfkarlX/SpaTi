package com.example.spaTi.ui.homeUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spaTi.data.models.Service
import com.example.spaTi.data.models.Tag
import com.example.spaTi.databinding.ItemServiceBinding
import com.example.spaTi.databinding.ItemTagFeaturedBinding

class SearchAdapter  (
    private val onTagClicked: (Tag) -> Unit,
    private val onServiceClicked: (Service) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isAdapting: AdapterState = AdapterState.Tags
    private var servicesItems: MutableList<Service> = arrayListOf()
    private var tagsItems: MutableList<Tag> = arrayListOf()

    companion object {
        private const val VIEW_TYPE_TAG = 1
        private const val VIEW_TYPE_SERVICE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (isAdapting) {
            is AdapterState.Tags -> VIEW_TYPE_TAG
            is AdapterState.Services -> VIEW_TYPE_SERVICE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TAG -> TagViewHolder(
                ItemTagFeaturedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_SERVICE -> ServiceViewHolder(
                ItemServiceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class TagViewHolder(
        private val binding: ItemTagFeaturedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Tag) {
            binding.apply {
                tagAdd.visibility = View.GONE
                tagName.text = item.name
                relatedCountName.text = "Usado en ${item.relatedCount} servicios"

                root.setOnClickListener {
                    onTagClicked(item)
                }
            }
        }
    }

    inner class ServiceViewHolder(
        private val binding: ItemServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Service) {
            binding.apply {
                name.text = item.name

                root.setOnClickListener {
                    onServiceClicked(item)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TagViewHolder -> {
                val item = tagsItems[position]
                holder.bind(item)
            }
            is ServiceViewHolder -> {
                val item = servicesItems[position]
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int = when (isAdapting) {
        is AdapterState.Tags -> tagsItems.size
        is AdapterState.Services -> servicesItems.size
    }

    fun updateTagsList(tags: List<Tag>) {
        isAdapting = AdapterState.Tags
        tagsItems.clear()
        tagsItems.addAll(tags)
        notifyDataSetChanged()
    }

    fun updateServicesList(services: List<Service>) {
        isAdapting = AdapterState.Services
        servicesItems.clear()
        servicesItems.addAll(services)
        notifyDataSetChanged()
    }
}

sealed class AdapterState {
    object Services: AdapterState()
    object Tags: AdapterState()
}