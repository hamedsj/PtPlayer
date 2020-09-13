package me.pitok.videolist.views

import android.view.View
import com.airbnb.epoxy.EpoxyController
import me.pitok.videolist.entities.FileEntity

class VideoListController constructor(
    private val onItemClick : (String) -> Unit
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
                .onClick(onItemClick)
                .addTo(this)
            ids.add(itemId)
        }
    }
}