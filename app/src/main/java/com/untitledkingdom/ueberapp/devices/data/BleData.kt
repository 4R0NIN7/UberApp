package com.untitledkingdom.ueberapp.devices.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.untitledkingdom.ueberapp.database.DatabaseConstants
import java.time.LocalDateTime

@Entity(tableName = DatabaseConstants.TABLE)
data class BleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deviceReading: DeviceReading,
    val localDateTime: LocalDateTime,
    val serviceUUID: String,
)

data class DeviceReading(
    val temperature: Float,
    val humidity: Int
)
