package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.devices.data.BleData
import com.untitledkingdom.ueberapp.feature.main.MainRepositoryConst
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class FakeApi @Inject constructor() : ApiService {
    override suspend fun sendDataToService(bleData: List<BleData>): Response<ResponseBody> {
        delay(MainRepositoryConst.DELAY_API)
        return Response.success<ResponseBody>(
            ApiConst.HTTP_OK,
            null
        )
    }
}
