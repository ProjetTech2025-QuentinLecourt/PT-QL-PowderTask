package com.quentinlecourt.podwertask_mobile.data.api

import com.quentinlecourt.podwertask_mobile.data.model.LoginRequest
import com.quentinlecourt.podwertask_mobile.data.model.LoginResponse
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response

/**
 * MyApi
 * Interface qui recuille toutes les routes possible vers l'API
 */
interface MyAPI {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}