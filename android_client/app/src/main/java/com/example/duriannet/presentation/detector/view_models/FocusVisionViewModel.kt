package com.example.duriannet.presentation.detector.view_models

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class FocusVisionViewModel : ViewModel() {
    private var imageResult: Bitmap? = null

    fun setImageResult(imageResult: Bitmap) {
        this.imageResult = imageResult
    }

    fun getImageResult(): Bitmap? {
        return imageResult
    }
}