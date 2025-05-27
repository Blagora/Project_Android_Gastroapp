package com.example.gastroapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroapp.model.PlatoMenu
import com.example.gastroapp.databinding.ItemMenuPlatoBinding

class MenuAdapter : ListAdapter<PlatoMenu, MenuAdapter.PlatoViewHolder>(PlatoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val binding = ItemMenuPlatoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlatoViewHolder(private val binding: ItemMenuPlatoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plato: PlatoMenu) {
            binding.textViewNombrePlato.text = plato.nombrePlato
            binding.textViewDescripcionPlato.text = plato.descripcionPlato
            binding.textViewPrecioPlato.text = "${plato.precioPlato} COP" // Formatea como quieras
            binding.textViewSeccionPlato.text = plato.seccionPlato
        }
    }

    class PlatoDiffCallback : DiffUtil.ItemCallback<PlatoMenu>() {
        override fun areItemsTheSame(oldItem: PlatoMenu, newItem: PlatoMenu): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlatoMenu, newItem: PlatoMenu): Boolean {
            return oldItem == newItem
        }
    }
}