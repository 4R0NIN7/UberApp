package com.untitledkingdom.ueberapp.service.state

sealed interface BackgroundEvent {
    object StartReading : BackgroundEvent
    object StopReading : BackgroundEvent
}
