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
    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID")
    suspend fun getAllData(serviceUUID: String): List<BleDataEntity>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID ORDER BY dateTime DESC")
    fun getAllDataFlow(serviceUUID: String): Flow<List<BleDataEntity>>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID ORDER BY ID DESC LIMIT 1")
    fun getLastBleData(serviceUUID: String): Flow<BleDataEntity>

    @Query("SELECT ID from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID AND isSynchronized = 1 ORDER BY ID DESC LIMIT 1")
    fun getLastSentId(serviceUUID: String): Flow<Int>

    @Query("SELECT ID from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID AND isSynchronized = 0 ORDER BY ID DESC LIMIT 1")
    fun getLastId(serviceUUID: String): Flow<Int>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID AND isSynchronized = 0")
    suspend fun getDataNotSynchronized(serviceUUID: String): List<BleDataEntity>

    @Insert(onConflict = REPLACE)
    suspend fun saveData(data: BleDataEntity)

    @Insert(onConflict = REPLACE)
    suspend fun saveAllData(dataList: List<BleDataEntity>)

    @Query("DELETE from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID")
    suspend fun wipeData(serviceUUID: String)

    @Query("Select * from ble_data WHERE  date(dateTime,'unixepoch','localtime') = :dateYYYYMMDD")
    fun getDataFilteredByDate(dateYYYYMMDD: String): Flow<List<BleDataEntity>>

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

    @Query("SELECT count(*) as notSynchronized from ble_data WHERE serviceUUID = :serviceUUID AND isSynchronized = 0")
    fun countNotSynchronized(serviceUUID: String): Flow<Int?>
}
