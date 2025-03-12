package com.quentinlecourt.podwertask_mobile

import retrofit2.http.POST
import retrofit2.http.Body

class MyAPI {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}