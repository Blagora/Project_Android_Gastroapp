package com.example.gastroapp.presentation.events.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gastroapp.R
import com.example.gastroapp.databinding.ItemEventBinding
import com.example.gastroapp.presentation.events.models.Event

class EventAdapter(private val onEventClick: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventDescription.text = event.description
                eventPrice.text = "Precio: ${event.price}"

                Glide.with(eventImage)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.perfil_vacio)
                    .error(R.drawable.perfil_vacio)
                    .centerCrop()
                    .into(eventImage)

                seeButton.setOnClickListener {
                    onEventClick(event)
                }

                root.setOnClickListener {
                    onEventClick(event)
                }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
} 