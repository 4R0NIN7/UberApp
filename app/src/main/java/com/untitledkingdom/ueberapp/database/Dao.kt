package com.untitledkingdom.ueberapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query("SELECT * from ${DatabaseConst.TABLE}")
    suspend fun getAllData(): List<BleDataEntity>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID ORDER BY dateTime DESC")
    fun getAllDataFlow(serviceUUID: String): Flow<List<BleDataEntity>>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE ID = :id")
    suspend fun getData(id: Int): BleDataEntity

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID ORDER BY ID DESC LIMIT 1")
    fun getLastBleData(serviceUUID: String): Flow<BleDataEntity>

    @Insert(onConflict = REPLACE)
    suspend fun saveData(data: BleDataEntity)

    @Insert(onConflict = REPLACE)
    suspend fun saveAllData(dataList: List<BleDataEntity>)

    @Query("DELETE from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID")
    suspend fun wipeData(serviceUUID: String)

    @Query(
        "SELECT avg(temperature) as averageTemperature," +
            " min(temperature) as minimalTemperature," +
            " max(temperature) as maximalTemperature," +
            " avg(humidity) as averageHumidity," +
            " min(humidity) as minimalHumidity," +
            " max(humidity) as maximalHumidity," +
            " dateTime AS day" +
            " FROM ble_data GROUP BY date(dateTime, 'unixepoch', 'localtime')"
    )
    fun getAnalyticsPerDayFromDataBase(): Flow<List<BleDataCharacteristics>>

    @Query("Select * from ble_data WHERE  date(dateTime,'unixepoch','localtime') = :dateYYYYMMDD")
    fun getDataFilteredByDate(dateYYYYMMDD: String): Flow<List<BleDataEntity>>
}
