package com.example.duriannet.presentation.seller_locator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.duriannet.R
import com.example.duriannet.models.Seller
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val seller = marker.tag as? Seller ?: return null

        // 2. Inflate view and set title, address and rating
        val view = LayoutInflater.from(context).inflate(R.layout.item_marker_info, null)
        view.findViewById<TextView>(R.id.text_view_title).text = seller.name
        view.findViewById<TextView>(R.id.text_view_address).text = seller.description
        view.findViewById<TextView>(R.id.text_view_rating).text = "Rating: %.2f".format(seller.rating)
        view.findViewById<TextView>(R.id.text_view_durian_types).text = seller.durianTypes.joinToString(", ")

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}