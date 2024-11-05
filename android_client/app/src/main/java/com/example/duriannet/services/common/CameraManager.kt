package com.example.duriannet.services.common

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Range
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
) {
    private var bitmapBuffer: Bitmap? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService? = null

    // Camera configuration
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_4_3
    private var targetFrameRate = Range(20, 25)
    private var imageFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
    private var currentFlashMode = FlashMode.OFF

    // Gesture handling
    private var isSingleTap = false


    // Check if device has flash
    val hasFlash: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    // Check if flash is available for current camera
    val isFlashAvailable: Boolean
        get() = camera?.cameraInfo?.hasFlashUnit() == true


    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                onInitialized?.invoke()
            } catch (e: Exception) {
                onError?.invoke("Failed to start camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .setTargetFrameRate(targetFrameRate)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(imageFormat)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(previewView.surfaceProvider)
            setupCameraGestures()

            // Turn on torch if flash mode is ON
            camera?.cameraControl?.enableTorch(currentFlashMode == FlashMode.ON)


        } catch (e: Exception) {
            onError?.invoke("Use case binding failed: ${e.message}")
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        if (bitmapBuffer == null) {
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
        }

        bitmapBuffer?.let { bitmap ->
            imageProxy.use { bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

            val matrix = Matrix().apply {
                setRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            onImageAnalyzed?.invoke(rotatedBitmap)
        }

        imageProxy.close()
    }

    private var _isGestureEnabled: Boolean = true
    val isGestureEnabled: Boolean
        get() = _isGestureEnabled

    private fun setupCameraGestures() {
        val cameraControl = camera?.cameraControl ?: return
        val cameraInfo = camera?.cameraInfo ?: return

        val scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {

                    if (!_isGestureEnabled) return false

                    val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1F
                    cameraControl.setZoomRatio(currentZoomRatio * detector.scaleFactor)
                    return true
                }
            }
        )

        previewView.setOnTouchListener { _, event ->
            if (!_isGestureEnabled) return@setOnTouchListener false

            handleTouchEvent(event, cameraControl, scaleGestureDetector)
            previewView.performClick()
            true
        }
    }

    private fun handleTouchEvent(
        event: MotionEvent,
        cameraControl: CameraControl,
        scaleGestureDetector: ScaleGestureDetector,
    ) {
        scaleGestureDetector.onTouchEvent(event)

        // See (https://developer.android.com/develop/ui/views/touch-and-input/gestures/multi#track)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isSingleTap = event.pointerCount == 1
            }

            MotionEvent.ACTION_UP -> {
                if (isSingleTap && event.pointerCount == 1) {
                    handleFocus(event, cameraControl)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // if it's a multi-finger gesture, then it's a pinch gesture
                isSingleTap = event.pointerCount == 1
            }

            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> {
                isSingleTap = false
            }
        }
    }


    private fun handleFocus(event: MotionEvent, cameraControl: CameraControl) {
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(event.x, event.y)

        val focusAction = FocusMeteringAction.Builder(point)
            .addPoint(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(2, TimeUnit.SECONDS)
            .build()

        cameraControl.startFocusAndMetering(focusAction)
        onFocusAndMetering?.invoke(event)
    }

    // Configuration methods
    fun enableGestures(enable: Boolean) {
        _isGestureEnabled = enable
    }

    // lensFacing: CameraSelector.LENS_FACING_BACK or CameraSelector.LENS_FACING_FRONT
    fun setLensFacing(facing: Int) {
        lensFacing = facing
        bindCameraUseCases()
    }

    fun getLensFacing(): Int = lensFacing

    fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }


    // aspectRatio: AspectRatio.RATIO_4_3 or AspectRatio.RATIO_16_9
    fun setAspectRatio(ratio: Int) {
        aspectRatio = ratio
        bindCameraUseCases()
    }

    fun getAspectRatio(): Int = aspectRatio


    // targetFrameRate: default : Range(20, 25)
    fun setTargetFrameRate(range: Range<Int>) {
        targetFrameRate = range
        bindCameraUseCases()
    }

    fun getTargetFrameRate(): Range<Int> = targetFrameRate


    // Flash mode
    fun setFlashMode(mode: FlashMode): Boolean {
        if (!isFlashAvailable) return false

        currentFlashMode = mode
        return when (mode) {
            FlashMode.OFF -> {
                camera?.cameraControl?.enableTorch(false)
                true
            }

            FlashMode.ON -> {
                camera?.cameraControl?.enableTorch(true)
                true
            }

            FlashMode.AUTO -> {
                // Set up auto flash mode for image capture
                // Note: This is primarily useful for still image capture
                // For video/preview, torch mode (ON/OFF) is more applicable
                false
            }
        }
    }

    fun getFlashMode(): FlashMode = currentFlashMode

    fun toggleFlash(): FlashMode {
        val newMode = if (currentFlashMode == FlashMode.ON) FlashMode.OFF else FlashMode.ON
        setFlashMode(newMode)
        return currentFlashMode
    }


    // imageFormat: ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888 or ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
    fun setImageFormat(format: Int) {
        imageFormat = format
        bindCameraUseCases()
    }

    fun getImageFormat(): Int = imageFormat


    // image analyzer
    fun clearAnalyzer() {
        imageAnalyzer?.clearAnalyzer()
    }

    fun setAnalyzer() {
        imageAnalyzer?.setAnalyzer(cameraExecutor!!) { imageProxy ->
            processImage(imageProxy)
        }
    }

    fun release() {
        cameraExecutor?.shutdown()
        cameraProvider?.unbindAll()
        bitmapBuffer = null
    }


    // Callback interfaces
    private var onImageAnalyzed: ((Bitmap) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var onInitialized: (() -> Unit)? = null
    private var onFocusAndMetering: ((event: MotionEvent) -> Unit)? = null

    // Listener methods
    fun setOnImageAnalyzedListener(listener: (Bitmap) -> Unit) {
        onImageAnalyzed = listener
    }

    fun setOnErrorListener(listener: (String) -> Unit) {
        onError = listener
    }

    fun setOnInitializedListener(listener: () -> Unit) {
        onInitialized = listener
    }

    fun setOnFocusAndMeteringListener(listener: (MotionEvent) -> Unit) {
        onFocusAndMetering = listener
    }


    // Flash states
    enum class FlashMode {
        OFF, ON, AUTO
    }


    companion object {
        private const val TAG = "CameraManager"
    }
}