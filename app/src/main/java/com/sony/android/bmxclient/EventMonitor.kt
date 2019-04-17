package com.sony.android.bmxclient

import android.util.Log
import com.sony.android.bmxclient.mlkit.VisionImageProcessor
import java.lang.Exception

private const val TAG = "EventMonitor"

class EventMonitor(
    private val renderer: AppRenderer
) : VisionImageProcessor.Listener {
    private val INTERVAL = 3 * 1000
    private var lastSpeech: Long = 0

    override fun onSuccess(results: List<String>) {
        Log.i(TAG, "result: $results")
        if(System.currentTimeMillis() - lastSpeech >= INTERVAL && results.size > 0) {
            var text = "This is a ${results[0]}"
            if (results[0].startsWith('a') || results[0].startsWith('i') || results[0].startsWith('u')
                || results[0].startsWith('e') || results[0].startsWith('o')) {
                text = "This is an ${results[0]}"
            }
            renderer.render(text)
            lastSpeech = System.currentTimeMillis()
        }
    }

    override fun onFailure(e: Exception?) {
        Log.i(TAG, "failure: $e")
    }
}