package com.untitledkingdom.ueberapp.devices.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.untitledkingdom.ueberapp.database.DatabaseConstants
import java.time.LocalDateTime

@Entity(tableName = DatabaseConstants.TABLE)
data class BleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val data: Readings,
    val localDateTime: LocalDateTime,
    val serviceUUID: String,
)

data class Readings(
    val temperature: Double,
    val humidity: Double
)
