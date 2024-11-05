package com.example.duriannet.models

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Seller(
    val sellerId: Int,
    val name: String,
    val description: String,
    val durianTypes: HashSet<DurianType>,
    val image: Bitmap,
    val rating: Float,
    val latLng: LatLng,
) : ClusterItem {
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String = name
    override fun getSnippet(): String = description
    override fun getZIndex(): Float = rating
}

enum class DurianType {
    MusangKing,
    D24,
    RedPrawn,
    BlackThorn,
}