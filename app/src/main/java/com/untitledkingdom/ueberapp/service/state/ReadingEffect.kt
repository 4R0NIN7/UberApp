package com.untitledkingdom.ueberapp.service.state

import com.untitledkingdom.ueberapp.devices.data.DeviceReading

sealed interface ReadingEffect {
    object StartForegroundService : ReadingEffect
    object SendBroadcastToActivity : ReadingEffect
    data class UpdateNotification(val reading: DeviceReading) : ReadingEffect
    object Stop : ReadingEffect
}
