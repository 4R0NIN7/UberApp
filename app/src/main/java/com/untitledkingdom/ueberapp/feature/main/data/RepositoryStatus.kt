package com.untitledkingdom.ueberapp.feature.main.data

import com.untitledkingdom.ueberapp.devices.data.BleData

sealed class RepositoryStatus {
    data class Success(val data: List<BleData>) : RepositoryStatus()
    data class Loading(val data: List<BleData>) : RepositoryStatus()
    object Error : RepositoryStatus()
}
