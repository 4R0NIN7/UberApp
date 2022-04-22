package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.main.MainRepositoryConst
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class FakeApi @Inject constructor() : ApiService {
    override suspend fun sendDataToService(bleData: List<BleData>): Response<Unit> {
        delay(MainRepositoryConst.DELAY_API)
        return Response.success(Unit)
    }
}
