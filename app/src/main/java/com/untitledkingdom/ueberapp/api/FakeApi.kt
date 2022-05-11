package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class FakeApi @Inject constructor(private val database: Database) : ApiService {
    companion object {
        const val DELAY_API: Long = 2000
    }

    override suspend fun sendDataToService(bleDatumEntities: List<BleDataEntity>): Response<Unit> {
        delay(DELAY_API)
        return Response.success(Unit)
    }

    override suspend fun getLastSynchronizedReading(): Int =
        database.getDao().getAllData().last().id
}
