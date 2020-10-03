package me.pitok.design.views

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import me.pitok.design.entity.BottomSheetItemEntity
import kotlinx.android.synthetic.main.item_chooser_bottom_sheet.view.itemIcon
import kotlinx.android.synthetic.main.item_chooser_bottom_sheet.view.itemTitle

internal class ChooserBottomSheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemIcon: AppCompatImageView = view.itemIcon
    private val itemTitle: AppCompatTextView = view.itemTitle

    fun bind(item: BottomSheetItemEntity, onClick: () -> Unit) {
        if (item.itemIconResource != null) {
            itemIcon.setImageResource(item.itemIconResource)
            itemIcon.visibility = View.VISIBLE
        } else {
            itemIcon.visibility = View.GONE
        }
        item.itemTitleResource?.let {
            itemTitle.setText(it)
        }
        item.itemTitleString?.let {
            itemTitle.text = it
        }
        itemView.setOnClickListener {
            onClick.invoke()
            item.itemOnClickListener.invoke()
        }
    }
}