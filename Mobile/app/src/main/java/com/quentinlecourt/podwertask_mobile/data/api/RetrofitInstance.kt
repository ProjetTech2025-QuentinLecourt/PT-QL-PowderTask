package com.quentinlecourt.podwertask_mobile.data.api

import AuthInterceptor
import SessionManager
import android.content.Context
import com.quentinlecourt.podwertask_mobile.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private var retrofit: Retrofit? = null
    private lateinit var sessionManager: SessionManager

    fun initialize(context: Context) {
        sessionManager = SessionManager(context)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: MyAPI
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitInstance must be initialized first")
            }
            return retrofit!!.create(MyAPI::class.java)
        }
}