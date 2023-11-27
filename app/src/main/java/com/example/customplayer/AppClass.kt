package com.example.customplayer

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class AppClass:MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        NukeSSLCerts().nuke()
    }
}