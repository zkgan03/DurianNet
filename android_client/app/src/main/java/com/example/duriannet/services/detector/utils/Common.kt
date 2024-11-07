package com.example.duriannet.services.detector.utils

import com.example.duriannet.models.DetectionResult


object Common {

    // Non-Maximum Suppression :
    // a post-processing technique used in object detection to eliminate duplicate detections
    // and select the most relevant detected objects.
    fun applyNMS(boxes: List<DetectionResult>, iouThreshold: Float = 0.3f): List<DetectionResult> {

        val sortedBoxes = boxes.sortedByDescending { it.confidence }.toMutableList() // sort by confidence
        val selectedBoxes = mutableListOf<DetectionResult>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first() // highest confidence
            selectedBoxes.add(first) // add to selected boxes
            sortedBoxes.remove(first) // remove from sorted boxes

            val iterator = sortedBoxes.iterator() // iterate through remaining boxes
            while (iterator.hasNext()) {
                val nextBox = iterator.next()

                if(first.label != nextBox.label) continue // check if the classes are the same

                val iou = calculateIoU(first, nextBox)
                if (iou >= iouThreshold) {
                    iterator.remove() // remove the box if IoU is greater than threshold
                }
            }
        }

        return selectedBoxes
    }

    // Intersection over Union (IoU) :
    // a measure of the overlap between two bounding boxes.
    fun calculateIoU(boxA: DetectionResult, boxB: DetectionResult): Float {
        val xA = maxOf(boxA.left, boxB.left)
        val yA = maxOf(boxA.top, boxB.top)
        val xB = minOf(boxA.left + boxA.width, boxB.left + boxB.width)
        val yB = minOf(boxA.top + boxA.height, boxB.top + boxB.height)

        val interArea = maxOf(0, xB - xA) * maxOf(0, yB - yA)
        val boxAArea = boxA.width * boxA.height
        val boxBArea = boxB.width * boxB.height

        return interArea / (boxAArea + boxBArea - interArea).toFloat()
    }

}