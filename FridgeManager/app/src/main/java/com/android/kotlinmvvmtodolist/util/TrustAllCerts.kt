package com.android.kotlinmvvmtodolist.util

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object TrustAllCerts : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
//        TODO("Not yet implemented")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
//        TODO("Not yet implemented")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

    fun createSSLSocketFactory(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(this), SecureRandom())
        return sslContext.socketFactory
    }
}
