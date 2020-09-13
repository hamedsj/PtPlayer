package me.pitok.videolist.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.item_video_list.view.*
import me.pitok.videolist.R
import me.pitok.videolist.entities.FileEntity


@EpoxyModelClass
abstract class VideoListModel :
    EpoxyModelWithHolder<VideoListModel.VideoListViewHolder>(){

    @EpoxyAttribute lateinit var fileEntity: FileEntity
    @EpoxyAttribute var onClick: (String) -> Unit = {_ ->}

    inner class VideoListViewHolder : EpoxyHolder(){
        lateinit var fileIc: ImageView
        lateinit var nameTv: TextView
        lateinit var detailsTv: TextView
        lateinit var viewClick: View

        override fun bindView(itemView: View) {
            fileIc = itemView.itemVideoListIcon
            nameTv = itemView.itemVideoListName
            detailsTv = itemView.itemVideoListDetails
            viewClick = itemView.itemVideoListClickable
        }
    }

    override fun bind(holder: VideoListViewHolder) {
        super.bind(holder)
        with(fileEntity) {
            holder.nameTv.text = name
            holder.detailsTv.text = details
            holder.viewClick.setOnClickListener{onClick.invoke(path)}
        }
    }

    override fun getDefaultLayout(): Int {
        return R.layout.item_video_list
    }

}