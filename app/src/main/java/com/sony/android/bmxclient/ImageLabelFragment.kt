package com.sony.android.bmxclient

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ml.common.FirebaseMLException
import com.sony.android.bmxclient.mlkit.CameraSource
import com.sony.android.bmxclient.mlkit.imagelabeling.ImageLabelingProcessor
import kotlinx.android.synthetic.main.fragment_image_label.*
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ImageLabelFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ImageLabelFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ImageLabelFragment : Fragment(), AppRenderer {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var activity: Activity? = null
    private var listener: OnFragmentInteractionListener? = null
    private var cameraSource: CameraSource? = null
    private var eventMonitor: EventMonitor? = null
    private var tts: TextToSpeech? = null

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.activity!!.packageManager
                    .getPackageInfo(this.activity!!.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_label, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activity = context
        } else {
            throw RuntimeException(context.toString() + " must be Activity")
        }
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
        eventMonitor = EventMonitor(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (firePreview == null) {
            Log.d(TAG, "Preview is null")
        }

        if (fireFaceOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(activity, fireFaceOverlay, eventMonitor)
        }

        try {
            Log.i(TAG, "Using Image Label Detector Processor")
            cameraSource?.setMachineLearningFrameProcessor(ImageLabelingProcessor())
        } catch (e: FirebaseMLException) {
            Log.e(TAG, "can not create camera source")
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        cameraSource?.let {
            try {
                if (firePreview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                firePreview?.start(cameraSource, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    override fun render(speech: String) {
        tts?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
        tts = TextToSpeech(activity, null)
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview?.stop()
        tts?.shutdown()
        tts = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraSource?.release()
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        listener = null
        eventMonitor = null
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(context!!, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(context!!, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity!!, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private const val TAG = "ImageLabelFragment"
        private const val PERMISSION_REQUESTS = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImageLabelFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageLabelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
