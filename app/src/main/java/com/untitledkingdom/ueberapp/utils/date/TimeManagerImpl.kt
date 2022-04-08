package com.untitledkingdom.ueberapp.utils.date

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class TimeManagerImpl @Inject constructor() : TimeManager {
    private fun provideCurrentInstant(): Instant = Instant.now()
    private fun provideCurrentZone(): ZoneId = ZoneId.systemDefault()

    override fun provideCurrentLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(provideCurrentInstant(), provideCurrentZone())
}
