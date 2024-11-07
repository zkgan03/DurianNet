package com.example.duriannet.presentation.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import com.example.duriannet.R
import com.example.duriannet.models.DetectionResult
import kotlin.math.min
import kotlin.math.round

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: Array<DetectionResult> = arrayOf()

    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var padWidth: Float = 0f
    private var padHeight: Float = 0f

    private var imgWidth: Int = 0
    private var imgHeight: Int = 0

    private var offsetTop: Float = 0f
    private var offsetLeft: Float = 0f

    private var bounds = Rect()

    private var showText = true

    init {
        initPaints()
    }

    fun clear() {
        results = arrayOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()

        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = ContextCompat.getColor(context!!, R.color.accent_dark_green)
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.accent_dark_green)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

//        val testPaint = Paint().apply {
//            color = Color.RED
//            strokeWidth = 8F
//            style = Paint.Style.STROKE
//        }
//        canvas.drawRect(50f, 50f, 200f, 200f, testPaint)

//        Log.e("OverlayView", "offsetTop: $offsetTop, offsetLeft: $offsetLeft")
        for (result in results) {

//            Log.e("OverlayView", "result drawing: ${result.label}")
//            if (result.label.trim() != "potted plant") continue

            var top = ((result.top - padHeight) / scaleFactor) + offsetTop
            var left = ((result.left - padWidth) / scaleFactor) + offsetLeft
            var bottom = (top + result.height / scaleFactor)
            var right = (left + result.width / scaleFactor)

//            Log.e("OverlayView", "before Clamp : top: $top, left: $left, bottom: $bottom, right: $right")

//            val top = result.top * 1f / scaleFactor
//            val left = result.left * 1f / scaleFactor
//            val height = result.height * 1f / scaleFactor
//            val width = result.width * 1f / scaleFactor

            top = MathUtils.clamp(top, 0f, imgHeight.toFloat() + offsetTop)
            left = MathUtils.clamp(left, 0f, imgWidth.toFloat() + offsetLeft)
            bottom = MathUtils.clamp(bottom, 0f, imgHeight.toFloat() + offsetTop)
            right = MathUtils.clamp(right, 0f, imgWidth.toFloat() + offsetLeft)
//            top = MathUtils.clamp(top, 0f, this.height.toFloat())
//            left = MathUtils.clamp(left, 0f, this.width.toFloat())
//            bottom = MathUtils.clamp(bottom, 0f, this.height.toFloat())
//            right = MathUtils.clamp(right, 0f, this.width.toFloat())

//            Log.e("OverlayView", "After clamp : top: $top, left: $left, bottom: $bottom, right: $right")

            // Draw bounding box around detected objects
            canvas.drawRect(left, top, right, bottom, boxPaint)

            if (showText) {

                // Create text to display alongside detected objects
                val drawableText = "${result.label} ${round(result.confidence * 100).toInt()}%"

                // Draw rect behind display text (background of the label)
                // output to : bounds
                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
                val textWidth = bounds.width()
                val textHeight = bounds.height()

                canvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )

                // Draw text for detected object (draw label on top of the background rect)
                canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
            }

        }
    }

    fun setResults(
        detectionResults: Array<DetectionResult>,
        detectionSize: Pair<Int, Int>,
        oriSize: Pair<Int, Int>,
        offset: Pair<Float, Float> = Pair(0f, 0f),
        showText: Boolean = true,
    ) {
//        Log.e("OverlayView", "detect height: ${detectionSize.first}, detect width: ${detectionSize.second}")
//        Log.e("OverlayView", "ori height: ${oriSize.first}, ori width: ${oriSize.second}")
//        Log.e(
//            "OverlayView",
//            "first detection result: ${detectionResults[0].label}, ${detectionResults[0].confidence}, ${detectionResults[0].top}, ${detectionResults[0].left}, ${detectionResults[0].width}, ${detectionResults[0].height}"
//        )

        results = detectionResults
        this.showText = showText

        val (oriHeight, oriWidth) = oriSize
        val (detectionHeight, detectionWidth) = detectionSize
        val (offsetTop, offsetLeft) = offset

        this.offsetTop = offsetTop
        this.offsetLeft = offsetLeft

        this.imgWidth = oriWidth
        this.imgHeight = oriHeight

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.

        scaleFactor = min(detectionWidth * 1f / oriWidth, detectionHeight * 1f / oriHeight)

//        Log.e("OverlayView", "scaleFactor: $scaleFactor")

        padWidth = round((detectionWidth - oriWidth * scaleFactor) / 2)
        padHeight = round((detectionHeight - oriHeight * scaleFactor) / 2)


//        Log.e("OverlayView", "padWidth: $padWidth, padHeight: $padHeight")

        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}