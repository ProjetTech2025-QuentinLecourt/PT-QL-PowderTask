package com.quentinlecourt.podwertask_mobile.data.model

// Classe modèle pour une machine/balance connectée
data class Machine(
    val id: Int,
    val name: String,
    var isOnline: Boolean = false,
    var isSensorAccelerometerCorrect: Boolean? = null,
    var isSensorWeightCorrect: Boolean? = null,
    var lastConnectionTime: Long = 0
) {
    val hasAnyProblem: Boolean
        get() = isSensorAccelerometerCorrect == false || isSensorWeightCorrect == false
}