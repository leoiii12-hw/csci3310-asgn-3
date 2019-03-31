package com.cuhk.cuhilltown

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.annotation.AnyRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    private var item: Item? = null

    private lateinit var rootView: View
    private lateinit var videoView: VideoView
    private lateinit var mediaCtrl: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_DRAWABLE)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.

                item = Item(it.getInt(ARG_ITEM_DRAWABLE), it.getString(ARG_ITEM_TITLE), it.getString(ARG_ITEM_VIDEO_URL), it.getInt(ARG_ITEM_RAW))

                activity?.toolbar_layout?.title = item?.title
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.item_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.item_detail.text = item!!.videoUrl

            videoView = rootView.my_video

            mediaCtrl = MediaController(rootView.context)
            mediaCtrl.setAnchorView(videoView)

            videoView.setMediaController(mediaCtrl)

            if (item!!.videoUrl == "") {
                val uri = getUriFromDrawable(rootView.context, item!!.raw!!)
                videoView.setVideoURI(uri)

            } else {
                videoView.setVideoURI(Uri.parse(item!!.videoUrl))
            }

            videoView.start()
        }

        return rootView
    }

    override fun onDestroyView(){
        this.videoView.stopPlayback()

        super.onDestroyView()
    }


    override fun onPause() {
        this.videoView.pause()

        super.onPause()
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
