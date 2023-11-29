package com.example.ptplayer.player

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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
import com.example.ptplayer.R
import com.example.ptplayer.player.PlayerConstant.ALLOCATION_SIZE
import com.example.ptplayer.player.PlayerConstant.BACKWARD_INCREMENT
import com.example.ptplayer.player.PlayerConstant.BACK_BUFFER_DURATION
import com.example.ptplayer.player.PlayerConstant.BUFFER_FOR_PLAYBACK
import com.example.ptplayer.player.PlayerConstant.BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
import com.example.ptplayer.player.PlayerConstant.FORWARD_INCREMENT
import com.example.ptplayer.player.PlayerConstant.MAX_BUFFER_DURATION
import com.example.ptplayer.player.PlayerConstant.MIN_BUFFER_DURATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi class PrashantCustomPlayer (
    private val context:AppCompatActivity,
    attrSet:AttributeSet,
    defStyleAttr:Int) : FrameLayout(context,attrSet,defStyleAttr){

    private var mediaPlayer:ExoPlayer? =  null
    private var mediaPlayerView:PlayerView? = null
    private var playerSdkCallBack: PlayerSdkCallBack? = null
    private var contentUrl: String? = null
    private var contentType: ContentType? = null
    private var contentTitle:String? = null
    private var contentId:String? = null
    private var token:String? = null

    private var backButton:ImageView? = null
    private var skipPre:ImageView? = null
    private var playButton:ImageView? = null
    private var pauseButton:ImageView? = null
    private var skipFwd:ImageView? = null
    private var preTrack:ImageView? = null
    private var nextTrack:ImageView? = null
    private var volumeIcon:ImageView? = null
    private var settings:ImageView? = null
    private var scrubImage:ImageView? = null
    private val scrubber:CustomScrubber = CustomScrubber(context)
    private var playerScrub:CustomScrubber? = null
    private var currentProgress:TextView? = null
    private var totalProgress:TextView? = null
    private var playerBuffer:LoadingView? = null
    private var flPreview:FrameLayout? = null
    private var job: Job? = null
    private val scope = MainScope()

    private var tvContentTitle:TextView? = null
        constructor(context:Context,attrs:AttributeSet) :this(
            context as AppCompatActivity,attrs,0
        )

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view =
            LayoutInflater.from(getContext()).inflate(R.layout.prashant_custom_player, this)
        fetchAllId(view)
        setUpControlClickListeners(view)
        setClickListenerOnViews(::setUpControlClickListeners)
    }

    private fun setClickListenerOnViews(kFunction1: (View) -> Unit) {

        mediaPlayerView?.setOnClickListener(kFunction1)
        backButton?.setOnClickListener(kFunction1)
        skipFwd?.setOnClickListener(kFunction1)
        skipPre?.setOnClickListener(kFunction1)
        playButton?.setOnClickListener(kFunction1)
        pauseButton?.setOnClickListener(kFunction1)
        preTrack?.setOnClickListener(kFunction1)
        nextTrack?.setOnClickListener(kFunction1)
        volumeIcon?.setOnClickListener(kFunction1)
        settings?.setOnClickListener(kFunction1)
        scrubImage?.setOnClickListener(kFunction1)
        playerScrub?.setOnClickListener(kFunction1)
    }

    private fun fetchAllId(view: View) {

        mediaPlayerView = view.findViewById(R.id.media_player_view)
        backButton=view.findViewById(R.id.iv_back)
        skipFwd= view.findViewById(R.id.skip_fwd_btn)
        skipPre= view.findViewById(R.id.skip_pre_btn)
        playButton= view.findViewById(R.id.play_btn)
        pauseButton= view.findViewById(R.id.pause_btn)
        preTrack= view.findViewById(R.id.pre_video_btn)
        nextTrack= view.findViewById(R.id.next_video_btn)
        volumeIcon= view.findViewById(R.id.volume_btn)
        settings= view.findViewById(R.id.iv_setting)
        scrubImage= view.findViewById(R.id.imageView)
        tvContentTitle = view.findViewById(R.id.content_title)
        playerScrub = view.findViewById(R.id.player_scrub)
        currentProgress = view.findViewById(R.id.current_progress)
        totalProgress = view.findViewById(R.id.total_progress)
        playerBuffer = view.findViewById(R.id.player_buffer)
        flPreview = view.findViewById(R.id.previewFrameLayout)

    }

    private fun setUpControlClickListeners(view:View) {
       view.setOnClickListener { viewClicked->
           when(viewClicked.id) {
               R.id.play_btn->{
                  resumePlayer()
               }
               R.id.pause_btn->{
                   pausePlayer()
               }
               R.id.iv_back ->{
                   playerSdkCallBack?.onPlayerBackPressed()
               }
               R.id.skip_fwd_btn->{
                   val currentPosition = mediaPlayer?.currentPosition
                   val nextPosition = currentPosition?.plus(FORWARD_INCREMENT)
                   mediaPlayer?.seekTo(nextPosition as Long)
               }
               R.id.skip_pre_btn->{
                   val currentPosition = mediaPlayer?.currentPosition
                   val nextPosition = currentPosition?.minus(BACKWARD_INCREMENT)
                   mediaPlayer?.seekTo(nextPosition as Long)
               }
               R.id.next_video_btn->{
                   playerSdkCallBack?.onPlayNextContent()
               }
               R.id.pre_video_btn->{
                   playerSdkCallBack?.onPlayPreviousContent()
               }

               R.id.player_scrub->{
                   val progress = mediaPlayer?.duration?.let { mediaPlayer?.currentPosition?.toFloat()?.div(it) }
                   if (progress != null) {
                       mediaPlayer?.seekTo((progress * mediaPlayer?.duration!!).toLong())
                   }
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
        mediaPlayer = getMediaPLayerInstance(customLoadControl,trackSelector)
        mediaPlayer?.addListener(playerStateListener)
        mediaPlayerView?.player = mediaPlayer
        mediaPlayerView?.controllerHideOnTouch = true
        mediaPlayerView?.keepScreenOn = true
        mediaPlayerView?.setControllerHideDuringAds(true)
        val isDrm = isDrmContent(contentUrl.toString())
        val mediaItem = getMediaItem(drm = isDrm, videoUrl = contentUrl.toString())
        mediaPlayer?.setMediaItem(mediaItem)
        mediaPlayer?.prepare()
        mediaPlayer?.playWhenReady = true
        tvContentTitle?.text = contentTitle
        mediaPlayerView?.bringToFront()
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
            .setSeekBackIncrementMs(BACKWARD_INCREMENT.toLong())
            .build()
    }

    private var playerStateListener:Player.Listener = object :Player.Listener{

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {

           when(playbackState){


                Player.STATE_READY->{

                    hideLoadingView()
                    playerSdkCallBack?.onBufferingEnded()
                    playerSdkCallBack?.onVideoStart()
                    mediaPlayerView?.visibility = VISIBLE
                    mediaPlayerView?.videoSurfaceView?.visibility = VISIBLE
                    mediaPlayerView?.visibility = VISIBLE
                    totalProgress?.text = convertMsToMinSec(mediaPlayer?.duration as Long)
                    mediaPlayerView?.bringToFront()
                    startUpdates()
                    //stop showing Buffering here
               }

               Player.STATE_IDLE->{

                   mediaPlayerView?.videoSurfaceView?.visibility = VISIBLE
                   mediaPlayerView?.visibility = VISIBLE
                   mediaPlayerView?.bringToFront()
                   stopUpdates()
               }

               Player.STATE_BUFFERING->{

                   showLoadingView()
                   playerSdkCallBack?.onBufferingStart()
                   stopUpdates()
                   //start showing buffering view here
               }

               Player.STATE_ENDED->{

                   //need to call onPlayNextVideo if it is part of series else onVideoStop
                   playerSdkCallBack?.onVideoStop()
                   stopUpdates()
               }
           }
        }

        override fun onPlayerError(error: PlaybackException) {
            playerSdkCallBack?.onPlayerError()
            super.onPlayerError(error)
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateScrubberPosition()
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
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
            mediaPlayerView?.onPause()
            mediaPlayer?.playWhenReady = false
            playButton?.isVisible = true
            pauseButton?.isVisible = false
            playerSdkCallBack?.onVideoStop()
        }
    }

    fun resumePlayer(){
        if (mediaPlayer != null && mediaPlayerView != null){
            mediaPlayerView?.onResume()
            mediaPlayer?.playWhenReady = true
            playButton?.isVisible = false
            pauseButton?.isVisible = true
            playerSdkCallBack?.onVideoStart()
        }
    }

    fun releasePlayer(){
        if (mediaPlayer != null && mediaPlayerView != null){
            mediaPlayer?.release()
            mediaPlayerView?.player?.release()
            playerSdkCallBack?.onPlayerRelease()
        }
    }

    fun setKeyToken(key:String){
        token = key
    }

    fun setContentMetaData(contentType: ContentType, contentTitle:String? ="", contentId:String){
        this.contentType = contentType
        if (contentTitle?.isNotEmpty() == true){
            this.contentTitle = contentTitle
        }
        this.contentId = contentId
    }
    fun setVideoPlayerSdkListener(playerSdkCallBack: PlayerSdkCallBack) {
        this.playerSdkCallBack = playerSdkCallBack
    }

    fun updateScrubberPosition(){
     startUpdates()
    }

    private fun stopUpdates() {
        job?.cancel()
        job = null
    }

    private fun startUpdates() {
        stopUpdates()
        job = scope.launch {
            while(true) {
                val progress = mediaPlayer?.duration?.let { mediaPlayer?.currentPosition?.toFloat()?.div(it) }
                currentProgress?.text = convertMsToMinSec(mediaPlayer?.currentPosition as Long)
                withContext(Dispatchers.Main){
                    scrubber.setProgress(progress as Float, mediaPlayer?.duration as Long)
                }
                delay(1000)
            }
        }
    }

    private fun convertMsToMinSec(ms:Long):String{

        val minutes = (ms / (1000 * 60)) % 60
        val seconds = (ms / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun showLoadingView(){
        playerBuffer?.isVisible = true

    }

    private fun hideLoadingView(){
        playerBuffer?.isVisible = false

    }
}