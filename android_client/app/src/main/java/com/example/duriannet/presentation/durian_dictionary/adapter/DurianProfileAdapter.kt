package com.example.duriannet.presentation.durian_dictionary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duriannet.R
import com.example.duriannet.data.remote.dtos.response.DurianProfileForUserResponseDto

class DurianProfileAdapter(
    private val onViewDetailsClick: (DurianProfileForUserResponseDto) -> Unit
) : RecyclerView.Adapter<DurianProfileAdapter.ViewHolder>() {

    private val durians = mutableListOf<DurianProfileForUserResponseDto>()

    fun submitList(newList: List<DurianProfileForUserResponseDto>) {
        durians.clear()
        durians.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_durian_profile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(durians[position])
    }

    override fun getItemCount(): Int = durians.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val durianName: TextView = itemView.findViewById(R.id.durian_name)
        private val durianImage: ImageView = itemView.findViewById(R.id.iv_profile)
        private val viewDetailsButton: Button = itemView.findViewById(R.id.button2)

        fun bind(durian: DurianProfileForUserResponseDto) {
            durianName.text = durian.durianName
            Glide.with(itemView.context).load(durian.durianImage).into(durianImage)

            viewDetailsButton.setOnClickListener {
                onViewDetailsClick(durian)
            }
        }
    }
}
