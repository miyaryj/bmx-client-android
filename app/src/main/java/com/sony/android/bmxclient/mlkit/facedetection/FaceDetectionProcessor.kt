package com.sony.android.bmxclient.mlkit.facedetection

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.sony.android.bmxclient.mlkit.*
import java.io.IOException

/** Face Detector Demo.  */
class FaceDetectionProcessor : VisionProcessorBase<List<FirebaseVisionFace>>() {

    private val detector: FirebaseVisionFaceDetector

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .enableTracking()
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    ) {
        graphicOverlay.clear()
        val imageGraphic = CameraImageGraphic(graphicOverlay, originalCameraImage)
        graphicOverlay.add(imageGraphic)
        for (i in results.indices) {
            val face = results[i]

            val cameraFacing = frameMetadata.cameraFacing
            val faceGraphic = FaceGraphic(graphicOverlay, face, cameraFacing)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
        visionListener.onSuccess(ArrayList<String>())
    }

    override fun onFailure(e: Exception,  visionListener: VisionImageProcessor.Listener) {
        Log.e(TAG, "Face detection failed $e")
        visionListener.onFailure(e)
    }

    companion object {

        private val TAG = "FaceDetectionProcessor"
    }
}