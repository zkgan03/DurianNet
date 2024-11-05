package com.example.duriannet.services.common

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Util
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import android.provider.MediaStore.Files.FileColumns
import android.provider.MediaStore.MediaColumns
import com.example.duriannet.models.MediaStoreData
import com.example.duriannet.models.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreDataSource(
    private val context: Context,
) {

    fun loadAllMediaStoreData(): Flow<List<MediaStoreData>> = callbackFlow {
        val contentObserver =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    launch { trySend(queryAllMediaData()) }
                }
            }

        context.contentResolver.registerContentObserver(
            MEDIA_STORE_FILE_URI,
            /* notifyForDescendants=*/ true,
            contentObserver
        )

        trySend(queryAllMediaData())

        awaitClose { context.contentResolver.unregisterContentObserver(contentObserver) }
    }

    fun loadImages(): Flow<List<MediaStoreData>> = callbackFlow {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                launch { trySend(queryImages()) }
            }
        }

        // Register the content observer to listen for changes in the MediaStore.
        context.contentResolver.registerContentObserver(
            MEDIA_STORE_FILE_URI,
            /* notifyForDescendants=*/ true,
            contentObserver
        )

        // Send the initial data set.
        trySend(queryImages())

        // Unregister the content observer when the flow is closed.
        awaitClose {
            context.contentResolver.unregisterContentObserver(contentObserver)
        }
    }

    private fun queryAllMediaData(): MutableList<MediaStoreData> {
        Preconditions.checkArgument(
            Util.isOnBackgroundThread(),
            "Can only query from a background thread"
        )
        val data: MutableList<MediaStoreData> = ArrayList()
        val contentResolver = context.contentResolver
            .query(
                MEDIA_STORE_FILE_URI,
                PROJECTION,
                FileColumns.MEDIA_TYPE +
                        " = " +
                        FileColumns.MEDIA_TYPE_IMAGE +
                        " OR " +
                        FileColumns.MEDIA_TYPE +
                        " = " +
                        FileColumns.MEDIA_TYPE_VIDEO,
                /* selectionArgs= */ null,
                "${MediaColumns.DATE_TAKEN} DESC"
            ) ?: return data

        contentResolver.use { cursor ->
            val idColNum = cursor.getColumnIndexOrThrow(MediaColumns._ID)
            val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaColumns.DATE_TAKEN)
            val dateModifiedColNum = cursor.getColumnIndexOrThrow(MediaColumns.DATE_MODIFIED)
            val mimeTypeColNum = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE)
            val orientationColNum = cursor.getColumnIndexOrThrow(MediaColumns.ORIENTATION)
            val mediaTypeColumnIndex = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
            val displayNameIndex = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColNum)
                val dateTaken = cursor.getLong(dateTakenColNum)
                val mimeType = cursor.getString(mimeTypeColNum)
                val dateModified = cursor.getLong(dateModifiedColNum)
                val orientation = cursor.getInt(orientationColNum)
                val displayName = cursor.getString(displayNameIndex)
                val mediaType = if (cursor.getInt(mediaTypeColumnIndex) == FileColumns.MEDIA_TYPE_IMAGE)
                    MediaType.IMAGE
                else
                    MediaType.VIDEO

                data.add(
                    MediaStoreData(
                        mediaType = mediaType,
                        rowId = id,
                        uri = Uri.withAppendedPath(MEDIA_STORE_FILE_URI, id.toString()),
                        mimeType = mimeType,
                        dateModified = dateModified,
                        orientation = orientation,
                        dateTaken = dateTaken,
                        displayName = displayName,
                    )
                )
            }
        }
        return data
    }

    private suspend fun queryImages(): MutableList<MediaStoreData> {
        return withContext(Dispatchers.IO) {

            Preconditions.checkArgument(Util.isOnBackgroundThread(), "Can only query from a background thread")
            val data: MutableList<MediaStoreData> = ArrayList()
            val contentResolver = context.contentResolver.query(
                MEDIA_STORE_FILE_URI,
                PROJECTION,
                FileColumns.MEDIA_TYPE +
                        " = " +
                        FileColumns.MEDIA_TYPE_IMAGE,
                null,
                "${MediaColumns.DATE_TAKEN} DESC"
            ) ?: return@withContext data

            contentResolver.use { cursor ->
                val idColNum = cursor.getColumnIndexOrThrow(MediaColumns._ID)
                val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaColumns.DATE_TAKEN)
                val dateModifiedColNum = cursor.getColumnIndexOrThrow(MediaColumns.DATE_MODIFIED)
                val mimeTypeColNum = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE)
                val orientationColNum = cursor.getColumnIndexOrThrow(MediaColumns.ORIENTATION)
                val mediaTypeColumnIndex = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
                val displayNameIndex = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColNum)
                    val dateTaken = cursor.getLong(dateTakenColNum)
                    val mimeType = cursor.getString(mimeTypeColNum)
                    val dateModified = cursor.getLong(dateModifiedColNum)
                    val orientation = cursor.getInt(orientationColNum)
                    val displayName = cursor.getString(displayNameIndex)
                    val mediaType = if (cursor.getInt(mediaTypeColumnIndex) == FileColumns.MEDIA_TYPE_IMAGE)
                        MediaType.IMAGE
                    else
                        MediaType.VIDEO

                    data.add(
                        MediaStoreData(
                            mediaType = mediaType,
                            rowId = id,
                            uri = Uri.withAppendedPath(MEDIA_STORE_FILE_URI, id.toString()),
                            mimeType = mimeType,
                            dateModified = dateModified,
                            orientation = orientation,
                            dateTaken = dateTaken,
                            displayName = displayName,
                        )
                    )
                }
            }
            return@withContext data
        }


    }


    companion object {
        private val MEDIA_STORE_FILE_URI = MediaStore.Files.getContentUri("external")
        private val PROJECTION =
            arrayOf(
                MediaColumns._ID,
                MediaColumns.DATE_TAKEN,
                MediaColumns.DATE_MODIFIED,
                MediaColumns.MIME_TYPE,
                MediaColumns.ORIENTATION,
                MediaColumns.DISPLAY_NAME,
                FileColumns.MEDIA_TYPE
            )
    }
}

