package com.quentinlecourt.podwertask_mobile.data.model

data class MachineDetails(
    val accelerometer: Int, // 0-2
    val acc_x: Float,
    val acc_y: Float,
    val acc_z: Float,
    val acc_norm: Float,
    val weight_sensor: Int, // 0-2
    val weight_detected: Float,
    val calibration_index: Float,
    val display: Int, // 0-2
    val timestamp: Long
)