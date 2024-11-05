package com.example.duriannet.models

import android.net.Uri

/** A data model containing data for a single media item. */
data class MediaStoreData(
    private val mediaType: MediaType,
    val rowId: Long,
    val uri: Uri,
    val mimeType: String?,
    val dateModified: Long,
    val orientation: Int,
    val dateTaken: Long,
    val displayName: String?,
)


/** The type of data. */
enum class MediaType {
    VIDEO,
    IMAGE
}
