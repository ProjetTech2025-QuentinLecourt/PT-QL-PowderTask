package com.quentinlecourt.podwertask_mobile.data.model

// Classe modèle pour une machine/balance connectée
data class Machine(
    val id: Int,
    val name: String,
    var isOnline: Boolean = false,
    var accelerometerSensorStatus: Int? = 0,
    var weightSensorStatus: Int? = 0,
    var lastConnectionTime: Long?,
    var lastDetails: MachineDetails? = null
)