package com.example.duriannet.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException

object Common {
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.contains("emulator") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86"))
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }


    fun hasPermissions(context: Context, allPermission: Array<String>): Boolean {
        return allPermission.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun saveBitmap(context: Context, displayName: String, bitmap: Bitmap): Uri? {
        val imageCollections = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.applicationContext.contentResolver
        val imageContentUri = resolver.insert(imageCollections, imageDetails) ?: return null

        return try {
            resolver.openOutputStream(imageContentUri, "w").use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os!!)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageDetails.clear()
                imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageContentUri, imageDetails, null, null)
            }

            imageContentUri
        } catch (e: FileNotFoundException) {
            // Some legacy devices won't create directory for the Uri if dir not exist, resulting in
            // a FileNotFoundException. To resolve this issue, we should use the File API to save the
            // image, which allows us to create the directory ourselves.
            null
        }
    }


}