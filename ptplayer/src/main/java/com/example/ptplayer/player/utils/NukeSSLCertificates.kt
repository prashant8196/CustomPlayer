package com.example.ptplayer.player.utils

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class NukeSSLCertificates {

    companion object {
        private const val TAG = "NukeSSLCerts"
    }

    fun nuke() {
        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return emptyArray()
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
                    }
                }
            )

            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, session -> session.isValid }
        } catch (ignored: Exception) {
        }
    }
}
