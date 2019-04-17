package com.sony.android.bmxclient.mlkit

import android.graphics.Bitmap
import android.support.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.sony.android.bmxclient.mlkit.BitmapUtils
import com.sony.android.bmxclient.mlkit.FrameMetadata
import com.sony.android.bmxclient.mlkit.GraphicOverlay
import com.sony.android.bmxclient.mlkit.VisionImageProcessor
import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay, visionListener)
        }
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay, visionListener: VisionImageProcessor.Listener) {
        detectInVisionImage(
                null, /* bitmap */
                FirebaseVisionImage.fromBitmap(bitmap),
                null,
                graphicOverlay,
                visionListener)
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay, visionListener: VisionImageProcessor.Listener) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage!!, processingMetaData!!, graphicOverlay, visionListener)
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width)
                .setHeight(frameMetadata.height)
                .setRotation(frameMetadata.rotation)
                .build()

        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
                bitmap, FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata,
                graphicOverlay, visionListener)
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    ) {
        detectInImage(image)
                .addOnSuccessListener { results ->
                    onSuccess(originalCameraImage, results,
                            metadata!!,
                            graphicOverlay,
                            visionListener)
                    processLatestImage(graphicOverlay, visionListener)
                }
                .addOnFailureListener { e -> onFailure(e, visionListener) }
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     * image.
     */
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: T,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        visionListener: VisionImageProcessor.Listener
    )

    protected abstract fun onFailure(e: Exception, visionListener: VisionImageProcessor.Listener)
}
