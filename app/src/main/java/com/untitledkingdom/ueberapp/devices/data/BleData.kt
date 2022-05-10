package com.untitledkingdom.ueberapp.devices.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.untitledkingdom.ueberapp.database.DatabaseConst
import java.time.LocalDateTime

@Entity(tableName = DatabaseConst.TABLE)
data class BleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deviceReading: DeviceReading,
    val localDateTime: LocalDateTime,
    val serviceUUID: String,
    val isSynchronized: Boolean = false
)

data class DeviceReading(
    val temperature: Float,
    val humidity: Int
)
