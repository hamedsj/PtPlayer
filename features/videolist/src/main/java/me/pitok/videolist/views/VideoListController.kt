package me.pitok.videolist.views

import coil.ImageLoader
import com.airbnb.epoxy.EpoxyController
import me.pitok.videolist.entities.FileEntity

class VideoListController constructor(
    private val onItemClick : (String, Int) -> Unit,
    private val folderTintColor : Int,
    private val coilImageLoader: ImageLoader,
    private val screenWidth : Int
) : EpoxyController() {

    lateinit var items: List<FileEntity>

    override fun buildModels() {
        val ids = mutableListOf<Int>()
        items.forEach startOfForeach@{ item ->
            val itemId = items.indexOf(item)
            if (ids.contains(itemId)) return@startOfForeach
            VideoListModel_()
                .id(itemId)
                .fileEntity(item)
                .coilImageLoader(coilImageLoader)
                .folderTintColor(folderTintColor)
                .screenWidth(screenWidth)
                .onClick(onItemClick)
                .addTo(this)
            ids.add(itemId)
        }
    }
}