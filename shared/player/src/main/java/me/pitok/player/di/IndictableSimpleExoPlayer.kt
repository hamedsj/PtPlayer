package me.pitok.player.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import me.pitok.logger.Logger
import java.util.concurrent.TimeUnit

class IndictableSimpleExoPlayer(context: Context, val trackSelector: DefaultTrackSelector = DefaultTrackSelector(context)): SimpleExoPlayer(
    context,
    DefaultRenderersFactory(context),
    trackSelector,
    DefaultLoadControl(),
    DefaultBandwidthMeter.getSingletonInstance(context),
    AnalyticsCollector(Clock.DEFAULT),
    Clock.DEFAULT,
    Util.getLooper()
) {


    private var positionObservable : Observable<Long>? = null
    private var positionDisposable : Disposable? = null
    var onPositionChanged : (position: Long) -> Unit = {_->}

    private fun enableOnProgressChanged(){
        positionObservable =  Observable.interval(200, TimeUnit.MILLISECONDS)
            .map { currentPosition }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { position ->
                onPositionChanged.invoke(position)
            }
        positionDisposable = positionObservable?.subscribe()
    }

    private fun disableOnProgressChanged(){
        if (positionDisposable != null &&  !requireNotNull(positionDisposable?.isDisposed)) {
            positionDisposable?.dispose()
        }
        positionDisposable = null
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady && positionDisposable == null) {
            enableOnProgressChanged()
        }else if (!playWhenReady && positionDisposable != null){
            disableOnProgressChanged()
        }
        super.setPlayWhenReady(playWhenReady)
    }

    fun selectTrackGroup(groupIndex: Int){
        trackSelector
            .currentMappedTrackInfo
            ?.getTrackGroups(C.TRACK_TYPE_AUDIO)?.let { trackGroupArray ->
                val builder= trackSelector.parameters.buildUpon()
                val override = DefaultTrackSelector.SelectionOverride(groupIndex, 0)
                builder.clearSelectionOverrides(C.TRACK_TYPE_AUDIO)
                    .setRendererDisabled(C.TRACK_TYPE_AUDIO, false);
                builder.setSelectionOverride(
                    C.TRACK_TYPE_AUDIO,
                    trackGroupArray,
                    override
                )
                trackSelector.setParameters(builder)
            }
    }

    fun disableAudioRenderer(){
        val builder= trackSelector.parameters.buildUpon()
        builder.clearSelectionOverrides(C.TRACK_TYPE_AUDIO)
            .setRendererDisabled(C.TRACK_TYPE_AUDIO, true);
        trackSelector.setParameters(builder)
    }

    fun isGroupIndexSelected(groupIndex: Int): Boolean{
        if (!isAudioRendererEnabled()) return false
        trackSelector.currentMappedTrackInfo?.getTrackGroups(C.TRACK_TYPE_AUDIO)?.let{trackGroupArray ->
            trackSelector.parameters.getSelectionOverride(C.TRACK_TYPE_AUDIO, trackGroupArray)?.let {override ->
                return override.groupIndex == groupIndex
            }?:let {
                for(formatIndex in 0 until trackGroupArray.get(groupIndex).length){
                    // selectionFlag for default tracks is 0 and I don't know why =))
                    if (trackGroupArray
                            .get(groupIndex)
                            .getFormat(formatIndex).selectionFlags == 0){
                        return true
                    }
                }
                return false
            }
        }?:let{
            return false
        }
    }

    fun isAudioRendererEnabled(): Boolean{
        return !trackSelector.parameters.getRendererDisabled(C.TRACK_TYPE_AUDIO)
    }
}