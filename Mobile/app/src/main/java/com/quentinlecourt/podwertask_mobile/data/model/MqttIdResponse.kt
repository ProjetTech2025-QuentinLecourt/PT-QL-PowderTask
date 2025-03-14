package com.quentinlecourt.podwertask_mobile.data.model

/**
 * MqttIdResponse
 * Identifiant de connexion pour contacter le broker MQTT
 */
data class MqttIdResponse(
    val id: String,
    val password: String
)