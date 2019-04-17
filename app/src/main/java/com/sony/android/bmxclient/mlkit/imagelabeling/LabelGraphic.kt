package com.sony.android.bmxclient.mlkit.imagelabeling

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.sony.android.bmxclient.mlkit.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic(
    private val overlay: GraphicOverlay,
    private val labels: List<FirebaseVisionLabel>
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60.0f
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.width / 4.0f
        var y = overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label.label, x, y, textPaint)
            y -= 62.0f
        }
    }
}
