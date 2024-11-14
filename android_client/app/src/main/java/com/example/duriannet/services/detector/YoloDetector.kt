package com.example.duriannet.services.detector

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.example.duriannet.utils.Constant
import com.example.duriannet.services.detector.utils.Common.applyNMS
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.services.detector.base.BaseDetector
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.enum.DelegateEnum
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import toBitmap
import withLetterBox
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class YoloDetector(
    private val context: Context,
    config: DetectorConfiguration = DetectorConfiguration(),
    detectorListener: IDetectorListener?,
) : BaseDetector(
    config = config,
    detectorListener = detectorListener
) {

    private var interpreter: Interpreter? = null
    private var labels = mutableListOf<String>()

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0f, 255f))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()


    init {
        start()
    }

    override fun start() {

        if (interpreter != null) return

        val modelPath = Constant.YOLOV8_MODEL_PATH
        val labelPath = Constant.YOLOV8_LABEL_PATH

        val options = if (config.currentDelegate == DelegateEnum.GPU) {
            val compatList = CompatibilityList()

            Interpreter.Options().apply {
                isCancellable = true
                if (compatList.isDelegateSupportedOnThisDevice) {
                    val delegateOptions = compatList.bestOptionsForThisDevice
                    this.addDelegate(GpuDelegate(delegateOptions))
                } else {
                    this.setNumThreads(4)
                }
            }
        } else {
            Interpreter.Options().apply {
                this.setNumThreads(4)
            }
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)

        /*
        * As per the YOLOv8 model, the output tensor shape [1, 84, 8400] can be interpreted as follows:

        * The first dimension (1) is the batch size.
        * This represents the number of images processed in each batch.

        * The second dimension (84),
        * in the case of 20 classes, consists of a set of values for each bounding box prediction.
        * In YOLOv8, each bounding box is represented by 4 coordinates and class probabilities.
        * So for 80 classes, we have 4 (coordinates) + 1 (object score) + 80 (class probabilities) = 85 values per bounding box.
        * 84 because indexing starts from 0

        * The third dimension (8400) is the total number of bounding box predictions the model makes per image.
        * This total number is accumulated from various scales of the image.

        * */
        // 4 (coordinates) + 80 (class probabilities) = 84 total values per BB

        val inputShape = interpreter?.getInputTensor(0)?.shape() ?: return
        val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return

        // input shape = [1, 640,640, 3] or [1, 320,320, 3]
        tensorWidth = inputShape[1]
        tensorHeight = inputShape[2]

        Log.e(TAG, "Width: $tensorWidth, Height: $tensorHeight, NumChannel: $numChannel, Predictions: $predictions")

        // output shape = [1, 84, 8400]
        numChannel = outputShape[1]
        predictions = outputShape[2]

        // open label file and populate labels into list
        try {
            val inputStream: InputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
        } catch (e: IOException) {
            currentStatus = DetectorStatusEnum.ERROR
            detectorListener?.onError(
                "Object detector failed to initialize. See error logs for details"
            )
            e.printStackTrace()
        }

        currentStatus = DetectorStatusEnum.INITIALIZED
        detectorListener?.onInitialized()
    }

    override fun stop() {
        currentStatus = DetectorStatusEnum.STOPPING
        detectorListener?.onStopped()
        interpreter = null
//        interpreter?.setCancelled(true)
//        interpreter?.close()
        currentStatus = DetectorStatusEnum.STOPPED

    }

    private fun detect(bitmap: Bitmap): Array<DetectionResult>? {
        interpreter ?: return null
        if (tensorWidth == 0) return null
        if (tensorHeight == 0) return null
        if (numChannel == 0) return null
        if (predictions == 0) return null

        // resize bitmap to model input size
        val resizedBitmap = bitmap.withLetterBox(Pair(tensorWidth, tensorHeight))

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, predictions), OUTPUT_IMAGE_TYPE)
        interpreter?.run(imageBuffer, output.buffer)

        // post process the output tensor
        val bestBoxes = postProcess(output.floatArray) ?: return emptyArray()

        // Empty detection

        val filteredBoxes = if (bestBoxes.size > config.maxNumberDetection) {
            // get only the top maxNumberDetection boxes
            bestBoxes.subList(0, config.maxNumberDetection)
        } else {
            bestBoxes
        }

        return filteredBoxes.toTypedArray()
    }

    override fun detectLiveStream(bitmap: Bitmap) {

        var inferenceTime = SystemClock.uptimeMillis() // start time

        val detectionResults = this.detect(bitmap) ?: return

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime // end time

        if (detectionResults.isEmpty()) {
            detectorListener?.onEmptyDetect()
        }

        detectorListener?.onDetect(
            results = detectionResults,
            inferenceTime = inferenceTime,
            detectWidth = tensorWidth,
            detectHeight = tensorHeight,
            inputImage = bitmap
        )
    }

    override fun detectImage(bytes: ByteArray): Triple<Array<DetectionResult>, Int, Int> {
        //convert to bitmap
        val bmp = bytes.toBitmap()
        return detectImage(bmp)

    }

    override fun detectImage(bitmap: Bitmap): Triple<Array<DetectionResult>, Int, Int> {

        val detectionResults = this.detect(bitmap) ?: return Triple(emptyArray(), 0, 0)

        return Triple(detectionResults, tensorWidth, tensorHeight)
    }

    override fun updateListener(detectorListener: IDetectorListener?) {
        this.detectorListener = detectorListener
    }


    private fun postProcess(output: FloatArray): List<DetectionResult>? {

        /*
        * output : output tensor will be in flatten array format where (84x8400)
        * each bounding box prediction will has all classes probabilities
        * */

        // A mutable list to store the bounding boxes that pass the confidence threshold and other checks.
        val results = mutableListOf<DetectionResult>()

        // numElements (predictions) = 8400 / 2100 (total number of bounding box predictions the model makes per image)
        for (c in 0 until predictions) {
            var maxConf = 0f // Stores the maximum confidence score found (initialized to a predefined confidence threshold)
            var maxIdx = -1 // Stores the index of the class with the highest confidence score

            // get the highest confidence score among all classes and the relevant class index
            // first 4 values are bounding box coordinates (0-3)
            // the rest are class probabilities (4-83)
            for (j in 4 until numChannel) {
//                Log.e("output", output[c + j * predictions].toString());
                if (output[c + j * predictions] > maxConf) {
                    maxConf = output[c + j * predictions]
                    maxIdx = j - 4
                }
            }

            // If the maximum confidence score is greater than the predefined confidence threshold,
            if (maxConf > config.cnfThreshold) {
                val clsName = labels[maxIdx] // get the current index class name

                val cx = output[c] // get the center x value
                val cy = output[c + predictions] // get the center y value
                val w = output[c + predictions * 2] // get the width value
                val h = output[c + predictions * 3] // get the height value

                // bounding box coordinates
                val x1 = cx - (w / 2F)
                val y1 = cy - (h / 2F)
                val x2 = cx + (w / 2F)
                val y2 = cy + (h / 2F)

                // check if bounding box is within image bounds
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

//                Log.e(TAG, "Confidence: $maxConf, Label: $clsName, Bounding Box: $x1, $y1, $x2, $y2, w: $w, h: $h")

                // add bounding box to list
                results.add(
                    DetectionResult(
                        label = clsName,
                        confidence = maxConf.toDouble(),
                        top = (y1 * tensorHeight).toInt(),
                        left = (x1 * tensorWidth).toInt(),
                        width = ((x2 - x1) * tensorWidth).toInt(),
                        height = ((y2 - y1) * tensorHeight).toInt()
                    )
                )
            }
        }

        if (results.isEmpty()) return null

        return applyNMS(results, config.iouThreshold)
    }


    companion object {
        private const val TAG = "YoloDetector"
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        const val DETECT_IMG_SIZE = 320
    }


}