package com.quentinlecourt.podwertask_mobile.data.model

sealed class CalibrationReturn {
    data class AutoSuccess(val data: MachineAutoCalibration) : CalibrationReturn()
    data class ManualSuccess(val data: MachineManualCalibration) : CalibrationReturn()
    data class Error(val error: MqttError) : CalibrationReturn()
}

enum class CalibrationType {
    AUTO,
    MANUAL
}