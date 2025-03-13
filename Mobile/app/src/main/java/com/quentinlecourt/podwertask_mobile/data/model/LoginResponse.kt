package com.quentinlecourt.podwertask_mobile.data.model

/**
 * LoginResponse
 * Modèle de la réponse lors d'un login
 */
data class LoginResponse(
    val bearerToken: String,
    val message: String
)