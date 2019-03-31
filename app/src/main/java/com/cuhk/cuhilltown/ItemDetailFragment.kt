package com.cuhk.cuhilltown

import android.content.ContentResolver
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.support.annotation.AnyRes
import android.support.v4.app.Fragment
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import android.view.ViewGroup


private var lastPosition = 0
private var lastItemTitle = ""

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment(), SensorEventListener {

    private lateinit var item: Item

    private lateinit var rootView: View
    private lateinit var videoView: VideoView
    private lateinit var mediaCtrl: MediaController
    private lateinit var overlayView: ImageView

    private lateinit var sensorManager: SensorManager
    private lateinit var windowManager: WindowManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private var adjustedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_DRAWABLE)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.

                item = Item(it.getInt(ARG_ITEM_DRAWABLE), it.getString(ARG_ITEM_TITLE)!!, it.getString(ARG_ITEM_VIDEO_URL)!!, it.getInt(ARG_ITEM_RAW))

                activity?.toolbar_layout?.title = item.title
            } else {
                throw NullPointerException();
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("onCreateView")
        rootView = inflater.inflate(R.layout.item_detail, container, false)
        videoView = rootView.my_video
        mediaCtrl = MediaController(rootView.context)
        overlayView = rootView.playback_overlay

        // Video
        mediaCtrl.setAnchorView(videoView)
        videoView.setMediaController(mediaCtrl)

        if (item.videoUrl == "") {
            val uri = getUriFromDrawable(rootView.context, item.raw!!)
            videoView.setVideoURI(uri)
        } else {
            videoView.setVideoURI(Uri.parse(item.videoUrl))
        }

        if (item.title == lastItemTitle) {
            println(lastPosition)
            videoView.seekTo(lastPosition)
        } else {
            lastItemTitle = item.title
        }

        videoView.start()

        videoView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (overlayView.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = overlayView.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(0, Math.round(videoView.height / 2.0 - (25.0 * rootView.context.resources.displayMetrics.density)).toInt(), 0, 0)
                overlayView.requestLayout()
            }
        }

        // Sensor
        sensorManager = rootView.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        windowManager = rootView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return rootView
    }

    override fun onResume() {
        super.onResume()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        println("onPause")

        lastPosition = videoView.currentPosition

        videoView.pause()
        sensorManager.unregisterListener(this)

        super.onPause()
    }

    override fun onDestroyView() {
        videoView.stopPlayback()
        sensorManager.unregisterListener(this)

        super.onDestroyView()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        updateOrientationAngles()
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        when (this.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, adjustedRotationMatrix)
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, adjustedRotationMatrix)
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, adjustedRotationMatrix)
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, adjustedRotationMatrix)
        }

        SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles)

        val azimuth = orientationAngles[0] * 180 / Math.PI
        val pitch = orientationAngles[1] * 180 / Math.PI // Left Right
        val roll = orientationAngles[2] * 180 / Math.PI // Front Bottom

        if (Math.abs(roll) < 60 && Math.abs(pitch) < 10) {
            videoView.start()
            mediaCtrl.visibility = VISIBLE

            overlayView.visibility = GONE
        } else if (Math.abs(roll) > 60) {
            videoView.pause()
            mediaCtrl.visibility = GONE

            overlayView.visibility = VISIBLE
            overlayView.setImageResource(R.drawable.pause)
        } else if (pitch < -10) {
            videoView.seekTo(videoView.currentPosition - 2000)
            mediaCtrl.visibility = GONE

            overlayView.visibility = VISIBLE
            overlayView.setImageResource(R.drawable.rewind)
        } else if (pitch > 10) {
            videoView.seekTo(videoView.currentPosition + 2000)
            mediaCtrl.visibility = GONE

            overlayView.visibility = VISIBLE
            overlayView.setImageResource(R.drawable.forward)
        }

        // println("$azimuth, $pitch, $roll, ${this.windowManager.defaultDisplay.rotation}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun getUriFromDrawable(context: Context, @AnyRes drawableId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getResources().getResourcePackageName(drawableId)
                    + '/'.toString() + context.getResources().getResourceTypeName(drawableId)
                    + '/'.toString() + context.getResources().getResourceEntryName(drawableId)
        )
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_DRAWABLE = "item_drawable"
        const val ARG_ITEM_TITLE = "item_title"
        const val ARG_ITEM_VIDEO_URL = "item_video_url"
        const val ARG_ITEM_RAW = "item_raw"
    }
}
