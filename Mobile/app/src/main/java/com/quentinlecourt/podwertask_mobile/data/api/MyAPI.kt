package com.quentinlecourt.podwertask_mobile.data.api

import com.quentinlecourt.podwertask_mobile.data.model.LoginRequest
import com.quentinlecourt.podwertask_mobile.data.model.LoginResponse
import com.quentinlecourt.podwertask_mobile.data.model.MqttIdResponse
import com.quentinlecourt.podwertask_mobile.data.model.ScaleResponse
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET

/**
 * MyApi
 * Interface qui recuille toutes les routes possible vers l'API
 */
interface MyAPI {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("mqtt")
    suspend fun getMqttId(): Response<MqttIdResponse>

    @GET("machines")
    suspend fun getMachines(): Response<ScaleResponse>

}