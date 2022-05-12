package com.untitledkingdom.ueberapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.untitledkingdom.ueberapp.database.data.BleDataEntity

@Database(
    entities = [BleDataEntity::class],
    version = DatabaseConst.VERSION,
    exportSchema = false
)
@TypeConverters(TimeConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun getDao(): Dao
}
