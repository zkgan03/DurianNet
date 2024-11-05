package com.example.duriannet.services.detector

import android.graphics.Bitmap
import android.util.Log
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.base.BaseDetector
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.hub_dto.DetectionResultDto
import com.example.duriannet.services.detector.hub_dto.toDetectionResult
import com.example.duriannet.utils.Constant.SERVER_BASE_URL
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.microsoft.signalr.messagepack.MessagePackHubProtocol
import toByteArray
import withLetterBox
import java.util.concurrent.ConcurrentHashMap

class DetectionHub(
    config: DetectorConfiguration = DetectorConfiguration(),
    detectorListener: IDetectorListener?,
) : BaseDetector(
    config = config,
    detectorListener = detectorListener
) {

    private val hubConnection = HubConnectionBuilder
        .create(HUB_URL)
        .withServerTimeout(600000)
        .withKeepAliveInterval(10000)
        .withTransport(TransportEnum.WEBSOCKETS)
        .withHubProtocol(MessagePackHubProtocol())
        .build()

    private val requestInferenceTime = ConcurrentHashMap<String, Long>()
    private val requestBitmap = ConcurrentHashMap<String, Bitmap>()

    init {
        Log.d(TAG, "Connection already started")
        bindHubCallback()
        start()
    }

    private fun bindHubCallback() {
        hubConnection.on("Initialized", { str ->
            Log.e(TAG, "Initialized")
            currentStatus = DetectorStatusEnum.INITIALIZED
            detectorListener?.onInitialized()
        }, String::class.java)


        hubConnection.on("DetectionResult", { requestId, result ->

            if (result.isEmpty()) {
                detectorListener?.onEmptyDetect()
            } else {

                val latestDetectedBitmap = requestBitmap[requestId]
                requestBitmap.remove(requestId)

                val inferenceTime = System.currentTimeMillis() - requestInferenceTime[requestId]!!
                requestInferenceTime.remove(requestId)

                detectorListener?.onDetect(
                    results = result.map { it.toDetectionResult() }.toTypedArray(),
                    inferenceTime = inferenceTime,
                    detectWidth = DETECT_IMG_SIZE,
                    detectHeight = DETECT_IMG_SIZE,
                    inputImage = latestDetectedBitmap!!
                )
            }

        }, String::class.java, Array<DetectionResultDto>::class.java)

        hubConnection.on("DetectionError", { error ->
            currentStatus = DetectorStatusEnum.ERROR
            detectorListener?.onError(error)
        }, String::class.java)

    }


    override fun start() {
        currentStatus = DetectorStatusEnum.INITIALIZING

        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            Log.e(TAG, "Connection already started")
            currentStatus = DetectorStatusEnum.INITIALIZED
            return
        } else {
            Log.e(TAG, "Starting SignalR connection")
            try {
                hubConnection.start().blockingAwait()

                hubConnection.send(
                    "Init",
                    arrayOf(
                        config.cnfThreshold,
                        config.iouThreshold,
                        config.maxNumberDetection
                    ) //must send in sequence
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error starting SignalR connection", e)
                currentStatus = DetectorStatusEnum.ERROR
                detectorListener?.onError("Error starting SignalR connection")
            }
        }
    }

    override fun stop() {
        currentStatus = DetectorStatusEnum.STOPPING
        detectorListener?.onStopped()
        hubConnection.stop().blockingAwait()
        currentStatus = DetectorStatusEnum.STOPPED
    }

    override fun detectLiveStream(bitmap: Bitmap) {
//        Log.e(TAG, "Detecting live stream")

        processIfInitialized {
            val requestId = System.currentTimeMillis().toString()
            requestBitmap[requestId] = bitmap
            requestInferenceTime[requestId] = System.currentTimeMillis()

            val bitmapLB = bitmap.withLetterBox(newShape = Pair(DETECT_IMG_SIZE, DETECT_IMG_SIZE))
            detectAsync(requestId, bitmapLB.toByteArray())
        }

    }

    override fun detectImage(bytes: ByteArray): Triple<Array<DetectionResult>, Int, Int> {
        processIfInitialized {
            return detectSync(bytes)
        }

        return Triple(arrayOf(), DETECT_IMG_SIZE, DETECT_IMG_SIZE)

    }

    override fun detectImage(bitmap: Bitmap): Triple<Array<DetectionResult>, Int, Int> {

        processIfInitialized {
            val requestId = System.currentTimeMillis().toString()
            requestBitmap[requestId] = bitmap
            requestInferenceTime[requestId] = System.currentTimeMillis()

            val bitmapLB = bitmap.withLetterBox(newShape = Pair(DETECT_IMG_SIZE, DETECT_IMG_SIZE))
            return detectImage(bitmapLB.toByteArray())
        }

        return Triple(arrayOf(), DETECT_IMG_SIZE, DETECT_IMG_SIZE)
    }

    override fun updateListener(detectorListener: IDetectorListener?) {
        processIfInitialized {
            this.detectorListener = detectorListener
        }
    }

    override fun updateConfigurations(config: DetectorConfiguration) {
        super.updateConfigurations(config)

        processIfInitialized {
            hubConnection.send(
                "UpdateConfiguration",
                arrayOf(
                    config.cnfThreshold,
                    config.iouThreshold,
                    config.maxNumberDetection
                ) //must send in sequence
            )
        }
    }


    /**
     * Private methods
     * */
    private fun detectAsync(requestId: String, bytes: ByteArray) {
        if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
            detectorListener?.onError("Connection not established")
            start() // try to start connection again
            return
        }

        try {
            hubConnection.send("DetectLiveStream", requestId, bytes)
        } catch (e: Exception) {
            detectorListener?.onError("Error sending image to server")
        }
    }

    private fun detectSync(bytes: ByteArray): Triple<Array<DetectionResult>, Int, Int> {
        if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
            Log.e(TAG, "Connection not established")
            detectorListener?.onError("Connection not established")
            start() // try to start connection again
            return Triple(arrayOf(), DETECT_IMG_SIZE, DETECT_IMG_SIZE)
        }

        try {
            // synchronous call
            val result = hubConnection.invoke(Array<DetectionResult>::class.java, "DetectImage", bytes).blockingGet()
            return Triple(result, DETECT_IMG_SIZE, DETECT_IMG_SIZE)

        } catch (e: Exception) {
            detectorListener?.onError("Error sending image to server")
        }

        return Triple(arrayOf(), DETECT_IMG_SIZE, DETECT_IMG_SIZE)
    }

    companion object {
        const val TAG = "DetectionHub"
        val HUB_URL = SERVER_BASE_URL + "DetectionHub"
        const val DETECT_IMG_SIZE = 640
    }
}