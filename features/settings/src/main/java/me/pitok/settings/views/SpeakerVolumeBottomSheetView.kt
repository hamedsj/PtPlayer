package me.pitok.settings.views

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

class SpeakerVolumeBottomSheetView(
    context: Context
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme) {

    private val bottomSheetTitle: AppCompatTextView
    private val bottomSheetSlider: Slider
    private val bottomSheetSystemVolumeSw: SwitchCompat
    private val bottomSheetSystemVolumeClickable: View
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
    var defaultSpeakerVolume: Int = 0
        set(value) {
            when (value) {
                -1 -> {
                    bottomSheetSystemVolumeSw.isChecked = true
                    bottomSheetSlider.isEnabled = false
                    field = 100
                }
                in 0..100 -> {
                    bottomSheetSystemVolumeSw.isChecked = false
                    bottomSheetSlider.isEnabled = true
                    bottomSheetSlider.value = value.toFloat()
                    field = value
                }
                else -> {
                    field = 100
                }
            }
        }

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_speaker_volume_bottom_sheet, null, false)
        bottomSheetTitle = view.findViewById(R.id.sheetTitle)
        bottomSheetSlider = view.findViewById(R.id.speakerVolumeBottomSheetSeekbar)
        bottomSheetSystemVolumeSw = view.findViewById(R.id.speakerVolumeBottomSheetSystemVolumeSw)
        bottomSheetSystemVolumeClickable = view.findViewById(R.id.speakerVolumeBottomSheetSystemVolumeClickable)
        bottomSheetPrimaryBt = view.findViewById(R.id.speakerVolumeBottomSheetPrimaryBt)
        bottomSheetSecondaryBt = view.findViewById(R.id.speakerVolumeBottomSheetSecondaryBt)
        view.background = ContextCompat.getDrawable(context,R.drawable.shape_bottom_sheet_background)
        setContentView(view)

        bottomSheetSystemVolumeSw.setOnCheckedChangeListener { _, checked ->
            bottomSheetSlider.isEnabled = !checked
        }

        bottomSheetSystemVolumeClickable.setOnClickListener{
            bottomSheetSystemVolumeSw.isChecked = !bottomSheetSystemVolumeSw.isChecked
        }

        bottomSheetPrimaryBt.setOnClickListener{
            onPrimaryClick.invoke(
                if (!bottomSheetSystemVolumeSw.isChecked)
                    bottomSheetSlider.value.toInt()
                else
                    -1
            )
        }
        bottomSheetSecondaryBt.setOnClickListener{
            onSecondaryClick.invoke(
                if (!bottomSheetSystemVolumeSw.isChecked)
                    bottomSheetSlider.value.toInt()
                else
                    -1
            )
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