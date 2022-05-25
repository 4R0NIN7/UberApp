package com.untitledkingdom.ueberapp.background.state

import com.untitledkingdom.ueberapp.devices.data.Reading

sealed interface ReadingEffect {
    data class StartNotifying(val reading: Reading) : ReadingEffect
    data class NotifyBatterLow(val batteryLevel: Int) : ReadingEffect
    object Stop : ReadingEffect
}
