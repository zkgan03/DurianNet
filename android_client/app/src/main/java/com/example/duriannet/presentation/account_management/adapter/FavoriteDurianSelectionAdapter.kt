package com.example.duriannet.presentation.account_management.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duriannet.R
import com.example.duriannet.data.remote.dtos.response.DurianProfileForUserResponseDto

class FavoriteDurianSelectionAdapter(
    private val onFavoriteChange: (DurianProfileForUserResponseDto, Boolean) -> Unit
) : RecyclerView.Adapter<FavoriteDurianSelectionAdapter.FavoriteDurianViewHolder>() {

    private val durianList = mutableListOf<DurianProfileForUserResponseDto>()
    private val favoriteSet = mutableSetOf<Int>() // To track favorited durians by ID

    fun submitList(durians: List<DurianProfileForUserResponseDto>, favorites: List<Int>) {
        durianList.clear()
        durianList.addAll(durians)
        favoriteSet.clear()
        favoriteSet.addAll(favorites)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteDurianViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_durian, parent, false)
        return FavoriteDurianViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteDurianViewHolder, position: Int) {
        val durian = durianList[position]
        holder.bind(durian, favoriteSet.contains(durian.durianId))
    }

    override fun getItemCount(): Int = durianList.size

    inner class FavoriteDurianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val durianName: TextView = itemView.findViewById(R.id.favorite_durian_name)
        private val durianImage: ImageView = itemView.findViewById(R.id.iv_favorite__profile)
        private val favoriteCheckbox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(durian: DurianProfileForUserResponseDto, isFavorite: Boolean) {
            durianName.text = durian.durianName
            Glide.with(itemView.context).load(durian.durianImage).into(durianImage)
            favoriteCheckbox.isChecked = isFavorite

            favoriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteChange(durian, isChecked)
            }
        }
    }
}
