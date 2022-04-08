package com.untitledkingdom.ueberapp.feature.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.untitledkingdom.ueberapp.database.DatabaseConstants
import java.time.LocalDateTime

@Entity(tableName = DatabaseConstants.TABLE)
data class BleData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val data: String,
    val localDateTime: LocalDateTime,
    val serviceUUID: String,
    val characteristicUUID: String
)
