package com.untitledkingdom.ueberapp.utils.date

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class TimeManagerImpl @Inject constructor() : TimeManager {
    private fun provideCurrentInstant(): Instant = Instant.now()
    private fun provideCurrentZone(): ZoneId = ZoneId.systemDefault()
    override fun localDateTimeToEpochSecond(dateTime: LocalDateTime): Long =
        dateTime.atZone(provideCurrentZone()).toEpochSecond()

    override fun epochSecondToLocalDateTime(epochSecond: Long): LocalDateTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(epochSecond),
            provideCurrentZone()
        )

    override fun provideCurrentLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(provideCurrentInstant(), provideCurrentZone())
}
