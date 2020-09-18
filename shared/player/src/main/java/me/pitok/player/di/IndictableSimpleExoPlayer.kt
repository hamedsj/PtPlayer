package me.pitok.player.di

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class IndictableSimpleExoPlayer(context: Context): SimpleExoPlayer(
    context,
    DefaultRenderersFactory(context),
    DefaultTrackSelector(context),
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
}