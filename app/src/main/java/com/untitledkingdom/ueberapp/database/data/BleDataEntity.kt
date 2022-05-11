package com.untitledkingdom.ueberapp.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.untitledkingdom.ueberapp.database.DatabaseConst
import java.time.LocalDateTime

@Entity(tableName = DatabaseConst.TABLE)
data class BleDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val temperature: Float,
    val humidity: Int,
    val dateTime: LocalDateTime,
    val serviceUUID: String,
    val isSynchronized: Boolean = false
)
