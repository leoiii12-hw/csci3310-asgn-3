package com.cuhk.cuhilltown

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(item_list)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val items = listOf(
            Item(
                R.drawable.soaring_over_cuhk,
                "Soaring CUHK",
                "",
                R.raw.soaring_cuhk
            ),
            Item(
                R.drawable.humble_cottage,
                "Humble Cottage",
                "http://course.cse.cuhk.edu.hk/~csci3310/1819R2/asg3/humble_cottage_cuhk.mp4"
            ),
            Item(
                R.drawable.green_map,
                "Green Building Awards",
                "http://course.cse.cuhk.edu.hk/~csci3310/1819R2/asg3/green_bldg_cuhk.mp4"
            ),
            Item(
                R.drawable.connecting_the_space,
                "Space and Earth",
                "http://course.cse.cuhk.edu.hk/~csci3310/1819R2/asg3/connecting_space_cuhk.mp4"
            ),
            Item(
                R.drawable.ir_cu,
                "InfraRed Spots",
                "http://course.cse.cuhk.edu.hk/~csci3310/1819R2/asg3/infrared_cuhk.mp4"
            )
        )

        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, items, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: List<Item>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Item
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt(ItemDetailFragment.ARG_ITEM_DRAWABLE, item.drawable)
                            putString(ItemDetailFragment.ARG_ITEM_TITLE, item.title)
                            putString(ItemDetailFragment.ARG_ITEM_VIDEO_URL, item.videoUrl)
                            if (item.raw != null) putInt(ItemDetailFragment.ARG_ITEM_RAW, item.raw)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_DRAWABLE, item.drawable)
                        putExtra(ItemDetailFragment.ARG_ITEM_TITLE, item.title)
                        putExtra(ItemDetailFragment.ARG_ITEM_VIDEO_URL, item.videoUrl)
                        if (item.raw != null) putExtra(ItemDetailFragment.ARG_ITEM_RAW, item.raw)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.thumbnailView.setImageResource(item.drawable)
            holder.contentView.text = item.title

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val thumbnailView: ImageView = view.thumbnail
            val contentView: TextView = view.content
        }
    }
}
