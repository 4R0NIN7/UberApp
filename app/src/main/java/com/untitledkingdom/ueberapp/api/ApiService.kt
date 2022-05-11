package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(ApiConst.URL_POST_BLE_DATA)
    suspend fun sendDataToService(@Body bleDatumEntities: List<BleDataEntity>): Response<Unit>
}
