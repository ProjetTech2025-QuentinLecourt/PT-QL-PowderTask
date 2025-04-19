package com.quentinlecourt.podwertask_mobile.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // J'utilise pour faire passer cet object entre 2 activités par putExtra()
data class Machine(
    val id: Int,
    val name: String,
    var isOnline: Boolean = false,
    var accelerometerStatus: Int?, // 0-2
    var accX: Float?,
    var accY: Float?,
    var accZ: Float?,
    var accNorm: Float?,
    var weightSensorStatus: Int?, // 0-2
    var weightDetected: Float?,
    var calibrationIndex: Float?,
    var display: Int?, // 0-2
    var datetimeDelivery: Long?
) : Parcelable