package com.untitledkingdom.ueberapp.feature.main.data

import com.untitledkingdom.ueberapp.devices.data.BleData

sealed class RepositoryStatus {
    data class SuccessBleData(val data: List<BleData>) : RepositoryStatus()
    data class SuccessString(val data: String) : RepositoryStatus()
    data class Loading(val data: List<BleData>) : RepositoryStatus()
    object Success : RepositoryStatus()
    object Error : RepositoryStatus()
}
