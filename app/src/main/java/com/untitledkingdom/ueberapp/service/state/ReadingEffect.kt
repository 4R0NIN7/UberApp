package com.untitledkingdom.ueberapp.service.state

import com.untitledkingdom.ueberapp.devices.data.DeviceReading

sealed interface ReadingEffect {
    object SendBroadcastToActivity : ReadingEffect
    data class StartNotifying(val reading: DeviceReading) : ReadingEffect
    object Stop : ReadingEffect
}
