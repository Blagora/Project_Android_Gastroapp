package com.example.gastroapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gastroapp.R
import com.example.gastroapp.databinding.ItemEventBinding
import com.example.gastroapp.model.Evento

class EventoAdapter(private val onItemClicked: (Evento) -> Unit) :
    ListAdapter<Evento, EventoAdapter.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventoViewHolder(
        private val binding: ItemEventBinding,
        private val onItemClicked: (Evento) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: Evento) {
            binding.eventTitle.text = evento.nombreEvento
            binding.eventDescription.text = evento.descripcionEvento

            if (evento.precioEvento.isNotEmpty() && evento.precioEvento.lowercase() != "0" && evento.precioEvento.lowercase() != "gratis") {
                binding.eventPrice.text = "Precio: ${evento.precioEvento} COP"
                binding.eventPrice.visibility = View.VISIBLE
            } else if (evento.precioEvento.lowercase() == "gratis") {
                binding.eventPrice.text = "Precio: Gratis"
                binding.eventPrice.visibility = View.VISIBLE
            }
            else {
                binding.eventPrice.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(evento.imagenUrl.ifEmpty { null })
                .placeholder(R.drawable.placeholder_restaurante)
                .error(R.drawable.placeholder_restaurante)
                .centerCrop() // Añadido para mejor ajuste de imagen
                .into(binding.eventImage) // Antes: imageViewEvento

            binding.seeButton.setOnClickListener { // Antes: buttonVerEvento
                onItemClicked(evento)
            }
            // Si quieres que todo el item sea clickeable:
            itemView.setOnClickListener { onItemClicked(evento) }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem == newItem
        }
    }
}