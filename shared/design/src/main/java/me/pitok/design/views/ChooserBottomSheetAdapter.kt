package me.pitok.design.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.pitok.design.R
import me.pitok.design.entity.BottomSheetItemEntity

internal class ChooserBottomSheetAdapter(
    private val items: List<BottomSheetItemEntity>,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<ChooserBottomSheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooserBottomSheetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chooser_bottom_sheet, parent, false)
        return ChooserBottomSheetViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChooserBottomSheetViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }
}