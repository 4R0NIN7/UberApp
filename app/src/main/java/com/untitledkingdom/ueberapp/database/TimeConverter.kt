package com.untitledkingdom.ueberapp.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import java.time.LocalDateTime
import javax.inject.Inject

@ProvidedTypeConverter
class TimeConverter @Inject constructor(private val timeManager: TimeManager) {

    @TypeConverter
    fun toLocalDateTime(epochSecond: Long): LocalDateTime {
        return timeManager.epochSecondToLocalDateTime(epochSecond)
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long {
        return timeManager.localDateTimeToEpochSecond(
            dateTime = dateTime
        )
    }
}
