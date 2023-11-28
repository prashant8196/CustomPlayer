package com.example.customplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import com.example.customplayer.databinding.ActivityMainBinding
import com.example.ptplayer.player.ContentType
import com.example.ptplayer.player.PlayerSdkCallBack

class MainActivity : AppCompatActivity(),PlayerSdkCallBack {

    @OptIn(UnstableApi::class) override fun onDestroy() {
        binding.ptPlayer.releasePlayer()
        super.onDestroy()
    }

    private lateinit var binding:ActivityMainBinding
    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.m3u8"
        binding.ptPlayer.setContentFilePath(url)
        binding.ptPlayer.setContentMetaData(ContentType.VOD,"Sample","11634")
        binding.ptPlayer.setVideoPlayerSdkListener(this)
        binding.ptPlayer.startPlayer()

    }

    override fun onPlayerReady(contentUrl: String) {

    }

    override fun onPlayNextContent() {

    }

    override fun onPlayPreviousContent() {

    }

    override fun onPlayerError() {

    }

    override fun onPlayClick() {

    }

    override fun onPauseClick() {

    }

    override fun onVideoStart() {

    }

    override fun onVideoStop() {

    }

    override fun onBufferingStart() {

        binding.buffer.isVisible = true
    }

    override fun onBufferingEnded() {

        binding.buffer.isVisible = false
    }

    override fun onPlayBackProgress(position: Long, duration: Long) {

    }

    override fun onFullScreenEnter() {

    }

    override fun onFullScreenExit() {

    }

    override fun isMute(value: Boolean) {

    }

    override fun onSeek(duration: Long) {

    }

    override fun onSubTitleChange() {

    }

    override fun onLanguageChange() {

    }

    override fun onVideoQualityChange() {

    }

    override fun onPlayerRelease() {

    }

    @OptIn(UnstableApi::class) override fun onPlayerBackPressed() {

        onBackPressed()
        binding.ptPlayer.releasePlayer()
    }
}