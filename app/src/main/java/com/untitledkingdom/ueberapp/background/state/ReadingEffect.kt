package com.untitledkingdom.ueberapp.background.state

import com.untitledkingdom.ueberapp.devices.data.Reading

sealed interface ReadingEffect {
    object SendBroadcastToActivity : ReadingEffect
    data class StartNotifying(val reading: Reading) : ReadingEffect
    object Stop : ReadingEffect
}
