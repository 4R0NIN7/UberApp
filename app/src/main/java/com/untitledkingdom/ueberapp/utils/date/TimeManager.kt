package com.untitledkingdom.ueberapp.utils.date

import java.time.LocalDateTime

interface TimeManager {
    fun provideCurrentLocalDateTime(): LocalDateTime
}
