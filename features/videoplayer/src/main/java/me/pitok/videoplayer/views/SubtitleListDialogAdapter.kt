package me.pitok.videoplayer.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.pitok.videoplayer.R
import me.pitok.videoplayer.entity.ListDialogItemEntity

internal class SubtitleListDialogAdapter(
    private val items: List<ListDialogItemEntity>,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<SubtitleListDialogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleListDialogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subtitle_list_dialog, parent, false)
        return SubtitleListDialogViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SubtitleListDialogViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }
}