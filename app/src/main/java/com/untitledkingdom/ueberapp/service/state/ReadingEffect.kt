package com.untitledkingdom.ueberapp.service.state

import com.untitledkingdom.ueberapp.devices.data.Reading

sealed interface ReadingEffect {
    object SendBroadcastToActivity : ReadingEffect
    data class StartNotifying(val reading: Reading) : ReadingEffect
    object Stop : ReadingEffect
}
