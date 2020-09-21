package me.pitok.videoplayer.views

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.subtitle_list_dialog.view.*
import me.pitok.videoplayer.entity.ListDialogItemEntity

internal class SubtitleListDialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemIcon: AppCompatImageView = view.itemIcon
    private val itemTitle: AppCompatTextView = view.itemTitle

    fun bind(item: ListDialogItemEntity, onClick: () -> Unit) {
        if (item.itemIconResource != null) {
            itemIcon.setImageResource(item.itemIconResource)
            itemIcon.visibility = View.VISIBLE
        } else {
            itemIcon.visibility = View.GONE
        }
        itemTitle.text = item.itemTitleResource
        itemView.setOnClickListener {
            onClick.invoke()
            item.itemOnClickListener.invoke(item.itemUnique)
        }
    }
}