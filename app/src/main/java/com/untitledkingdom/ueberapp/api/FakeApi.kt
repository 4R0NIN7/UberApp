package com.untitledkingdom.ueberapp.api

import com.untitledkingdom.ueberapp.database.data.BleDataEntity
import kotlinx.coroutines.delay
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

class FakeApi @Inject constructor() : ApiService {
    companion object {
        const val DELAY_API: Long = 2000
        const val SEED = 100
    }

    override suspend fun sendDataToService(bleDatumEntities: List<BleDataEntity>): Response<Unit> {
        Timber.d("Sending data")
        delay(DELAY_API)
        val random = Random(SEED)
        return if (random.nextBoolean())
            Response.success(Unit)
        else Response.error(ApiConst.HTTP_UNAUTHORIZED, "".toResponseBody())
    }
}
