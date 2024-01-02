package com.example.customplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import com.example.customplayer.databinding.ActivityMainBinding
import com.example.ptplayer.player.constants.ContentType
import com.example.ptplayer.player.interfaces.PlayerSdkCallBack

class MainActivity : AppCompatActivity(), PlayerSdkCallBack {

    @OptIn(UnstableApi::class) override fun onDestroy() {
        binding.ptPlayer.releasePlayer()
        super.onDestroy()
    }

    @OptIn(UnstableApi::class) override fun onStop() {
        binding.ptPlayer.releasePlayer()
        super.onStop()
    }

    private lateinit var binding:ActivityMainBinding
    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        val sprite = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg"
        binding.ptPlayer.setContentFilePath(url)

       // binding.ptPlayer.setBgImage(url = "https://altb-img.multitvsolution.com/multitv/content/1061_651bac882efbe_854x480.jpg",null)
        binding.ptPlayer.setSpriteData(sprite,false)
        binding.ptPlayer.setContentMetaData(ContentType.AUD,"Sample","11634")
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

        binding.playerBuffer.isVisible = true
    }

    override fun onBufferingEnded() {
        binding.playerBuffer.isVisible = false
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
        binding.ptPlayer.releasePlayer()
    }

    override fun onSettingClicked() {
        Toast.makeText(this,"Setting Clicked",Toast.LENGTH_SHORT).show()
    }

    override fun onThrowCustomError(error: String) {

    }

    override fun onMoreOptionClicked() {

    }

    override fun onShuffleClicked(state: Boolean) {

    }

    override fun onRepeatClicked() {

    }
}