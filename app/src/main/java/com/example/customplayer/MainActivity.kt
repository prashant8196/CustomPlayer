package com.example.customplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.multidex.MultiDex
import com.example.customplayer.databinding.ActivityMainBinding
import com.example.ptplayer.player.constants.ContentType
import com.example.ptplayer.player.interfaces.PlayerSdkCallBack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        MultiDex.install(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val url = "https://aolvideos.multitvsolution.com/obs/output/717_5fd70e9debb7d/dash/master.mpd"
       // binding.ptPlayer.setKeyToken("https://widevine-dash.ezdrm.com/widevine-php/widevine-foreignkey.php?pX=63CF74&user_id=ODkwMjEx&type=widevine&authorization=ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhkWFJvYjNKcGVtVmtJanAwY25WbExDSjFjMlZ5Ym1GdFpTSTZJbVJsWm1GMWJIUmZZV1J0YVc0aUxDSjBiMnRsYmlJNklqVm1aREZqWmpKbVl6bG1ORFVpTENKaGNIQmZhV1FpT2pjeE55d2liM2R1WlhKZmFXUWlPalUzTUN3aVlYQndYMjVoYldVaU9pSmtaV1poZFd4MFgyRmtiV2x1SWl3aVpYaHdJam94TnpBM09UYzVNREl3ZlEuM1dIaG9SUHZFcTNTYWwyZ1ZhWk1HWFFKdWZKeEhKcEV2ejlORXprMXh0dw==&payload=eyJjb250ZW50X2lkIjoiMTAxNTEwIiwia19pZCI6IjNiZmZjYWZhZTYzYjQ5NTk4YjRiMDdkMDEzNTk1MDY1IiwidXNlcl9pZCI6Ijg5MDIxMSIsInBhY2thZ2VfaWQiOiIxIiwibGljZW5jZV9kdXJhdGlvbiI6IjUwMDAiLCJzZWN1cml0eV9sZXZlbCI6IjAiLCJyZW50YWxfZHVyYXRpb24iOiIwIiwiY29udGVudF90eXBlIjoiMSIsImRvd25sb2FkIjoiMSJ9")
        val sprite = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg"
        binding.ptPlayer.setContentFilePath(url)
        binding.ptPlayer.setKeyToken("https://widevine-dash.ezdrm.com/widevine-php/widevine-foreignkey.php?pX=63CF74&user_id=ODQwMDIw&type=widevine&authorization=ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhkWFJvYjNKcGVtVmtJanAwY25WbExDSjFjMlZ5Ym1GdFpTSTZJbVJsWm1GMWJIUmZZV1J0YVc0aUxDSjBiMnRsYmlJNklqVm1aREZqWmpKbVl6bG1ORFVpTENKaGNIQmZhV1FpT2pjeE55d2liM2R1WlhKZmFXUWlPalUzTUN3aVlYQndYMjVoYldVaU9pSmtaV1poZFd4MFgyRmtiV2x1SWl3aVpYaHdJam94TnpFeU1UTTRNVFU0ZlEuTUZ4cG8xVXhmTGtRRGtzQ2lTc2RsMTZCMTk2eEtCUzlrTldWWDYzMFBXTQ==&payload=eyJjb250ZW50X2lkIjoiMTAwMzAyIiwia19pZCI6ImJjOTIxMTJjOGFjODQyZDE5ZWE4Zjc1MDc5NzczNmQ0IiwidXNlcl9pZCI6Ijg0MDAyMCIsInBhY2thZ2VfaWQiOiIyIiwibGljZW5jZV9kdXJhdGlvbiI6IjMwMDAiLCJzZWN1cml0eV9sZXZlbCI6IjAiLCJyZW50YWxfZHVyYXRpb24iOiIwIiwiY29udGVudF90eXBlIjoiMSIsImRvd25sb2FkIjoiMSJ9")
        //binding.ptPlayer.setReplayMode(true)
        /*binding.ptPlayer.setRadioView(false)
        binding.ptPlayer.setRepeatModelEnabled(isEnabled = true , focusFlag = true)*/

        //binding.ptPlayer.setBgImage(url = "https://altb-img.multitvsolution.com/multitv/content/1061_651bac882efbe_854x480.jpg",null)
        //binding.ptPlayer.setSpriteData(sprite,false)
        binding.ptPlayer.setContentMetaData(ContentType.VOD,"Sample","11634")
        binding.ptPlayer.setVideoPlayerSdkListener(this)
        binding.ptPlayer.startPlayer()
        binding.ptPlayer.focusNextPrevButton(false,true)

        //binding.ptPlayer.requestFocusOnPlayPause()

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

    }

    @OptIn(UnstableApi::class) override fun onSettingClicked() {
        Toast.makeText(this,"Setting Clicked",Toast.LENGTH_SHORT).show()
        var a =  binding.ptPlayer.getSubTitleFormats()
        binding.ptPlayer.changeSubTitle(a.first().language.toString())
       // binding.ptPlayer.changeSubTitle()
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