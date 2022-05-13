package com.untitledkingdom.ueberapp.feature.main.data

import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.devices.data.DeviceReading

sealed class RepositoryStatus {
    data class SuccessBleData(val deviceReading: DeviceReading?) : RepositoryStatus()
    data class SuccessGetListBleData(val deviceReadings: List<DeviceReading>) : RepositoryStatus()
    data class SuccessBleCharacteristics(val bleCharacteristics: List<BleDataCharacteristics>) : RepositoryStatus()
    object Error : RepositoryStatus()
}
