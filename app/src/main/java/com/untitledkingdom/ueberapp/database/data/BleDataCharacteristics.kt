package com.untitledkingdom.ueberapp.database.data

import java.time.LocalDateTime

data class BleDataCharacteristics(
    val averageTemperature: Double,
    val minimalTemperature: Double,
    val maximalTemperature: Double,
    val averageHumidity: Double,
    val minimalHumidity: Double,
    val maximalHumidity: Double,
    val day: LocalDateTime
)
