package me.pitok.player.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class IndictableSimpleExoPlayer(
    context: Context,
    val trackSelector: DefaultTrackSelector = DefaultTrackSelector(
        context
    )
) : SimpleExoPlayer(
    Builder(
        context,
        DefaultRenderersFactory(context),
        trackSelector,
        DefaultMediaSourceFactory(context),
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                10 * 1000,
                60 * 1000,
                2000,
                3000
            )
            .build(),
        DefaultBandwidthMeter.getSingletonInstance(context),
        AnalyticsCollector(Clock.DEFAULT),
    )
) {


    var onPositionChanged: (position: Long) -> Unit = { _ -> }
    private var job1: CoroutineContext? = null
    private var lastPositionSent = 0L


    private fun enableOnProgressChanged() {
        lastPositionSent = System.currentTimeMillis()
        job1 = GlobalScope.launch {
            while (true) {
                if (System.currentTimeMillis() - lastPositionSent < 200) {
                    delay(75)
                    continue
                }
                withContext(Dispatchers.Main) {
                    onPositionChanged.invoke(currentPosition)
                }
                lastPositionSent = System.currentTimeMillis()
            }
        }
    }

    private fun disableOnProgressChanged() {
        job1?.cancel()
        job1 = null
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady && job1 == null) {
            enableOnProgressChanged()
        } else if (!playWhenReady && job1 != null) {
            disableOnProgressChanged()
        }
        super.setPlayWhenReady(playWhenReady)
    }

    fun selectTrackGroup(groupIndex: Int) {
        trackSelector
            .currentMappedTrackInfo
            ?.getTrackGroups(C.TRACK_TYPE_AUDIO)?.let { trackGroupArray ->
                val builder = trackSelector.parameters.buildUpon()
                val override = DefaultTrackSelector.SelectionOverride(groupIndex, 0)
                builder.clearSelectionOverrides(C.TRACK_TYPE_AUDIO)
                    .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                builder.setSelectionOverride(
                    C.TRACK_TYPE_AUDIO,
                    trackGroupArray,
                    override
                )
                trackSelector.setParameters(builder)
            }
    }

    fun disableAudioRenderer() {
        val builder = trackSelector.parameters.buildUpon()
        builder.clearSelectionOverrides(C.TRACK_TYPE_AUDIO)
            .setRendererDisabled(C.TRACK_TYPE_AUDIO, true)
        trackSelector.setParameters(builder)
    }

    fun isGroupIndexSelected(groupIndex: Int): Boolean {
        if (!isAudioRendererEnabled()) return false
        trackSelector.currentMappedTrackInfo?.getTrackGroups(C.TRACK_TYPE_AUDIO)
            ?.let { trackGroupArray ->
                trackSelector.parameters.getSelectionOverride(C.TRACK_TYPE_AUDIO, trackGroupArray)
                    ?.let { override ->
                        return override.groupIndex == groupIndex
                    } ?: let {
                    for (formatIndex in 0 until trackGroupArray.get(groupIndex).length) {
                        // selectionFlag for default tracks is 0 and I don't know why =))
                        if (trackGroupArray
                                .get(groupIndex)
                                .getFormat(formatIndex).selectionFlags == 0
                        ) {
                            return true
                        }
                    }
                    return false
                }
            } ?: let {
            return false
        }
    }

    fun isAudioRendererEnabled(): Boolean {
        return !trackSelector.parameters.getRendererDisabled(C.TRACK_TYPE_AUDIO)
    }
}