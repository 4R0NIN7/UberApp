package com.untitledkingdom.ueberapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.untitledkingdom.ueberapp.devices.data.BleData

@Database(
    entities = [BleData::class],
    version = DatabaseConstants.VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun getDao(): Dao
}
