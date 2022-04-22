package com.untitledkingdom.ueberapp.service.state

sealed interface ReadingEffect {
    object SendBroadcastToActivity : ReadingEffect
    object StartForegroundService : ReadingEffect
    object Stop : ReadingEffect
}
