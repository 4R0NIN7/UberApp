package com.untitledkingdom.ueberapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.untitledkingdom.ueberapp.devices.data.BleData
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query("SELECT * from ${DatabaseConst.TABLE}")
    suspend fun getAllData(): List<BleData>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID")
    fun getAllDataFlow(serviceUUID: String): Flow<List<BleData>>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE ID = :id")
    suspend fun getData(id: Int): BleData

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID ORDER BY ID DESC LIMIT 1")
    fun getLastBleData(serviceUUID: String): Flow<BleData>

    @Insert(onConflict = REPLACE)
    suspend fun saveData(data: BleData)

    @Insert(onConflict = REPLACE)
    suspend fun saveAllData(dataList: List<BleData>)

    @Query("DELETE from ${DatabaseConst.TABLE} WHERE serviceUUID = :serviceUUID")
    suspend fun wipeData(serviceUUID: String)
}
