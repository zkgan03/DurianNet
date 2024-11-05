package com.example.duriannet.presentation.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class WideMediaView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val newHeight = width * 1f / 9 * 16         //16:9 aspect ratio
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight.toInt(), MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}