package me.pitok.settings.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import me.pitok.settings.R

@SuppressLint("SetTextI18n")
class SubtitleFontSizeBottomSheetView(
    context: Context
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme) {

    private val bottomSheetTitle: AppCompatTextView
    private val bottomSheetSlider: Slider
    private val bottomSheetSubtitleFontTitle: AppCompatTextView
    private val bottomSheetSubtitleFontSize: AppCompatTextView
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

    var onPrimaryClick: (Int) -> Unit = {}
    var onSecondaryClick: (Int) -> Unit = {}
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
    var sliderPosition: Int = 18
        set(value) {
            if (value>18) bottomSheetSlider.value = 18f
            if (value<14) bottomSheetSlider.value = 14f
            else bottomSheetSlider.value = value.toFloat()
            bottomSheetSubtitleFontSize.text = "${bottomSheetSlider.value.toInt()}sp"
            field = value
        }

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_subtitle_font_size_bottom_sheet, null, false)
        bottomSheetTitle = view.findViewById(R.id.sheetTitle)
        bottomSheetSlider = view.findViewById(R.id.subtitleFontSizeBottomSheetSeekbar)
        bottomSheetSubtitleFontTitle = view.findViewById(R.id.subtitleFontSizeBottomSheetTitle)
        bottomSheetSubtitleFontSize = view.findViewById(R.id.subtitleFontSizeBottomSheetSize)
        bottomSheetPrimaryBt = view.findViewById(R.id.subtitleFontSizeBottomSheetPrimaryBt)
        bottomSheetSecondaryBt = view.findViewById(R.id.subtitleFontSizeBottomSheetSecondaryBt)
        view.background = ContextCompat.getDrawable(context,R.drawable.shape_bottom_sheet_background)
        setContentView(view)

        bottomSheetSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            bottomSheetSubtitleFontSize.text = "${value.toInt()}sp"
        })

        bottomSheetPrimaryBt.setOnClickListener{
            onPrimaryClick.invoke(bottomSheetSlider.value.toInt())
        }
        bottomSheetSecondaryBt.setOnClickListener{
            onSecondaryClick.invoke(bottomSheetSlider.value.toInt())
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