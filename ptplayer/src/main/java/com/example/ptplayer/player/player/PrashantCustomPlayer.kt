package com.example.ptplayer.player.player

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.graphics.Bitmap
import android.graphics.Color.*
import android.graphics.PorterDuff
import android.media.AudioManager
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnKeyListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.AdViewProvider
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.AdsConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector.MappedTrackInfo
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.ptplayer.R
import com.example.ptplayer.player.constants.ContentType
import com.example.ptplayer.player.constants.PlayerConstant.ALLOCATION_SIZE
import com.example.ptplayer.player.constants.PlayerConstant.BACKWARD_INCREMENT
import com.example.ptplayer.player.constants.PlayerConstant.BACK_BUFFER_DURATION
import com.example.ptplayer.player.constants.PlayerConstant.BUFFER_FOR_PLAYBACK
import com.example.ptplayer.player.constants.PlayerConstant.BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
import com.example.ptplayer.player.constants.PlayerConstant.FORWARD_INCREMENT
import com.example.ptplayer.player.constants.PlayerConstant.INVALID_CONTENT_ID
import com.example.ptplayer.player.constants.PlayerConstant.MAX_BUFFER_DURATION
import com.example.ptplayer.player.constants.PlayerConstant.MIN_BUFFER_DURATION
import com.example.ptplayer.player.constants.PlayerConstant.MPD
import com.example.ptplayer.player.interfaces.PlayerSdkCallBack
import com.example.ptplayer.player.utils.GlideThumbnailTransformation
import com.example.ptplayer.player.utils.convertSpriteData
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@UnstableApi
class PrashantCustomPlayer(
    private val context: AppCompatActivity,
    attrSet: AttributeSet,
    defStyleAttr: Int
) : FrameLayout(context, attrSet, defStyleAttr), OnScrubListener {

    private var mediaPlayer: ExoPlayer? = null
    private var mediaPlayerView: PlayerView? = null
    private var playerSdkCallBack: PlayerSdkCallBack? = null
    private var contentUrl: String? = null
    private var contentType: ContentType? = null
    private var contentTitle: String? = null
    private var contentId: String? = null
    private var token: String? = null
    private var adUrl: String? = null
    private var currentVolume:Float? = null
    private var adsLoader: ImaAdsLoader? = null

    private var skipPre: ImageView? = null
    private var playButton: ImageView? = null
    private var pauseButton: ImageView? = null
    private var skipFwd: ImageView? = null
    private var preTrack: ImageView? = null
    private var nextTrack: ImageView? = null
    private var volumeIcon: ImageView? = null
    private var muteIcon:ImageView? = null
    private var settings: ImageView? = null
    private var replay:ImageView? = null
    private var scrubImage: ImageView? = null
    private var playerScrub: DefaultTimeBar? = null
    private var flPreview: FrameLayout? = null
    private var tvContentTitle: TextView? = null
    private var audioManager: AudioManager? = null
    private var screenMode: ImageView? = null
    private var customControl: ConstraintLayout? = null
    private var spriteData: Bitmap? = null
    private var spriteUrl: String? = null
    private var videoFormatList = java.util.ArrayList<Format>()
    private var videoSubTitleList = java.util.ArrayList<Format>()
    private var isReplayEnabled:Boolean = false
    private var llCentre: LinearLayout? = null
    private var llBottom: LinearLayout? = null
    private var clTime:ConstraintLayout? = null

    private var job: Job? = null
    private val scope = MainScope()

    constructor(context: Context, attrs: AttributeSet) : this(
        context as AppCompatActivity, attrs, 0
    )

    private fun buildAdEventListener(): AdEvent.AdEventListener {
        return AdEvent.AdEventListener { adEvent ->
            // Handle ad events
            when (adEvent.type) {
                AdEvent.AdEventType.LOADED -> {
                    // Ad loaded
                    val loaded = 1
                }

                AdEvent.AdEventType.STARTED -> {
                    val loaded = 1
                }

                AdEvent.AdEventType.COMPLETED -> {
                    val loaded = 1
                }
                // Handle other ad event types as needed
                else -> {

                }
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view =
            LayoutInflater.from(getContext()).inflate(R.layout.prashant_custom_player, this)
        fetchAllId(view)
        setClickListenerOnViews()
        setUpControlClickListeners(view)
        audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        val focusChangeListener = OnFocusChangeListener { viewFocus, hasFocus ->
            if (hasFocus) {
                val color = ContextCompat.getColor(context, R.color.aol_secondary)
                (viewFocus as? ImageView)?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            } else {
                (viewFocus as? ImageView)?.colorFilter = null
            }
        }
        setOnFocusListenerOnViews(focusChangeListener)
    }

    private fun setClickListenerOnViews() {

        val enterKeyListener = OnKeyListener { view, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            ) {
                setUpControlClickListeners(view)
                return@OnKeyListener true
            }
            false
        }

        mediaPlayerView?.setOnKeyListener(enterKeyListener)
        skipFwd?.setOnKeyListener(enterKeyListener)
        skipPre?.setOnKeyListener(enterKeyListener)
        playButton?.setOnKeyListener(enterKeyListener)
        pauseButton?.setOnKeyListener(enterKeyListener)
        preTrack?.setOnKeyListener(enterKeyListener)
        nextTrack?.setOnKeyListener(enterKeyListener)
        volumeIcon?.setOnKeyListener(enterKeyListener)
        muteIcon?.setOnKeyListener(enterKeyListener)
        settings?.setOnKeyListener(enterKeyListener)
        scrubImage?.setOnKeyListener(enterKeyListener)
        screenMode?.setOnKeyListener(enterKeyListener)
        replay?.setOnKeyListener(enterKeyListener)
        playerScrub?.addListener(this)
    }

    private fun setOnFocusListenerOnViews(onFocusChangeListener: OnFocusChangeListener) {

        skipFwd?.onFocusChangeListener = onFocusChangeListener
        skipPre?.onFocusChangeListener = onFocusChangeListener
        playButton?.onFocusChangeListener = onFocusChangeListener
        pauseButton?.onFocusChangeListener = onFocusChangeListener
        preTrack?.onFocusChangeListener = onFocusChangeListener
        nextTrack?.onFocusChangeListener = onFocusChangeListener
        volumeIcon?.onFocusChangeListener = onFocusChangeListener
        muteIcon?.onFocusChangeListener = onFocusChangeListener
        settings?.onFocusChangeListener = onFocusChangeListener
        scrubImage?.onFocusChangeListener = onFocusChangeListener
        screenMode?.onFocusChangeListener = onFocusChangeListener
        replay?.onFocusChangeListener = onFocusChangeListener

    }

    private fun fetchAllId(view: View) {

        mediaPlayerView = view.findViewById(R.id.media_player_view_video)
        skipFwd = view.findViewById(R.id.skip_fwd_btn)
        skipPre = view.findViewById(R.id.skip_pre_btn)
        playButton = view.findViewById(R.id.play_btn)
        pauseButton = view.findViewById(R.id.pause_btn)
        volumeIcon = view.findViewById(R.id.volume_btn)
        muteIcon = view.findViewById(R.id.mute_btn)
        scrubImage = view.findViewById(R.id.imageView)
        tvContentTitle = view.findViewById(R.id.content_title)
        playerScrub = view.findViewById(R.id.player_scrub)
        flPreview = view.findViewById(R.id.previewFrameLayout)
        customControl = view.findViewById(R.id.custom_control)
        preTrack = view.findViewById(R.id.previous)
        nextTrack = view.findViewById(R.id.next)
        settings = view.findViewById(R.id.exo_settings)
        screenMode = view.findViewById(R.id.iv_screen_mode)
        llCentre = view.findViewById(R.id.ll_centre)
        llBottom = view.findViewById(R.id.ll_bottom)
        clTime = view.findViewById(R.id.cl_time)
        replay = view.findViewById(R.id.replay)

    }

    private fun setUpControlClickListeners(view: View) {
        when (view.id) {
            R.id.play_btn -> {
                resumePlayer()
            }

            R.id.pause_btn -> {
                pausePlayer()
            }

            R.id.skip_fwd_btn -> {
                val currentPosition = mediaPlayer?.currentPosition
                val nextPosition = currentPosition?.plus(FORWARD_INCREMENT)
                mediaPlayer?.seekTo(nextPosition as Long)
            }

            R.id.skip_pre_btn -> {
                val currentPosition = mediaPlayer?.currentPosition
                val nextPosition = currentPosition?.minus(BACKWARD_INCREMENT)
                mediaPlayer?.seekTo(nextPosition as Long)
            }

            R.id.iv_screen_mode -> {
                setMiniPlayerLayout(true)
            }

            R.id.exo_settings -> {

                playerSdkCallBack?.onSettingClicked()
            }

            R.id.volume_btn ->{

                volumeIcon?.isVisible = false
                muteIcon?.isVisible = true
                muteIcon?.requestFocus()
                currentVolume = mediaPlayer?.volume
                mediaPlayer?.volume = 0f
            }

            R.id.mute_btn ->{

                mediaPlayer?.volume = currentVolume as Float
                muteIcon?.isVisible = false
                volumeIcon?.isVisible = true
                volumeIcon?.requestFocus()
            }

            R.id.previous ->{

                playerSdkCallBack?.onPlayPreviousContent()
                preTrack?.requestFocus()

            }

            R.id.next ->{

                playerSdkCallBack?.onPlayNextContent()
                nextTrack?.requestFocus()
            }

            R.id.replay ->{
                initializePlayer()
            }
        }
    }

    private fun initializePlayer() {

        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }

        hideControlWithReplay(false)
       /* adsLoader = ImaAdsLoader.Builder(context)
            .setAdEventListener(buildAdEventListener())
            .build()*/
        if (contentUrl?.isNotEmpty() == true) {

            val customLoadControl = getCustomLoadControl()
            val trackSelector = DefaultTrackSelector(context)
            mediaPlayer = getMediaPLayerInstance(customLoadControl, trackSelector)
            mediaPlayer?.addListener(playerStateListener)
            mediaPlayerView?.player = mediaPlayer
            mediaPlayerView?.controllerHideOnTouch = true
            mediaPlayerView?.keepScreenOn = true
            mediaPlayerView?.setControllerHideDuringAds(true)
            val isDrm = isDrmContent(contentUrl.toString())
            val subtitle: MutableList<MediaItem.SubtitleConfiguration>?
            val mediaItem = getMediaItem(
                drm = isDrm,
                videoUrl = contentUrl.toString(),
                drmLicenseUrl = token,
                adsUrl = adUrl
            )
            mediaPlayer?.setMediaItem(mediaItem)
            mediaPlayer?.prepare()
            mediaPlayer?.playWhenReady = true

        } else {

            playerSdkCallBack?.onThrowCustomError(INVALID_CONTENT_ID)

        }
    }

    fun setReplayMode(isReplay:Boolean){
        isReplayEnabled = isReplay
    }

    private fun getMediaItem(
        drm: Boolean,
        videoUrl: String,
        drmLicenseUrl: String? = null,
        adsUrl: String? = null,
        subtitle: MutableList<MediaItem.SubtitleConfiguration>? = null,
        srtManual: String? = null
    ): MediaItem {

       /* val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        val mediaSourceFactory: MediaSource.Factory = DefaultMediaSourceFactory(dataSourceFactory)
            .setLocalAdInsertionComponents(
                AdsLoader.Provider { unusedAdTagUri: AdsConfiguration? -> adsLoader }, mediaPlayerView as AdViewProvider)
        mediaPlayer = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build()
        mediaPlayerView?.player = mediaPlayer
        adsLoader?.setPlayer(mediaPlayer)*/

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(videoUrl)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(contentTitle).build())

        if (!srtManual.isNullOrEmpty()){
            val subTitleToPass = MediaItem.SubtitleConfiguration.Builder(
                Uri.parse(srtManual)
            ).setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(ImmutableList.of(subTitleToPass))

            mediaPlayer?.trackSelectionParameters = mediaPlayer?.trackSelectionParameters?.buildUpon()
                ?.setTrackTypeDisabled(C.TRACK_TYPE_TEXT,false)
                ?.build()!!
        }

        if (drm && !drmLicenseUrl.isNullOrEmpty()) {
            val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(drmLicenseUrl)
                .build()
            mediaItemBuilder.setDrmConfiguration(drmConfig)
        }

        if (!adsUrl.isNullOrEmpty()) {
            val adTagUri = Uri.parse(adsUrl)
            mediaItemBuilder.setAdsConfiguration(
                AdsConfiguration.Builder(adTagUri).build()
            )
        }

        if (subtitle != null) {
            mediaItemBuilder.setSubtitleConfigurations(subtitle)
        }

        return mediaItemBuilder.build()
    }

    private fun getCustomLoadControl(): LoadControl {

        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER_DURATION, MAX_BUFFER_DURATION,
                BUFFER_FOR_PLAYBACK, BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
            )
            .setAllocator(DefaultAllocator(true, ALLOCATION_SIZE))
            .setBackBuffer(BACK_BUFFER_DURATION, false)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .build()

    }

    private fun getMediaPLayerInstance(
        customLoadControl: LoadControl,
        trackSelector: TrackSelector
    ): ExoPlayer {

        return ExoPlayer.Builder(context)
            .setLoadControl(customLoadControl)
            .setTrackSelector(trackSelector)
            .setSeekForwardIncrementMs(FORWARD_INCREMENT.toLong())
            .setSeekBackIncrementMs(BACKWARD_INCREMENT.toLong())
            .build()
    }

    private var playerStateListener: Player.Listener = object : Player.Listener {

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {

            when (playbackState) {

                Player.STATE_READY -> {
                    startUpdates()
                    playerSdkCallBack?.onBufferingEnded()
                    playerSdkCallBack?.onVideoStart()
                    mediaPlayerView?.isVisible = true
                    if (mediaPlayer?.duration != null) {
                        playerScrub?.setDuration(mediaPlayer?.duration!!)
                    }
                    playerScrub?.setPosition(mediaPlayer?.currentPosition as Long)
                    mediaPlayerView?.bringToFront()

                }

                Player.STATE_IDLE -> {
                    stopUpdates()
                    mediaPlayerView?.bringToFront()
                }

                Player.STATE_BUFFERING -> {
                    stopUpdates()
                    playerSdkCallBack?.onBufferingStart()

                }

                Player.STATE_ENDED -> {
                    stopUpdates()
                    if (isReplayEnabled){
                        hideControlWithReplay(true)
                    }else{
                        hideControlWithReplay(false)
                        playerSdkCallBack?.onPlayNextContent()
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            playerSdkCallBack?.onPlayerError()
            super.onPlayerError(error)
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
        }
    }

    private fun isDrmContent(videoUrl: String): Boolean {
        return videoUrl.split("\\.".toRegex()).last() == MPD
    }

    fun hideControlWithReplay(hide:Boolean){
        if (hide){
            replay?.isVisible = true
            llCentre?.isVisible = false
            llBottom?.isVisible = false
            playerScrub?.isVisible = false
            clTime?.isVisible = false
        }else{
            replay?.isVisible = false
            llCentre?.isVisible = true
            llBottom?.isVisible = true
            playerScrub?.isVisible = true
            clTime?.isVisible = true
        }
    }

    fun setContentFilePath(url: String) {
        mediaPlayer = null
        contentUrl = url
    }

    fun setAdUrl(adurl: String) {
        adUrl = adurl
    }

    fun startPlayer() {
        initializePlayer()
    }

    fun parseSubtitle(url:String){
        val parsedUri = Uri.parse(url)
        parsedUri.queryParameterNames.size
    }

    fun pausePlayer() {
        if (mediaPlayer != null && mediaPlayerView != null) {
            mediaPlayerView?.onPause()
            mediaPlayer?.playWhenReady = false
            playButton?.isVisible = true
            playButton?.requestFocus()
            pauseButton?.isVisible = false
            playerSdkCallBack?.onVideoStop()
        }
    }

    fun resumePlayer() {
        if (mediaPlayer != null && mediaPlayerView != null) {
            mediaPlayerView?.onResume()
            mediaPlayer?.playWhenReady = true
            playButton?.isVisible = false
            pauseButton?.isVisible = true
            pauseButton?.requestFocus()
            playerSdkCallBack?.onVideoStart()
        }
    }

    fun releasePlayer() {
        if (mediaPlayer != null && mediaPlayerView != null) {
            mediaPlayer?.release()
            mediaPlayerView?.player?.release()
            playerSdkCallBack?.onPlayerRelease()
            playerSdkCallBack = null
        }
    }

    private fun getMediaItemWithAd(
        drm: Boolean, videoUrl: String,
        drmLicenseUrl: String? = null,
        adsUrl: String? = null,
        subtitle: MediaItem.SubtitleConfiguration? = null
    ): MediaItem {

        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        val mediaSourceFactory: MediaSource.Factory = DefaultMediaSourceFactory(dataSourceFactory)
            .setLocalAdInsertionComponents(
                {
                    adsLoader
                },
                mediaPlayerView as AdViewProvider
            )

        // Create an ExoPlayer and set it as the player for content and ads.
        mediaPlayer = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build()
        mediaPlayerView?.player = mediaPlayer
        adsLoader!!.setPlayer(mediaPlayer)

        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        val contentUri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4")
        val adTagUri =
            Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/23078103558/home_video_ad_unit&description_url=http%3A%2F%2Fwww.multitvsolution.com&tfcd=0&npa=0&sz=400x300%7C640x480&ciu_szs=480x320%2C300x250&gdfp_req=1&output=vast&env=vp&unviewed_position_start=1&impl=s&correlator=")

        return MediaItem.Builder()
            .setUri(contentUri)
            .setAdsConfiguration(AdsConfiguration.Builder(adTagUri).build())
            .build()

    }

    fun setKeyToken(key: String) {
        token = key
    }

    fun setContentMetaData(
        contentType: ContentType,
        contentTitle: String? = "",
        contentId: String
    ) {
        this.contentType = contentType
        if (contentTitle?.isNotEmpty() == true) {
            this.contentTitle = contentTitle
            tvContentTitle?.text = contentTitle
        }
        this.contentId = contentId
    }

    fun setVideoPlayerSdkListener(playerSdkCallBack: PlayerSdkCallBack) {
        this.playerSdkCallBack = playerSdkCallBack
    }

    private fun convertMsToMinSec(ms: Long): String {

        val minutes = (ms / (1000 * 60)) % 60
        val seconds = (ms / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        mediaPlayer?.pause()
        loadSprite(position, mediaPlayer?.duration?.toInt()?.div(1000) as Int)
        updatePreviewPosition(
            position.div(1000).toInt(),
            mediaPlayer?.duration?.toInt()?.div(1000) as Int,
            flPreview as FrameLayout
        )
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        loadSprite(position, mediaPlayer?.duration?.toInt()?.div(1000) as Int)
        updatePreviewPosition(
            position.div(1000).toInt(),
            mediaPlayer?.duration?.toInt()?.div(1000) as Int,
            flPreview as FrameLayout
        )
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {

        loadSprite(position, mediaPlayer?.duration?.toInt()?.div(1000) as Int)
        updatePreviewPosition(
            position.div(1000).toInt(),
            mediaPlayer?.duration?.toInt()?.div(1000) as Int,
            flPreview as FrameLayout
        )

        if (!canceled) {
            flPreview?.isVisible = false
            mediaPlayer?.seekTo(position)
            if (pauseButton?.isVisible == true){
                mediaPlayer?.play()
            }
        }
    }

    private fun stopUpdates() {
        job?.cancel()
        job = null
    }

    private fun startUpdates() {
        stopUpdates()
        job = scope.launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    playerScrub?.setPosition(mediaPlayer?.currentPosition as Long)
                }
                delay(1000)
            }
        }
    }

    fun setMiniPlayerLayout(flag:Boolean){
        if (flag){
            mediaPlayerView?.hideController()
            mediaPlayerView?.useController = false
            mediaPlayerView?.isFocusable = false
            mediaPlayerView?.isClickable = false
            playerSdkCallBack?.onFullScreenExit()
        }else{
            mediaPlayerView?.useController = true
            mediaPlayerView?.showController()
            mediaPlayerView?.isFocusable = true
            mediaPlayerView?.isClickable = true
            playerSdkCallBack?.onFullScreenEnter()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setSpriteData(spriteUrl: String, toLoadFromBitmap: Boolean) {

        if (toLoadFromBitmap) {
            GlobalScope.launch {
                try {
                    spriteData = convertSpriteData(spriteUrl)
                } catch (ex: Exception) {
                    Log.e("ExampleUsage", "Exception: $ex")
                }
            }

        } else {
            this.spriteUrl = spriteUrl
        }
    }

    private fun loadSprite(position: Long, maxLine: Int) {

        flPreview?.isVisible = true
        if (spriteData != null) {
            Glide.with(scrubImage as ImageView).load(spriteData)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).fitCenter()
                .transform(GlideThumbnailTransformation(position, maxLine))
                .into(scrubImage as ImageView)
        } else {
            if (!TextUtils.isEmpty(spriteUrl)) {
                Glide.with(scrubImage as ImageView).load(spriteUrl)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).fitCenter()
                    .transform(GlideThumbnailTransformation(position, maxLine))
                    .into(scrubImage as ImageView)
            } else {

                flPreview?.isVisible = false
            }
        }
    }

    private fun updatePreviewPosition(
        scrubPosition: Int,
        maxPosition: Int,
        previewLayout: FrameLayout
    ) {

        val positionPercent = scrubPosition.toFloat() / maxPosition.toFloat()
        val newHorizontalBias = positionPercent * (1 - 0.35)

        val layoutParams = previewLayout.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.horizontalBias = newHorizontalBias.toFloat()
        previewLayout.layoutParams = layoutParams
        previewLayout.requestLayout()
    }

     fun getVideoFormats():ArrayList<Format> {
        val trackSelector = mediaPlayer?.trackSelector as? DefaultTrackSelector
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
        videoFormatList.clear()
        mappedTrackInfo?.let {
            val videoRendererIndices = (0 until it.rendererCount).filter { index -> isVideoRenderer(it, index) }

            videoRendererIndices.forEach { videoRendererIndex ->
                val override = it.getTrackGroups(videoRendererIndex)
                videoFormatList.addAll(getVideoQualityList(override))
            }
        }
         return videoFormatList
    }
    private fun getVideoQualityList(trackGroups: TrackGroupArray): List<Format> {
        val videoQuality = mutableListOf<Format>()
        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups[groupIndex]
            videoQuality.addAll((0 until group.length).map { trackIndex -> group.getFormat(trackIndex) })
        }
        return videoQuality
    }

    private fun isVideoRenderer(
        mappedTrackInfo: MappedTrackInfo,
        rendererIndex: Int
    ): Boolean {
        return mappedTrackInfo.getTrackGroups(rendererIndex).length > 0 &&
                C.TRACK_TYPE_VIDEO == mappedTrackInfo.getRendererType(rendererIndex)
    }

     fun changeVideoResolution(width:Int,height:Int){
        val trackSelector = mediaPlayer?.trackSelector as? DefaultTrackSelector
        trackSelector?.buildUponParameters()?.setMaxVideoSize(height, width)?.let { newParameters ->
            trackSelector.setParameters(newParameters)
        }
    }

    fun seekTo(time:Long){
        mediaPlayer?.seekTo(time)
    }

    fun getCurrentDuration():Long{
        return mediaPlayer?.currentPosition as Long
    }

    fun setPlayBackSpeed(speed:Float){
        mediaPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun hideControls(){
        mediaPlayerView?.hideController()
    }

    fun showControls(){
        mediaPlayerView?.showController()
    }

    fun focusNextPrevButton(
        nextButtonVisibility: Boolean,
        previousButtonVisibility: Boolean
    ) {
        nextTrack?.isFocusable = nextButtonVisibility
        preTrack?.isFocusable = previousButtonVisibility
        if (nextButtonVisibility){
            nextTrack?.setColorFilter(getContext().resources.getColor(R.color.white))
        }else{
            nextTrack?.setColorFilter(getContext().resources.getColor(R.color.grey))
        }
        if (previousButtonVisibility){
            preTrack?.setColorFilter(getContext().resources.getColor(R.color.white))
        }else{
            preTrack?.setColorFilter(getContext().resources.getColor(R.color.grey))
        }
    }

    fun getSubTitleFormats():ArrayList<Format> {
        val trackSelector = mediaPlayer?.trackSelector as? DefaultTrackSelector
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
        videoSubTitleList.clear()
        mappedTrackInfo?.let {
            val videoRendererIndices = (0 until it.rendererCount).filter { index -> isVideoRendererSubTitle(it, index) }

            videoRendererIndices.forEach { videoRendererIndex ->
                val override = it.getTrackGroups(videoRendererIndex)
                videoSubTitleList.addAll(getVideoQualityList(override))
            }
        }
        return videoSubTitleList
    }
    private fun getSubTitleList(trackGroups: TrackGroupArray): List<Format> {
        val videoQuality = mutableListOf<Format>()
        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups[groupIndex]
            videoQuality.addAll((0 until group.length).map { trackIndex -> group.getFormat(trackIndex) })
        }
        return videoQuality
    }

    private fun isVideoRendererSubTitle(
        mappedTrackInfo: MappedTrackInfo,
        rendererIndex: Int
    ): Boolean {
        return mappedTrackInfo.getTrackGroups(rendererIndex).length > 0 &&
                C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(rendererIndex)
    }

    fun changeSubTitle(id:String){
        val trackSelector = mediaPlayer?.trackSelector as? DefaultTrackSelector
        trackSelector?.buildUponParameters()?.setPreferredTextLanguage(id)?.let { newParameters ->
            trackSelector.setParameters(newParameters)
        }

        mediaPlayerView?.subtitleView?.isVisible = true
    }

    fun hideSubTitle(){
        mediaPlayerView?.subtitleView?.isVisible = false
    }
}