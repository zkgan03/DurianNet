package com.example.duriannet.utils

object Constant {
    val SERVER_BASE_URL: String
        get() {
            return if (Common.isEmulator()) {
                "http://10.0.2.2:5176/"
            } else {
                "http://192.168.0.130:5176/"
            }
        }
    const val YOLOV8_MODEL_PATH = "yolov8s_int8_320.tflite"
    const val YOLOV8_LABEL_PATH = "labels.txt"
}