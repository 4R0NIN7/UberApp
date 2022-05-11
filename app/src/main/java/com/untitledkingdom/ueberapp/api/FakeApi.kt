package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class FakeApi @Inject constructor() : ApiService {
    companion object {
        const val DELAY_API: Long = 2000
    }

    override suspend fun sendDataToService(bleDatumEntities: List<BleDataEntity>): Response<Unit> {
        delay(DELAY_API)
        return Response.success(Unit)
    }
}
