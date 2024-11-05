import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.core.math.MathUtils
import com.example.duriannet.models.DetectionResult
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.round

// refs : https://github.com/pytorch/android-demo-app/issues/259

/**
 * Image augmentations: first scale the image and later padding image,  increase the strength of the model.
 * Always this scale and padding will make the image object detection gave more high probability or more robust.
 *
 *
 * Reference:
 * https://github.com/ultralytics/yolov5/blob/db6ec66a602a0b64a7db1711acd064eda5daf2b3/utils/augmentations.py#L91-L122
 * def letterbox(im, new_shape=(640, 640), color=(114, 114, 114), auto=True, scaleFill=False, scaleup=True, stride=32):
 * method
 *
 * @param srcBitmap
 * @param newShape  (640*640)
 * @param color     always gray (114,114,114)
 * @param auto      default:false, no use
 * @param scaleFill default:false,  no use
 * @param scaleUp   default:false
 * @param stride    default:32 , no use
 * @return
 */
fun Bitmap.withLetterBox(
    newShape: Pair<Int, Int> = Pair(640, 640),
    color: Triple<Int, Int, Int> = Triple(114, 114, 114),
    auto: Boolean = false,
    scaleFill: Boolean = false,
    scaleUp: Boolean = false,
    center: Boolean = true,
    stride: Int = 32,
): Bitmap {
    // current shape
    var bitmapSrc = this
    val currentWidth = bitmapSrc.width
    val currentHeight = bitmapSrc.height

    // new shape eg: 640*640
    val newWidth = newShape.first
    val newHeight = newShape.second

    // only scale image，no padding,just return scale image
    // I modify this logic something difference with the python code clean & speed.
    if (scaleFill) {
        // filter =  bilinear filtering
        return Bitmap.createScaledBitmap(bitmapSrc, newWidth, newHeight, true)
    }

    // Scale ratio (new / old)
    var r = min(
        (newWidth * 1.0f / currentWidth).toDouble(),
        (newHeight * 1.0f / currentHeight).toDouble()
    ).toFloat()

    //  Only scale down, do not scale up (for better val mAP)
    if (!scaleUp) {
        r = min(r.toDouble(), 1.0).toFloat()
    }

    // new dimensions of the image after scaling but before adding any padding
    val newUnpadWidth = Math.round(currentWidth * r)
    val newUnpadHeight = Math.round(currentHeight * r)

    // resize if both size are not the same for both the original and the new image size
    // normally scale down the image
    if (!(currentWidth == newUnpadWidth && currentHeight == newUnpadHeight)) {
        bitmapSrc = Bitmap.createScaledBitmap(bitmapSrc, newUnpadWidth, newUnpadHeight, true)
    }

    //  wh padding
    var dw = newWidth - newUnpadWidth
    var dh = newHeight - newUnpadHeight

    // auto always false, no use for android demo
    if (auto) { // # wh padding
        dw %= stride
        dh %= stride
    }

    // padding with gray color
    val outBitmap = Bitmap.createBitmap(bitmapSrc.width + dw, bitmapSrc.height + dh, Bitmap.Config.ARGB_8888)
    val can = Canvas(outBitmap)
    can.drawRGB(color.first, color.second, color.third) // gray color

    if (center) {
        dw /= 2
        dh /= 2
    }
    // params : (Bitmap bitmap, float left, float top, Paint paint)
    can.drawBitmap(bitmapSrc, dw.toFloat(), dh.toFloat(), null)

    return outBitmap
}

fun Bitmap.toByteArray(): ByteArray {
    val bitmap = this
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

    return stream.toByteArray()
}

fun ByteArray.toBitmap(): Bitmap {
    return Bitmap.createBitmap(BitmapFactory.decodeByteArray(this, 0, this.size))
}

fun Bitmap.drawResults(
    results: Array<DetectionResult>,
    detectionSize: Pair<Int, Int>,
    showText: Boolean = true,
    strokeWidth: Float = 4f,
    @ColorInt textColor: Int = Color.WHITE,
    textSize: Float = 25f,
    @ColorInt backgroundColor: Int = Color.RED,
): Bitmap {
    val bitmap = this
    val canvas = Canvas(bitmap)

    val boxPaint = Paint()
    boxPaint.color = backgroundColor
    boxPaint.style = Paint.Style.STROKE
    boxPaint.strokeWidth = strokeWidth

    val txtBackgroundPaint = Paint()
    txtBackgroundPaint.color = backgroundColor
    txtBackgroundPaint.style = Paint.Style.FILL
    txtBackgroundPaint.textSize = textSize

    val textPaint = Paint()
    textPaint.color = textColor
    textPaint.style = Paint.Style.FILL
    textPaint.textSize = textSize

    val (detectionWidth, detectionHeight) = detectionSize
    val scaleFactor = min(detectionWidth * 1f / bitmap.width, detectionHeight * 1f / bitmap.height)
    val padWidth = (detectionWidth - bitmap.width * scaleFactor) / 2
    val padHeight = (detectionHeight - bitmap.height * scaleFactor) / 2

    val bound = Rect()

    for (result in results) {
        var top = ((result.top - padHeight) / scaleFactor)
        var left = ((result.left - padWidth) / scaleFactor)
        var bottom = (top + result.height / scaleFactor)
        var right = (left + result.width / scaleFactor)

        top = MathUtils.clamp(top, 0f, bitmap.height.toFloat())
        left = MathUtils.clamp(left, 0f, bitmap.width.toFloat())
        bottom = MathUtils.clamp(bottom, 0f, bitmap.height.toFloat())
        right = MathUtils.clamp(right, 0f, bitmap.width.toFloat())

        val rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        canvas.drawRect(rect, boxPaint)

        if (showText) {
            val text = "${result.label} ${round(result.confidence * 100).toInt()}%"
            txtBackgroundPaint.getTextBounds(text, 0, text.length, bound)

            val textWidth = bound.width()
            val textHeight = bound.height()

            canvas.drawRect(
                left,
                top,
                left + textWidth + 8,
                top + textHeight + 8,
                txtBackgroundPaint
            )

            canvas.drawText(text, left, top + bound.height(), textPaint)
        }
    }

    return bitmap
}
