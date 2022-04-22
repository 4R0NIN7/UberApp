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

    @Query("SELECT * from ${DatabaseConst.TABLE}")
    fun getAllDataFlow(): Flow<List<BleData>>

    @Query("SELECT * from ${DatabaseConst.TABLE} WHERE ID = :id")
    suspend fun getData(id: Int): BleData

    @Insert(onConflict = REPLACE)
    suspend fun saveData(data: BleData)

    @Insert(onConflict = REPLACE)
    suspend fun saveAllData(dataList: List<BleData>)

    @Query("DELETE from ${DatabaseConst.TABLE}")
    suspend fun wipeData()
}
