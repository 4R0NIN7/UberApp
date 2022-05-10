package com.untitledkingdom.ueberapp.feature.main.data

import com.untitledkingdom.ueberapp.devices.data.BleData

sealed class RepositoryStatus {
    data class SuccessBleData(val data: BleData) : RepositoryStatus()
    data class SuccessGetListBleData(val data: List<BleData>) : RepositoryStatus()
    object Error : RepositoryStatus()
}
