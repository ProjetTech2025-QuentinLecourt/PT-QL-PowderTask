package com.quentinlecourt.podwertask_mobile.data.model

/**
 * LoginRequest
 * Modèle de la requête pour éffectuer un login
 */
data class LoginRequest(
    val email: String,
    val password: String
)