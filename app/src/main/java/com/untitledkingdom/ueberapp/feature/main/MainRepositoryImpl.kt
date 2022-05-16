package com.untitledkingdom.ueberapp.feature.main

import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.feature.main.data.RepositoryStatus
import com.untitledkingdom.ueberapp.utils.functions.toDeviceReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val database: Database,
) : MainRepository {
    override suspend fun wipeData(serviceUUID: String) {
        database.getDao().wipeData(serviceUUID)
    }

    override fun getDataFilteredByDate(dateYYYYMMDD: String, serviceUUID: String): Flow<RepositoryStatus> =
        database.getDao().getDataFilteredByDate(dateYYYYMMDD, serviceUUID = serviceUUID).map { list ->
            RepositoryStatus.SuccessGetListBleData(list.map { it.toDeviceReading() })
        }.catch {
            emit(RepositoryStatus.SuccessGetListBleData(listOf()))
        }

    override fun getLastDataFromDataBase(serviceUUID: String): Flow<RepositoryStatus> =
        database.getDao().getLastBleData(serviceUUID).map { data ->
            try {
                RepositoryStatus.SuccessBleData(data.toDeviceReading())
            } catch (e: Exception) {
                RepositoryStatus.SuccessBleData(deviceReading = null)
            }
        }

    override fun getCharacteristicsPerDay(): Flow<RepositoryStatus> =
        database.getDao().getAnalyticsPerDayFromDataBase().map { listAnalytics ->
            RepositoryStatus.SuccessBleCharacteristics(listAnalytics)
        }
}
