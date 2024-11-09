package com.example.duriannet.presentation.seller_locator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.duriannet.R
import com.example.duriannet.databinding.ItemSearchResultBinding
import com.example.duriannet.models.Seller
import com.example.duriannet.services.common.GoogleMapManager

class SearchResultsAdapter(
    private var userLocation: Pair<Double, Double> = Pair(0.0, 0.0),
    private var onItemClicked: (Seller) -> Unit = {},
) : ListAdapter<Seller, SearchResultsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Seller>() {
    override fun areItemsTheSame(oldItem: Seller, newItem: Seller): Boolean {
        return oldItem.sellerId == newItem.sellerId
    }

    override fun areContentsTheSame(oldItem: Seller, newItem: Seller): Boolean {
        return oldItem == newItem
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchResult = getItem(position)
        holder.bind(searchResult)
    }

    fun updateUserLocation(userLocation: Pair<Double, Double>) {
        this.userLocation = userLocation
        notifyDataSetChanged()
    }

    fun setOnItemClickedListener(onItemClicked: (Seller) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemSearchResultBinding.bind(view)

        fun bind(seller: Seller) {
            binding.distanceFromCurrentLocation.text =
                String.format(
                    "%.2f km",
                    GoogleMapManager.calculateDistanceInMeter(
                        userLocation.first,
                        userLocation.second,
                        seller.latLng.latitude,
                        seller.latLng.longitude
                    ) / 1000.0f
                )

            binding.sellerName.text = seller.name
            binding.sellerAddress.text = GoogleMapManager.getAddress(
                itemView.context,
                seller.latLng.latitude,
                seller.latLng.longitude
            )
            binding.itemView.setOnClickListener {
                onItemClicked(seller)
            }
        }
    }
}
