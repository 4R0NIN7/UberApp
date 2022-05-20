package com.untitledkingdom.ueberapp.background.state

sealed interface ReadingEvent {
    object StartReading : ReadingEvent
    object StopReading : ReadingEvent
}
