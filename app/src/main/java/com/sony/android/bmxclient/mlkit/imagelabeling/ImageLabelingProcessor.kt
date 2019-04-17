package com.sony.android.bmxclient.mlkit.imagelabeling

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector
import com.sony.android.bmxclient.mlkit.*
import java.io.IOException

/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor : VisionProcessorBase<List<FirebaseVisionLabel>>() {

    private val detector: FirebaseVisionLabelDetector = FirebaseVision.getInstance().visionLabelDetector

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionLabel>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionLabel>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    ) {
        graphicOverlay.clear()
        originalCameraImage.let { image ->
            val imageGraphic = CameraImageGraphic(graphicOverlay, image)
            graphicOverlay.add(imageGraphic)
        }
        val labelGraphic = LabelGraphic(graphicOverlay, results)
        graphicOverlay.add(labelGraphic)
        graphicOverlay.postInvalidate()

        val list = ArrayList<String>()
        for (result in results) {
            list.add(result.label)
        }
        visionListener.onSuccess(list)
    }

    override fun onFailure(e: Exception,  visionListener: VisionImageProcessor.Listener) {
        Log.w(TAG, "Label detection failed.$e")
        visionListener.onFailure(e)
    }

    companion object {

        private const val TAG = "ImageLabelingProcessor"
    }
}
