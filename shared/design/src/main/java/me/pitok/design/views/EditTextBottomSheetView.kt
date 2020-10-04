package me.pitok.design.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.pitok.design.R

class EditTextBottomSheetView(
    context: Context
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme) {

    private val bottomSheetTitle: AppCompatTextView
    private val bottomSheetEditText: AppCompatEditText
    private val bottomSheetPrimaryBt: AppCompatTextView
    private val bottomSheetSecondaryBt: AppCompatTextView

    var sheetTitle: String = ""
        set(value) {
            if (value.isNotEmpty()) {
                bottomSheetTitle.visibility = View.VISIBLE
                bottomSheetTitle.text = value
            }
            field = value
        }

    var onPrimaryClick: (String) -> Unit = {}
    var onSecondaryClick: (String) -> Unit = {}
    var primaryText: String = ""
        set(value) {
            bottomSheetPrimaryBt.text = value
            field = value
        }
    var secondaryText: String = ""
        set(value) {
            bottomSheetSecondaryBt.text = value
            field = value
        }
    var editTextHint: String = ""
        set(value) {
            bottomSheetEditText.hint = value
            field = value
        }


    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_edittext_bottom_sheet, null, false)
        bottomSheetTitle = view.findViewById(R.id.sheetTitle)
        bottomSheetEditText = view.findViewById(R.id.editTextBottomSheetEt)
        bottomSheetPrimaryBt = view.findViewById(R.id.editTextBottomSheetPrimaryBt)
        bottomSheetSecondaryBt = view.findViewById(R.id.editTextBottomSheetSecondaryBt)
        view.background = ContextCompat.getDrawable(context,R.drawable.shape_bottom_sheet_background)
        setContentView(view)

        bottomSheetPrimaryBt.setOnClickListener{
            onPrimaryClick.invoke(bottomSheetEditText.text.toString().trim())
        }
        bottomSheetSecondaryBt.setOnClickListener{
            onSecondaryClick.invoke(bottomSheetEditText.text.toString().trim())
        }

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