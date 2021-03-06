package com.encoders.myexoplayer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util

class MainActivity :  AppCompatActivity(), Player.EventListener,
    AdErrorEvent.AdErrorListener,  AdEvent.AdEventListener{

    private var player: SimpleExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var adsLoader: ImaAdsLoader? = null
    private lateinit var playerView: StyledPlayerView

    val ADS_URL = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
    }
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override  fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String
        stateString = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE "
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING "
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY "
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED "
            else -> "UNKNOWN_STATE "
        }
        Log.d("TAG", "changed state to $stateString")
    }

    override fun onAdEvent(adEvent: AdEvent?) {
        adEvent?.type?.name.let { Log.d("ADS", "AdEvent listener :: $it!!") }
    }


    override fun onAdError(adErrorEvent: AdErrorEvent?) {
        adErrorEvent?.error?.message?.let { Log.d("ADS", "AdError listener :: $it!!") }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.removeListener(this);
            player!!.release()
            player = null
        }
    }



    fun initializePlayer() {

        val httpDataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSourceFactory(
            ExoPlayerLibraryInfo.DEFAULT_USER_AGENT,
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,  /* allowCrossProtocolRedirects= */
            true
        )
        val adsProvider : DefaultMediaSourceFactory.AdsLoaderProvider = DefaultMediaSourceFactory.AdsLoaderProvider {
            getAdsLoaderProvider()
        }


        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(this)
            .setAdsLoaderProvider(adsProvider)
            .setAdViewProvider(playerView)
            .setDrmHttpDataSourceFactory(httpDataSourceFactory)

        player = SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        playerView.setPlayer(player)
        player!!.setMediaItem(getMediaItem())

        // prepare media list
        player!!.setPlayWhenReady(playWhenReady);
        player!!.seekTo(currentWindow, playbackPosition);
        player!!.addListener(this);
        player!!.prepare();
    }




    fun getMediaItem() : MediaItem {
        // add one media item
        // val mediaItem: MediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
        return MediaItem.Builder()
            .setUri(Uri.parse("https://enabatalmanasek.com/project/request/videos/232/EgNfKHn9S8i76pN3OkVaTmKl0YoOtZ.mp4"))
            .setMimeType(MimeTypes.BASE_TYPE_VIDEO)
            .setAdTagUri(Uri.parse(ADS_URL))
            .build()
    }

    private fun getAdsLoaderProvider() : ImaAdsLoader {
        adsLoader = ImaAdsLoader.Builder(this)
            .setAdErrorListener(this)
            .setAdEventListener(this)
            .build()
        adsLoader!!.setPlayer(player)
        return adsLoader!!
    }






}