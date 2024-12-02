package com.example.duriannet.presentation.account_management.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R

//Profile Fragment
class FavoriteDurianAdapter(
    private val clickListener: (Int) -> Unit
) : RecyclerView.Adapter<FavoriteDurianAdapter.FavoriteDurianViewHolder>() {

    private val durians = mutableListOf<Pair<Int, String>>() // Pair of (durianId, durianName)

    fun submitList(newDurians: List<Pair<Int, String>>) {
        durians.clear()
        durians.addAll(newDurians)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteDurianViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_durian_selected, parent, false)
        return FavoriteDurianViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteDurianViewHolder, position: Int) {
        val (durianId, durianName) = durians[position]
        holder.bind(durianName)
        holder.itemView.setOnClickListener {
            clickListener(durianId) // Pass the durianId to the clickListener
        }
    }

    override fun getItemCount(): Int = durians.size

    class FavoriteDurianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val durianName: TextView = itemView.findViewById(R.id.favorite_durian_name)

        fun bind(name: String) {
            durianName.text = name
        }
    }
}

