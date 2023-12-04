package com.example.ptplayer.player.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(DelicateCoroutinesApi::class)
suspend fun convertSpriteData(path: String): Bitmap? = suspendCoroutine { continuation ->
    GlobalScope.launch(Dispatchers.IO) {
        var inStream: InputStream?
        var bmp: Bitmap? = null
        val responseCode: Int

        try {
            val url = URL(path)
            val con = url.openConnection() as HttpURLConnection
            con.doInput = true
            con.connect()
            responseCode = con.responseCode

            withContext(Dispatchers.IO) {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inStream = con.inputStream
                    bmp = BitmapFactory.decodeStream(inStream)
                    inStream?.close()
                }
            }

            // Complete the coroutine with the result
            continuation.resume(bmp)
        } catch (ex: Exception) {
            Log.e("Exception:::", ex.toString())
            // Complete the coroutine with an exception if an error occurs
            continuation.resumeWithException(ex)
        }
    }
}
