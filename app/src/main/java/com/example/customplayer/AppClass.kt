package com.example.customplayer

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.ptplayer.player.utils.NukeSSLCertificates

class AppClass:MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        NukeSSLCertificates().nuke()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}