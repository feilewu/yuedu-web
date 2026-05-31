package io.legado.server.http

import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

object SSLHelper {

    val unsafeTrustManager: X509TrustManager = object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    val unsafeSSLSocketFactory: SSLSocketFactory by lazy {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(unsafeTrustManager), SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val unsafeHostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }

    class SSLParams {
        lateinit var sSLSocketFactory: SSLSocketFactory
        lateinit var trustManager: X509TrustManager
    }

    fun getSocketFactory(vararg certificates: InputStream): SSLParams {
        return getSocketFactory(KeyStore.getDefaultType(), null, *certificates)
    }

    fun getSocketFactory(
        keyStoreType: String,
        keyStorePwd: String? = null,
        vararg certificates: InputStream
    ): SSLParams {
        val sslParams = SSLParams()
        return try {
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, keyStorePwd?.toCharArray())
            for (i in certificates.indices) {
                val certificate = certificates[i]
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val cert = certificateFactory.generateCertificate(certificate) as X509Certificate
                keyStore.setCertificateEntry("certificate$i", cert)
            }
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            sslParams.sSLSocketFactory = unsafeSSLSocketFactory
            sslParams.trustManager = unsafeTrustManager
            sslParams
        } catch (e: Exception) {
            sslParams.sSLSocketFactory = unsafeSSLSocketFactory
            sslParams.trustManager = unsafeTrustManager
            sslParams
        }
    }
}
