package com.example.customplayer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import com.example.customplayer.PlayerConstant.ALLOCATION_SIZE
import com.example.customplayer.PlayerConstant.BACKWARD_INCREMENT
import com.example.customplayer.PlayerConstant.BACK_BUFFER_DURATION
import com.example.customplayer.PlayerConstant.BUFFER_FOR_PLAYBACK
import com.example.customplayer.PlayerConstant.BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
import com.example.customplayer.PlayerConstant.FORWARD_INCREMENT
import com.example.customplayer.PlayerConstant.MAX_BUFFER_DURATION
import com.example.customplayer.PlayerConstant.MIN_BUFFER_DURATION
import com.example.customplayer.databinding.CustomControlBinding
import com.example.customplayer.databinding.PrashantCustomPlayerBinding

@UnstableApi class PrashantCustomPlayer (
    private val context:AppCompatActivity,
    attrSet:AttributeSet,
    defStyleAttr:Int) : FrameLayout(context,attrSet,defStyleAttr){

    private var mediaPlayer:ExoPlayer? =  null
    private val mediaPlayerView:PlayerView? = null
    private var playerSdkCallBack:PlayerSdkCallBack? = null
    private var contentUrl: String? = null
    private var contentType:ContentType? = null
    private var contentTitle:String? = null
    private var contentId:String? = null
    private var token:String? = null
    private lateinit var bindingPlayer: PrashantCustomPlayerBinding
    private lateinit var bindingController:CustomControlBinding
        constructor(context:Context,attrs:AttributeSet) :this(
            context as AppCompatActivity,attrs,0
        )

    override fun onFinishInflate() {
        super.onFinishInflate()
        bindingPlayer = PrashantCustomPlayerBinding.inflate(LayoutInflater.from(context), this, true)
        bindingController = CustomControlBinding.inflate(LayoutInflater.from(context),this,true)
        setUpControlClickListeners()
    }

    private fun setUpControlClickListeners() {
       bindingController.root.setOnClickListener { view->
           when(view.id) {
               bindingController.playBtn.id->{
                  resumePlayer()
               }
               bindingController.ivBack.id ->{
                   playerSdkCallBack?.onPlayerBackPressed()
               }
               bindingController.skipFwdBtn.id->{
                   val currentPosition = mediaPlayer?.currentPosition
                   val nextPosition = currentPosition?.plus(10000)
                   mediaPlayer?.seekTo(nextPosition as Long)
               }
               bindingController.skipPreBtn.id->{
                   val currentPosition = mediaPlayer?.currentPosition
                   val nextPosition = currentPosition?.minus(10000)
                   mediaPlayer?.seekTo(nextPosition as Long)
               }
               bindingController.nextVideoBtn.id->{
                   playerSdkCallBack?.onPlayNextContent()
               }
               bindingController.preVideoBtn.id->{
                   playerSdkCallBack?.onPlayPreviousContent()
               }
               bindingController.ivMaximise.id->{
                   playerSdkCallBack?.onFullScreenEnter()
               }
               bindingController.ivScale.id->{

               }
           }
       }
    }

    private fun initializePlayer(instantPlay:Boolean){

        if (mediaPlayer != null){
            mediaPlayer?.release()
        }

        val customLoadControl = getCustomLoadControl()
        val trackSelector = DefaultTrackSelector(context)
        //trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context).setRendererDisabled(C.TRACK_TYPE_VIDEO,true).build()
        mediaPlayer = getMediaPLayerInstance(customLoadControl,trackSelector)
        mediaPlayer?.addListener(playerStateListener)
        mediaPlayerView?.player = mediaPlayer
        mediaPlayerView?.controllerHideOnTouch = true
        mediaPlayerView?.setControllerHideDuringAds(true)
        val isDrm = isDrmContent(contentUrl.toString())
        val mediaItem = getMediaItem(drm = isDrm, videoUrl = contentUrl.toString())
        mediaPlayer?.setMediaItem(mediaItem)
        mediaPlayer?.prepare()
        if (instantPlay){
            mediaPlayer?.playWhenReady = true
        }
    }

    private fun getMediaItem(drm: Boolean, videoUrl: String ,
                             drmLicenseUrl: String? = null,
                             adsUrl: String? = null,
                             subtitle: MediaItem.SubtitleConfiguration? = null): MediaItem {

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(videoUrl)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(contentTitle).build())

        if (drm && !drmLicenseUrl.isNullOrEmpty()) {
            val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(drmLicenseUrl)
                .build()
            mediaItemBuilder.setDrmConfiguration(drmConfig)
        }

        if (!adsUrl.isNullOrEmpty()) {
            val adTagUri = Uri.parse(adsUrl)
            mediaItemBuilder.setAdsConfiguration(MediaItem.AdsConfiguration.Builder(adTagUri).build())
        }

        return mediaItemBuilder.build()
    }

    private fun getCustomLoadControl(): LoadControl {

        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(MIN_BUFFER_DURATION, MAX_BUFFER_DURATION,
                BUFFER_FOR_PLAYBACK, BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER)
            .setAllocator(DefaultAllocator(true, ALLOCATION_SIZE))
            .setBackBuffer(BACK_BUFFER_DURATION,false)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .build()

    }

    private fun getMediaPLayerInstance(customLoadControl:LoadControl,trackSelector:TrackSelector): ExoPlayer{

        return ExoPlayer.Builder(context)
            .setLoadControl(customLoadControl)
            .setTrackSelector(trackSelector)
            .setSeekForwardIncrementMs(FORWARD_INCREMENT.toLong())
            .setSeekForwardIncrementMs(BACKWARD_INCREMENT.toLong())
            .build()
    }

    private var playerStateListener:Player.Listener = object :Player.Listener{

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
           when(playbackState){

                Player.STATE_READY->{

                    playerSdkCallBack?.onBufferingEnded()
                    playerSdkCallBack?.onVideoStart()
                    //stop showing Buffering here
               }

               Player.STATE_IDLE->{

                   mediaPlayerView?.videoSurfaceView?.visibility = VISIBLE
                   mediaPlayerView?.visibility = VISIBLE
                   mediaPlayerView?.bringToFront()
               }

               Player.STATE_BUFFERING->{

                   playerSdkCallBack?.onBufferingStart()
                   //start showing buffering view here
               }

               Player.STATE_ENDED->{

                   //need to call onPlayNextVideo if it is part of series else onvideostop
                   playerSdkCallBack?.onVideoStop()
               }
           }
        }

        override fun onPlayerError(error: PlaybackException) {
            playerSdkCallBack?.onPlayerError()
            super.onPlayerError(error)
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            super.onSurfaceSizeChanged(width, height)
        }
    }

    private fun isDrmContent(videoUrl:String) : Boolean{
        return videoUrl.split("\\.".toRegex())[1] == "mpd"
    }

    //Video Player functions to be called from implementing activity/fragment/class directly

    fun setContentFilePath(url: String){
        contentUrl = url
    }
    fun startPlayer(){
        initializePlayer(true)
    }

    fun pausePlayer(){
        if (mediaPlayer != null && mediaPlayerView != null){
            mediaPlayerView.onPause()
            mediaPlayer?.playWhenReady = false
            playerSdkCallBack?.onVideoStop()
        }
    }

    fun resumePlayer(){
        if (mediaPlayer != null && mediaPlayerView != null){
            mediaPlayerView.onResume()
            mediaPlayer?.playWhenReady = true
            playerSdkCallBack?.onVideoStart()
        }
    }

    fun releasePlayer(){
        if (mediaPlayer != null && mediaPlayerView != null){
            mediaPlayer?.release()
            mediaPlayerView.player?.release()
            playerSdkCallBack?.onPlayerRelease()
        }
    }

    fun setKeyToken(key:String){
        token = key
    }

    fun setContentMetaData(contentType:ContentType,contentTitle:String? ="",contentId:String){
        this.contentType = contentType
        if (contentTitle?.isNotEmpty() == true){
            this.contentTitle = contentTitle
        }
        this.contentId = contentId
    }
}