package com.example.ptplayer.player.interfaces

interface PlayerSdkCallBack {

    fun onPlayerReady(contentUrl:String)

    fun onPlayNextContent()

    fun onPlayPreviousContent()

    fun onPlayerError()

    fun onPlayClick()

    fun onPauseClick()

    fun onVideoStart()

    fun onVideoStop()

    fun onBufferingStart()

    fun onBufferingEnded()

    fun onPlayBackProgress(position:Long , duration:Long)

    fun onFullScreenEnter()

    fun onFullScreenExit()

    fun isMute(value:Boolean)

    fun onSeek(duration:Long)

    fun onSubTitleChange()

    fun onLanguageChange()

    fun onVideoQualityChange()

    fun onPlayerRelease()

    fun onPlayerBackPressed()

    fun onSettingClicked()

    fun onThrowCustomError(error:String)

    fun onMoreOptionClicked()

    fun onShuffleClicked(state:Boolean)

    fun onRepeatClicked()

}