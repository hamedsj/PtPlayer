package me.pitok.settings.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.design.entity.BottomSheetItemEntity
import me.pitok.design.views.ChooserBottomSheetView
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.mvi.MviView
import me.pitok.navigation.observeNavigation
import me.pitok.settings.R
import me.pitok.settings.databinding.FragmentSettingsBinding
import me.pitok.settings.di.SettingsComponentBuilder
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import me.pitok.settings.viewmodels.SettingsViewModel
import javax.inject.Inject

class SettingsFragment : Fragment(), MviView<SettingsState> {

    companion object {
        const val CLICK_ANIMATION_DURATION = 100L
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @ApplicationContext
    @Inject
    lateinit var applicationContext: Context

    private lateinit var binding: FragmentSettingsBinding

    private val settingsViewModel: SettingsViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        SettingsComponentBuilder.getComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.state.observe(viewLifecycleOwner, ::render)
        settingsViewModel.navigationObservable.observeNavigation(this)
        binding.settingsBackIc.setOnClickListener(::onBackClickListener)
        binding.settingsPlaybackSpeedClickable.setOnClickListener(::onPlaybackSpeedOptionClick)
        binding.settingsSpeakerVolumeClickable.setOnClickListener(::onSpeakerVolumeOptionClick)
        binding.settingsScreenOrientationClickable.setOnClickListener(::onScreenOrientationOptionClick)
        binding.settingsSubtitleFontSizeClickable.setOnClickListener(::onSubtitleFontSizeOptionClick)
        binding.settingsSubtitleTextColorClickable.setOnClickListener(::onSubtitleTextColorOptionClick)
        binding.settingsSubtitleHighlightColorClickable.setOnClickListener(::onSubtitleHighlightColorOptionClick)
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.FetchSettedOptions
            )
        }
    }

    private fun onPlaybackSpeedOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowPlaybackSpeedMenuIntent
            )
        }
    }

    private fun onSpeakerVolumeOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowSpeakerVolumeBottomSheetIntent
            )
        }
    }

    private fun onScreenOrientationOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowScreenOrientationBottomSheetIntent
            )
        }
    }

    private fun onSubtitleFontSizeOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowSubtitleFontSizeBottomSheetIntent
            )
        }
    }

    private fun onSubtitleTextColorOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowSubtitleTextColorBottomSheetIntent
            )
        }
    }

    private fun onSubtitleHighlightColorOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowSubtitleHighlightColorBottomSheetIntent
            )
        }
    }

    private fun onBackClickListener(view: View){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.ExitFromSettings
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun render(state: SettingsState) {
        when(state){
            is SettingsState.ShowSettedSettings -> {
                state.defaultPlaybackSpeed?.let {
                    binding.settingsSettedDefaultPlaybackSpeed.text = it
                }
                state.defaultSpeakerVolume?.let{
                    binding.settingsSettedDefaultSpeakerVolume.text = it
                }?:let {
                    binding.settingsSettedDefaultSpeakerVolume.text =
                        if (settingsViewModel.defaultSpeakerVolume == -1)
                            getString(R.string.device_volume)
                        else
                            "${settingsViewModel.defaultSpeakerVolume}%"
                }
                state.defaultScreenOrientation?.let{
                    binding.settingsSettedDefaultScreenOrientation.text =
                        when (it) {
                            SettingsViewModel.LANDSCAPE_ORIENTATION -> getString(R.string.landscape)
                            SettingsViewModel.PORTRAIT_ORIENTATION -> getString(R.string.portrait)
                            else -> getString(R.string.auto_detect)
                        }
                }?:let {
                    binding.settingsSettedDefaultScreenOrientation.text =
                        when (settingsViewModel.defaultScreenOrientation) {
                            SettingsViewModel.LANDSCAPE_ORIENTATION -> getString(R.string.landscape)
                            SettingsViewModel.PORTRAIT_ORIENTATION -> getString(R.string.portrait)
                            else -> getString(R.string.auto_detect)
                        }
                }
                state.subtitleFontSize?.let{
                    binding.settingsSettedSubtitleFontSize.text = "${it}sp"
                }?:let {
                    binding.settingsSettedSubtitleFontSize.text =
                        "${settingsViewModel.subtitleFontSize}sp"
                }
                state.subtitleTextColor?.let{
                    binding.settingsSettedSubtitleTextColor.setImageResource(
                        when (it) {
                            ContextCompat.getColor(requireContext(), R.color.colorSubtitleWhite) ->
                                R.drawable.shape_color_preview_white
                            ContextCompat.getColor(requireContext(), R.color.colorSubtitleBlack) ->
                                R.drawable.shape_color_preview_black
                            ContextCompat.getColor(requireContext(), R.color.colorSubtitleRed) ->
                                R.drawable.shape_color_preview_red
                            ContextCompat.getColor(requireContext(), R.color.colorSubtitleGreen) ->
                                R.drawable.shape_color_preview_green
                            ContextCompat.getColor(requireContext(), R.color.colorSubtitleBlue) ->
                                R.drawable.shape_color_preview_blue
                            ContextCompat.getColor(requireContext(),R.color.colorSubtitleYellow) ->
                                R.drawable.shape_color_preview_yellow
                            ContextCompat.getColor(requireContext(),R.color.colorSubtitleIndigo) ->
                                R.drawable.shape_color_preview_indigo
                            else ->
                                R.drawable.shape_color_preview_white
                    })
                }?:let {
                    if (settingsViewModel.subtitleTextColor == null) {
                        binding.settingsSettedSubtitleTextColor.setImageResource(
                            R.drawable.shape_color_preview_white
                        )
                    }
                }
                state.subtitleHighlightColor?.let{
                    binding.settingsSettedSubtitleHighlightColor.setImageResource(
                        when (it) {
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorSubtitleHighlightWhite
                            ) ->
                                R.drawable.shape_color_preview_white
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorSubtitleHighlightBlack
                            ) ->
                                R.drawable.shape_color_preview_black
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorSubtitleHighlightRed
                            ) ->
                                R.drawable.shape_color_preview_red
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorSubtitleHighlightGreen
                            ) ->
                                R.drawable.shape_color_preview_green
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorSubtitleHighlightBlue
                            ) ->
                                R.drawable.shape_color_preview_blue
                            ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightYellow) ->
                                R.drawable.shape_color_preview_yellow
                            ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightIndigo) ->
                                R.drawable.shape_color_preview_indigo
                            else ->
                                android.R.color.transparent
                        })
                }?:let {
                    if (settingsViewModel.subtitleHighlightColor == null) {
                        binding.settingsSettedSubtitleHighlightColor.setImageResource(
                            R.drawable.shape_color_preview_black
                        )
                    }
                }
            }
            is SettingsState.ShowPlaybackSpeedMenu -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.25f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_25x,
                            { changePlayBackSpeed(0.25f) }
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.5f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_50x,
                            { changePlayBackSpeed(0.5f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.75f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_75x,
                            { changePlayBackSpeed(0.75f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1x,
                            { changePlayBackSpeed(1f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.25f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_25x,
                            { changePlayBackSpeed(1.25f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.5f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_50x,
                            { changePlayBackSpeed(1.5f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.75f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_75x,
                            { changePlayBackSpeed(1.75f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 2f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._2x,
                            { changePlayBackSpeed(2f) }
                        )
                    )
                    show()
                }
            }
            is SettingsState.ShowSpeakerVolumeBottomSheet -> {
                SpeakerVolumeBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    primaryText = "Set"
                    secondaryText = "CANCEL"
                    defaultSpeakerVolume = settingsViewModel.defaultSpeakerVolume
                    onPrimaryClick = {speakerVolumetoSet ->
                        lifecycleScope.launch {
                            settingsViewModel.intents.send(
                                SettingsIntent.SetSpeakerVolume(speakerVolumetoSet)
                            )
                        }
                        dismiss()
                    }
                    onSecondaryClick = {
                        dismiss()
                    }
                    show()
                }
            }
            is SettingsState.ShowScreenOrientationBottomSheet -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultScreenOrientation ==
                                SettingsViewModel.AUTO_ORIENTATION)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string.auto_detect,
                            { changeScreenOrientation(SettingsViewModel.AUTO_ORIENTATION) }
                        ),BottomSheetItemEntity(
                            if (settingsViewModel.defaultScreenOrientation ==
                                SettingsViewModel.LANDSCAPE_ORIENTATION)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string.landscape,
                            { changeScreenOrientation(SettingsViewModel.LANDSCAPE_ORIENTATION) }
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultScreenOrientation ==
                                SettingsViewModel.PORTRAIT_ORIENTATION)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string.portrait,
                            { changeScreenOrientation(SettingsViewModel.PORTRAIT_ORIENTATION) }
                        )
                    )
                    show()
                }
            }
            is SettingsState.ShowSubtitleFontSizeBottomSheet -> {
                SubtitleFontSizeBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    primaryText = "Set"
                    secondaryText = "CANCEL"
                    sliderPosition = settingsViewModel.subtitleFontSize
                    onPrimaryClick = {fontSizeToSet ->
                        lifecycleScope.launch {
                            settingsViewModel.intents.send(
                                SettingsIntent.SetSubtitleFontSize(fontSizeToSet)
                            )
                        }
                        dismiss()
                    }
                    onSecondaryClick = {
                        dismiss()
                    }
                    show()
                }
            }
            is SettingsState.ShowSubtitleTextColorBottomSheet -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor == null ||
                                    settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleWhite))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_white,
                            R.string.white,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleWhite))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleBlack))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_black,
                            R.string.black,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleBlack))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleRed))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_red,
                            R.string.red,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleRed))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleGreen))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_green,
                            R.string.green,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleGreen))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleBlue))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_blue,
                            R.string.blue,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleBlue))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleYellow))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_yellow,
                            R.string.yellow,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleYellow))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleTextColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleIndigo))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_indigo,
                            R.string.indigo,
                            {changeSubtitleTextColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleIndigo))}
                        )
                    )
                    show()
                }
            }
            is SettingsState.ShowSubtitleHighlightColorBottomSheet -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),android.R.color.transparent))
                                R.drawable.ic_check
                            else
                                null,
                            null,
                            R.string.transparent,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),android.R.color.transparent)
                            )}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightWhite))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_white,
                            R.string.white,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightWhite))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor == null ||
                                settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightBlack))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_black,
                            R.string.black,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightBlack))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightRed))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_red,
                            R.string.red,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightRed))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightGreen))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_green,
                            R.string.green,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightGreen))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightBlue))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_blue,
                            R.string.blue,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightBlue))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightYellow))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_yellow,
                            R.string.yellow,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightYellow))}
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.subtitleHighlightColor ==
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightIndigo))
                                R.drawable.ic_check
                            else
                                null,
                            R.drawable.shape_color_preview_indigo,
                            R.string.indigo,
                            {changeSubtitleHighlightColor(
                                ContextCompat.getColor(requireContext(),R.color.colorSubtitleHighlightIndigo))}
                        )
                    )
                    show()
                }
            }
        }
    }

    private fun changeScreenOrientation(orientation: Int){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetScreenOrientation(orientation)
            )
        }
    }

    private fun changePlayBackSpeed(speed: Float){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetPlaybackSpeed(speed)
            )
        }
    }

    private fun changeSubtitleTextColor(color: Int){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetSubtitleTextColor(color)
            )
        }
    }

    private fun changeSubtitleHighlightColor(color: Int){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetSubtitleHighlightColor(color)
            )
        }
    }

}