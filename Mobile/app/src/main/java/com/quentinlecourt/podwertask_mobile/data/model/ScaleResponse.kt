package com.quentinlecourt.podwertask_mobile.data.model

data class ScaleDto(
    val id: Int,
    val scale_name: String
)

data class ScaleResponse(
    val success: Boolean,
    val message: String,
    val data: ScaleData
)

data class ScaleData(
    val scales: List<ScaleDto>
)
