package com.example.duriannet.utils

object Constant {
    val SERVER_BASE_URL: String
        get() {
//            return if (Common.isEmulator()) {
//                "http://10.0.2.2:5176/" // Updated port to 7091
//            } else {
//                /*"http://192.168.0.130:7091/"*/ // Ensure the correct port is reflected here too
//                //"http://172.16.37.102:5176/"
//                /*"http://192.168.0.130:5176/"*/
//            }
            return if (Common.isEmulator()) {
                "http://10.0.2.2:5176/"
            } else {
                "http://192.168.0.130:5176/"
            }
        }

    const val YOLOV8_MODEL_PATH = "durian_int8_2.tflite"
    const val YOLOV8_LABEL_PATH = "durian_label.txt"
}