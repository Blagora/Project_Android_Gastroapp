package com.example.gastroapp.presentation.events.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.gastroapp.R
import com.example.gastroapp.presentation.events.models.Event

class EventAdapter(
    private val events: List<Event>,
    private val onEventClicked: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        private val eventDescription: TextView = itemView.findViewById(R.id.eventDescription)
        private val eventPrice: TextView = itemView.findViewById(R.id.eventPrice)
        private val seeButton: Button = itemView.findViewById(R.id.seeButton)

        fun bind(event: Event) {
            eventTitle.text = event.title
            eventDescription.text = event.description
            eventPrice.text = "Precio: ${event.price}"

            // Cargar la imagen con Glide
            Glide.with(itemView.context)
                .load(event.imageUrl)
                .placeholder(R.drawable.placeholder_event)
                .error(R.drawable.placeholder_event)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
                .into(eventImage)

            seeButton.setOnClickListener {
                onEventClicked(event)
            }
        }
    }
} 