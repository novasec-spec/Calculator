package com.novasec.secureauth.network

import android.content.Context
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val BASE_URL = "https://amendable-applaud-stank.ngrok-free.dev/" // Replace with your API URL

    private val certificatePinner = CertificatePinner.Builder()
        // Add your certificate pins here
         .add("amendable-applaud-stank.ngrok-free.dev", "QnNJUL6lT4rsuHNAApTXhNSNVySZqna5JBNaHdot+7U=")
        // .add("api.yourdomain.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
        .build()

    private val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustManager), SecureRandom())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { hostname, session -> true }
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Remove this in production
        })
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
