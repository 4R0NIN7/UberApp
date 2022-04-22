package com.untitledkingdom.ueberapp.service.state

sealed interface ReadingEvent {
    object StartReading : ReadingEvent
    object StopReading : ReadingEvent
}
