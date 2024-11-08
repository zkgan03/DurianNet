package com.example.duriannet.services.detector.base

import android.content.res.Resources
import com.example.duriannet.services.detector.enum.DelegateEnum

data class DetectorConfiguration(
    var cnfThreshold : Float = 0.4f,
    var iouThreshold : Float = 0.5f,
    var maxNumberDetection : Int = 10,
    var currentDelegate : DelegateEnum = DelegateEnum.GPU
)