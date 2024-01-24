package com.example.ptplayer.player.player

import android.app.NotificationManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.MediaSession2Service
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PrashantAudioPlayer(
    private val context: AppCompatActivity, attrSet: AttributeSet, defStyleAttr: Int
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
    private var moreOptionButton: ImageView? = null
    private var skipPre: ImageView? = null
    private var playButton: ImageView? = null
    private var pauseButton: ImageView? = null
    private var skipFwd: ImageView? = null
    private var preTrack: ImageView? = null
    private var nextTrack: ImageView? = null
    private var volumeIcon: ImageView? = null
    private var muteIcon:ImageView? = null
    private var settings: ImageView? = null
    private var scrubImage: ImageView? = null
    private var playerScrub: DefaultTimeBar? = null
    private var tvContentTitle: TextView? = null
    private var volumeSeekBar: SeekBar? = null
    private var audioManager: AudioManager? = null
    private var screenMode: ImageView? = null
    private var isFullScreen: Int = 0
    private var customControl: ConstraintLayout? = null
    private var spriteData: Bitmap? = null
    private var spriteUrl: String? = null
    private var shuffleIcon: ImageView? = null
    private var repeatIcon: ImageView? = null
    private var bannerIcon: ImageView? = null
    private var shuffleState:Boolean? = false
    private var mediaSession: MediaSessionCompat? = null
    private var currentVolume:Float? = null

    private var job: Job? = null
    private val scope = MainScope()

    constructor(context: Context, attrs: AttributeSet) : this(
        context as AppCompatActivity, attrs, 0
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = LayoutInflater.from(getContext()).inflate(R.layout.prashant_audio_player, this)
        fetchAllId(view)
        setClickListenerOnViews()
        setUpControlClickListeners(view)
        audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        volumeSeekBar?.max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) as Int
        try {
            volumeSeekBar?.progress = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
            if (keyEvent.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
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
        playerScrub?.addListener(this)
        moreOptionButton?.setOnKeyListener(enterKeyListener)
        shuffleIcon?.setOnKeyListener(enterKeyListener)
        repeatIcon?.setOnKeyListener(enterKeyListener)
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
        playerScrub?.onFocusChangeListener = onFocusChangeListener
        shuffleIcon?.onFocusChangeListener = onFocusChangeListener
        repeatIcon?.onFocusChangeListener = onFocusChangeListener
        moreOptionButton?.onFocusChangeListener = onFocusChangeListener

    }

    private fun fetchAllId(view: View) {

        mediaPlayerView = view.findViewById(R.id.media_player_view)
        skipFwd = view.findViewById(R.id.skip_fwd_btn)
        skipPre = view.findViewById(R.id.skip_pre_btn)
        playButton = view.findViewById(R.id.play_btn)
        pauseButton = view.findViewById(R.id.pause_btn)
        volumeIcon = view.findViewById(R.id.volume_btn)
        muteIcon = view.findViewById(R.id.mute_btn)
        scrubImage = view.findViewById(R.id.imageView)
        tvContentTitle = view.findViewById(R.id.content_title)
        playerScrub = view.findViewById(R.id.player_scrub)
        volumeSeekBar = view.findViewById(R.id.volume_seekbar)
        customControl = view.findViewById(R.id.custom_control)
        preTrack = view.findViewById(R.id.exo_prev)
        nextTrack = view.findViewById(R.id.exo_next)
        settings = view.findViewById(R.id.exo_settings)
        screenMode = view.findViewById(R.id.iv_screen_mode)
        moreOptionButton = view.findViewById(R.id.exo_more)
        shuffleIcon = view.findViewById(R.id.exo_shuffle)
        repeatIcon = view.findViewById(R.id.exo_repeat)
        bannerIcon = view.findViewById(R.id.backgroundBanner)

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

                if (isFullScreen == 0) {
                    isFullScreen = 1
                    setFullScreenPlayerLayout()
                } else {
                    isFullScreen = 0
                    setMiniPlayerLayout()
                }
            }

            R.id.exo_settings -> {
                playerSdkCallBack?.onSettingClicked()
            }

            R.id.exo_more -> {
                playerSdkCallBack?.onMoreOptionClicked()
            }

            R.id.exo_shuffle -> {
                shuffleState = shuffleState != true
                playerSdkCallBack?.onShuffleClicked(shuffleState!!)
            }

            R.id.exo_repeat -> {
                playerSdkCallBack?.onRepeatClicked()
            }

            R.id.volume_btn ->{

                volumeIcon?.isVisible = false
                muteIcon?.isVisible = true
                currentVolume = mediaPlayer?.volume
                mediaPlayer?.volume = 0f
            }

            R.id.mute_btn ->{

                mediaPlayer?.volume = currentVolume as Float
                muteIcon?.isVisible = false
                volumeIcon?.isVisible = true
            }
        }
    }

    private fun initializePlayer() {

        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }
        if (contentUrl?.isNotEmpty() == true) {

            val customLoadControl = getCustomLoadControl()
            val trackSelector = DefaultTrackSelector(context)
            mediaPlayer = getMediaPLayerInstance(customLoadControl, trackSelector)
            mediaPlayer?.addListener(playerStateListener)
            mediaPlayerView?.player = mediaPlayer
            //mediaPlayerView?.controllerHideOnTouch = true
            mediaPlayerView?.keepScreenOn = true
            mediaPlayerView?.setControllerHideDuringAds(true)
            val isDrm = isDrmContent(contentUrl.toString())
            val mediaItem = getMediaItem(
                drm = isDrm,
                videoUrl = contentUrl.toString(),
                drmLicenseUrl = token,
                adsUrl = adUrl
            )
            mediaPlayer?.setMediaItem(mediaItem)
            mediaPlayer?.prepare()
            mediaPlayer?.playWhenReady = true
            tvContentTitle?.text = contentTitle
            createNotification()


        } else {

            playerSdkCallBack?.onThrowCustomError(INVALID_CONTENT_ID)

        }


        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, contentTitle)
                // Add other metadata as needed
                .build()
        )

        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (mediaPlayer?.playWhenReady == true) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer?.currentPosition ?: 0,
                    1.0f
                )
                .build()
        )

        initializeMediaSession()
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(context, "PrashantAudioPlayer")
        mediaSession?.setCallback(MediaSessionCallback())
        mediaSession?.isActive = true
    }


    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            resumePlayer()
        }

        override fun onPause() {
            pausePlayer()
        }

        override fun onStop() {
            releasePlayer()
        }

        override fun onSeekTo(pos: Long) {
            mediaPlayer?.seekTo(pos)
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }
    }


    fun playPause() {
        if (mediaPlayer?.playWhenReady == true) {
            mediaSession?.controller?.transportControls?.pause()
        } else {
            mediaSession?.controller?.transportControls?.play()
        }
    }

    fun stop() {
        mediaSession?.controller?.transportControls?.stop()
    }

    fun seekTo(time:Long){
        mediaPlayer?.seekTo(time)
    }

    fun getCurrentDuration():Long{
        return mediaPlayer?.currentPosition as Long
    }

    private fun getMediaItem(
        drm: Boolean,
        videoUrl: String,
        drmLicenseUrl: String? = null,
        adsUrl: String? = null,
        subtitle: MutableList<MediaItem.SubtitleConfiguration>? = null
    ): MediaItem {

        val mediaItemBuilder = MediaItem.Builder().setUri(videoUrl)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(contentTitle).build())

        if (drm && !drmLicenseUrl.isNullOrEmpty()) {
            val drmConfig =
                MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).setLicenseUri(drmLicenseUrl)
                    .build()
            mediaItemBuilder.setDrmConfiguration(drmConfig)
        }

        if (!adsUrl.isNullOrEmpty()) {
            val adTagUri = Uri.parse(adsUrl)
            mediaItemBuilder.setAdsConfiguration(
                MediaItem.AdsConfiguration.Builder(adTagUri).build()
            )
        }

        if (subtitle != null) {
            mediaItemBuilder.setSubtitleConfigurations(subtitle)
        }

        return mediaItemBuilder.build()
    }

    private fun getCustomLoadControl(): LoadControl {

        return DefaultLoadControl.Builder().setBufferDurationsMs(
            MIN_BUFFER_DURATION,
            MAX_BUFFER_DURATION,
            BUFFER_FOR_PLAYBACK,
            BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
        ).setAllocator(DefaultAllocator(true, ALLOCATION_SIZE))
            .setBackBuffer(BACK_BUFFER_DURATION, false)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET).build()

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
                    playerSdkCallBack?.onVideoStop()
                    playerSdkCallBack?.onPlayNextContent()
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
        return videoUrl.split("\\.".toRegex())[1] == MPD
    }

    fun setContentFilePath(url: String) {
        contentUrl = url
    }

    fun setAdUrl(adurl: String) {
        adUrl = adurl
    }

    fun startPlayer() {
        initializePlayer()
    }

     fun hideControllerAlways() {
        mediaPlayerView?.useController = true
        mediaPlayerView?.controllerShowTimeoutMs = 0
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
        }
    }
    fun setBgImage(url: String, placeholder: Int?) {
        loadImage(url, bannerIcon as ImageView, placeholder)
    }
    fun updateVolume() {
        try {
            volumeSeekBar?.progress = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setKeyToken(key: String) {
        token = key
    }

    fun setContentMetaData(
        contentType: ContentType, contentTitle: String? = "", contentId: String
    ) {
        this.contentType = contentType
        if (contentTitle?.isNotEmpty() == true) {
            this.contentTitle = contentTitle
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
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {

    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {

        if (!canceled) {
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

    private fun setMiniPlayerLayout() {
        playerSdkCallBack?.onFullScreenExit()
    }

    private fun setFullScreenPlayerLayout() {
        playerSdkCallBack?.onFullScreenEnter()

    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setSpriteData(spriteUrl: String, toLoadFromBitmap: Boolean) {

        if (toLoadFromBitmap) {
            GlobalScope.launch {
                try {
                    spriteData = convertSpriteData(spriteUrl)
                } catch (ex: Exception) {
                    Log.e("SpriteException", "Exception: $ex")
                }
            }

        } else {
            this.spriteUrl = spriteUrl
        }
    }

    private fun loadImage(
        url: String,
        imageview: ImageView,
        placeholderResId: Int?
    ) {
        var urlImage = url
        if (url.isBlank()) urlImage = "invalid"

        if (placeholderResId != null){
            Glide.with(this)
                .load(urlImage)
                .placeholder(placeholderResId)
                .into(imageview)
        }else{
            Glide.with(this)
                .load(urlImage)
                .into(imageview)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mediaSession?.isActive = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaSession?.isActive = false
    }


    private fun createNotification() {
        val mediaSessionToken = mediaSession?.sessionToken

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setSmallIcon(R.drawable.dot_player)
            .setContentTitle(contentTitle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "123"
    }
}