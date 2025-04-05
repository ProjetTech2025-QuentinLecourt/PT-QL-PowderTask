package com.quentinlecourt.podwertask_mobile.data.model

// Classe modèle pour une machine/balance connectée
data class Machine(
    val id: Int,
    val name: String,
    val isOnline: Boolean = false,
    val isAccelerometerWorking: Boolean,
    val isWeightSensorWorking: Boolean,
    val lastConnectionTime: Long
)