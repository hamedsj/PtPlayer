package me.pitok.design.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.pitok.design.R
import me.pitok.design.entity.BottomSheetItemEntity


class ChooserBottomSheetView(
    context: Context
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme) {

    private val bottomSheetTitle: AppCompatTextView

    private val bottomSheetRecycler: RecyclerView

    var dismissOnClick: Boolean = true



    var sheetTitle: String = ""
        set(value) {
            if (value.isNotEmpty()) {
                bottomSheetTitle.visibility = View.VISIBLE
                bottomSheetTitle.text = value
            }
            field = value
        }

    var sheetItems: List<BottomSheetItemEntity> = emptyList()
        set(value) {
            val adapter = ChooserBottomSheetAdapter(value) {
                if (dismissOnClick) {
                    dismiss()
                }
            }
            bottomSheetRecycler.adapter = adapter
            field = value
        }

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_chooser_bottom_sheet, null, false)
        bottomSheetRecycler = view.findViewById(R.id.bottomSheetRecycler)
        bottomSheetRecycler.layoutManager = LinearLayoutManager(context)
        bottomSheetTitle = view.findViewById(R.id.sheetTitle)
        view.background = ContextCompat.getDrawable(context,R.drawable.shape_bottom_sheet_background)
        setContentView(view)

        // fix issue of not showing completely in landscape mode
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = this as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val behavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from(
                    bottomSheet as View
                )
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }
    }
}