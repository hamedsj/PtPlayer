package me.pitok.videolist.views

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.util.CoilUtils
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_video_list.view.*
import me.pitok.sdkextentions.toDp
import me.pitok.sdkextentions.toPx
import me.pitok.videolist.R
import me.pitok.videolist.entities.FileEntity
import java.io.File


@EpoxyModelClass
abstract class VideoListModel :
    EpoxyModelWithHolder<VideoListModel.VideoListViewHolder>(){

    @EpoxyAttribute lateinit var fileEntity: FileEntity
    @EpoxyAttribute lateinit var onClick: (String, Int) -> Unit
    @EpoxyAttribute var folderTintColor: Int = 0
    @EpoxyAttribute lateinit var coilImageLoader: ImageLoader
    @EpoxyAttribute var screenWidth: Int = 0

    inner class VideoListViewHolder : EpoxyHolder(){
        lateinit var fileIc: RoundedImageView
        lateinit var nameTv: TextView
        lateinit var detailsTv: TextView
        lateinit var viewClick: View
        private val fileImageHorizentalMargins = 24f.toPx()
        val fileImageWidth = (screenWidth/2)-fileImageHorizentalMargins
        val fileImageHeight = ( fileImageWidth * (9/16f)).toInt()
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
            holder.viewClick.setOnClickListener{onClick.invoke(path, type)}
            holder.fileIc.setImageResource(0)

            //cancel all old requests on this holder.fileIc
            CoilUtils.clear(holder.fileIc)

            when(type){
                FileEntity.FILE_TYPE -> {
                    val fileIcLayoutParams = holder.fileIc.layoutParams
                    fileIcLayoutParams.height = holder.fileImageHeight
                    fileIcLayoutParams.width = holder.fileImageWidth
                    holder.fileIc.apply {
                        adjustViewBounds = false
                        cornerRadius = 8f.toPx().toFloat()
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        clearColorFilter()
                        load(File(path), coilImageLoader){
                            placeholder(android.R.color.black)
                            error(android.R.color.black)
                            videoFrameMillis(1000)
                        }
                        layoutParams = fileIcLayoutParams
                    }
                }
                else -> {
                    val folderIcLayoutParams = holder.fileIc.layoutParams
                    folderIcLayoutParams.height = 80f.toPx()
                    folderIcLayoutParams.width = 80f.toPx()
                    holder.fileIc.apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        cornerRadius = 0f
                        setColorFilter(folderTintColor)
                        setImageResource(R.drawable.ic_folder)
                        adjustViewBounds = true
                        layoutParams = folderIcLayoutParams
                    }
                }
            }
        }
    }

    override fun getDefaultLayout(): Int {
        return R.layout.item_video_list
    }

}