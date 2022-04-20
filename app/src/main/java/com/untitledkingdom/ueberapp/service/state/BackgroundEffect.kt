package com.untitledkingdom.ueberapp.service.state

sealed interface BackgroundEffect {
    object SendBroadcastToActivity : BackgroundEffect
    object StartForegroundService : BackgroundEffect
    object Stop : BackgroundEffect
}
