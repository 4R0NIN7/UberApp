package com.untitledkingdom.ueberapp.utils.date

import java.time.LocalDateTime

interface TimeManager {
    fun provideCurrentLocalDateTime(): LocalDateTime
    fun localDateTimeToEpochSecond(dateTime: LocalDateTime): Long
    fun epochSecondToLocalDateTime(epochSecond: Long): LocalDateTime
}
