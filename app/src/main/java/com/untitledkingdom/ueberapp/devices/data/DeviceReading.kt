package com.untitledkingdom.ueberapp.devices.data

import java.time.LocalDateTime

data class DeviceReading(
    val id: Int,
    val reading: Reading,
    val localDateTime: LocalDateTime,
    val isSynchronized: Boolean
)

data class Reading(
    val temperature: Float,
    val humidity: Int
)
