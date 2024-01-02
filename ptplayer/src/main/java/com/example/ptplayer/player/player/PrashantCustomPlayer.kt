package com.example.ptplayer.player.player

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.graphics.Bitmap
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
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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

    private var skipPre: ImageView? = null
    private var playButton: ImageView? = null
    private var pauseButton: ImageView? = null
    private var skipFwd: ImageView? = null
    private var preTrack: ImageView? = null
    private var nextTrack: ImageView? = null
    private var volumeIcon: ImageView? = null
    private var settings: ImageView? = null
    private var scrubImage: ImageView? = null
    private var playerScrub: DefaultTimeBar? = null
    private var flPreview: FrameLayout? = null
    private var tvContentTitle: TextView? = null
    private var volumeSeekBar: SeekBar? = null
    private var audioManager: AudioManager? = null
    private var screenMode: ImageView? = null
    private var isFullScreen: Int = 0
    private var customControl: ConstraintLayout? = null
    private var spriteData: Bitmap? = null
    private var spriteUrl: String? = null

    private var job: Job? = null
    private val scope = MainScope()

    constructor(context: Context, attrs: AttributeSet) : this(
        context as AppCompatActivity, attrs, 0
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view =
            LayoutInflater.from(getContext()).inflate(R.layout.prashant_custom_player, this)
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
        settings?.setOnKeyListener(enterKeyListener)
        scrubImage?.setOnKeyListener(enterKeyListener)
        screenMode?.setOnKeyListener(enterKeyListener)
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
        settings?.onFocusChangeListener = onFocusChangeListener
        scrubImage?.onFocusChangeListener = onFocusChangeListener
        screenMode?.onFocusChangeListener = onFocusChangeListener
        playerScrub?.onFocusChangeListener = onFocusChangeListener

    }

    private fun fetchAllId(view: View) {

        mediaPlayerView = view.findViewById(R.id.media_player_view)
        skipFwd = view.findViewById(R.id.skip_fwd_btn)
        skipPre = view.findViewById(R.id.skip_pre_btn)
        playButton = view.findViewById(R.id.play_btn)
        pauseButton = view.findViewById(R.id.pause_btn)
        volumeIcon = view.findViewById(R.id.volume_btn)
        scrubImage = view.findViewById(R.id.imageView)
        tvContentTitle = view.findViewById(R.id.content_title)
        playerScrub = view.findViewById(R.id.player_scrub)
        flPreview = view.findViewById(R.id.previewFrameLayout)
        volumeSeekBar = view.findViewById(R.id.volume_seekbar)
        customControl = view.findViewById(R.id.custom_control)
        preTrack = view.findViewById(R.id.exo_prev)
        nextTrack = view.findViewById(R.id.exo_next)
        settings = view.findViewById(R.id.exo_settings)
        screenMode = view.findViewById(R.id.iv_screen_mode)

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
            mediaPlayerView?.controllerHideOnTouch = true
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

        } else {

            playerSdkCallBack?.onThrowCustomError(INVALID_CONTENT_ID)

        }
    }

    private fun getMediaItem(
        drm: Boolean,
        videoUrl: String,
        drmLicenseUrl: String? = null,
        adsUrl: String? = null,
        subtitle: MutableList<MediaItem.SubtitleConfiguration>? = null
    ): MediaItem {

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
        contentType: ContentType,
        contentTitle: String? = "",
        contentId: String
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
            mediaPlayer?.play()
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
        mediaPlayerView?.hideController()
        mediaPlayerView?.isFocusable = false
        mediaPlayerView?.isClickable = false
        playerSdkCallBack?.onFullScreenExit()
    }

    private fun setFullScreenPlayerLayout() {
        mediaPlayerView?.isFocusable = true
        mediaPlayerView?.isClickable = true
        mediaPlayerView?.showController()
        playerSdkCallBack?.onFullScreenEnter()

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
}