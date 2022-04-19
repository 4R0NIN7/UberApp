package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.devices.data.BleData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(ApiConst.URL_POST_BLE_DATA)
    suspend fun sendDataToService(@Body bleData: List<BleData>): Response<ResponseBody>
}
