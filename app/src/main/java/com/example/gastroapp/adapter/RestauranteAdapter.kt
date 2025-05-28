package com.example.gastroapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.gastroapp.R
import com.example.gastroapp.model.HorarioDia
import com.example.gastroapp.model.Restaurante
import java.util.*

class   RestauranteAdapter(
    private val restaurantes: List<Restaurante>,
    private val onItemClick: (Restaurante) -> Unit,
    private val onReservarClick: (Restaurante) -> Unit
) : RecyclerView.Adapter<RestauranteAdapter.RestauranteViewHolder>() {

    // Configuración de Glide para reutilización
    private val requestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.placeholder_restaurante)
        .error(R.drawable.placeholder_restaurante)
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestauranteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurante, parent, false)
        return RestauranteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestauranteViewHolder, position: Int) {
        val restaurante = restaurantes[position]
        holder.bind(restaurante)
    }

    override fun getItemCount(): Int = restaurantes.size

    inner class RestauranteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgRestaurante: ImageView = itemView.findViewById(R.id.imgRestaurante)
        private val tvNombreRestaurante: TextView = itemView.findViewById(R.id.tvNombreRestaurante)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvCalificacion: TextView = itemView.findViewById(R.id.tvCalificacion)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        private val tvHorario: TextView = itemView.findViewById(R.id.tvHorario)
        private val btnReservar: Button = itemView.findViewById(R.id.btnReservar)

        fun bind(restaurante: Restaurante) {
            tvNombreRestaurante.text = restaurante.nombre
            ratingBar.rating = restaurante.calificacionPromedio
            tvCalificacion.text = String.format(Locale.US, "%.1f", restaurante.calificacionPromedio)
            tvDescripcion.text = restaurante.descripcion

            if (restaurante.galeriaImagenes.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(restaurante.galeriaImagenes[0])
                    .apply(requestOptions)
                    .thumbnail(0.25f)
                    .into(imgRestaurante)
            } else {
                imgRestaurante.setImageResource(R.drawable.placeholder_restaurante)
            }

            val currentDay = getCurrentDay()
            val horarioDia: HorarioDia? = restaurante.horario[currentDay]

            if (horarioDia != null && horarioDia.inicio.isNotEmpty() && horarioDia.fin.isNotEmpty()) {
                tvHorario.text = itemView.context.getString(R.string.horario_abierto_hoy, horarioDia.inicio, horarioDia.fin)
            } else {
                tvHorario.text = itemView.context.getString(R.string.horario_cerrado_hoy)
            }

            itemView.setOnClickListener { onItemClick(restaurante) }
            btnReservar.setOnClickListener { onReservarClick(restaurante) }
        }

        private fun getCurrentDay(): String {
            val calendar = Calendar.getInstance()
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "lunes"
                Calendar.TUESDAY -> "martes"
                Calendar.WEDNESDAY -> "miercoles"
                Calendar.THURSDAY -> "jueves"
                Calendar.FRIDAY -> "viernes"
                Calendar.SATURDAY -> "sabado"
                Calendar.SUNDAY -> "domingo"
                else -> "lunes"
            }
        }
    }
}