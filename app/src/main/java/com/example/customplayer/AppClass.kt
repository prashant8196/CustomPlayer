package com.example.customplayer

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class AppClass:MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        NukeSSLCerts().nuke()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}