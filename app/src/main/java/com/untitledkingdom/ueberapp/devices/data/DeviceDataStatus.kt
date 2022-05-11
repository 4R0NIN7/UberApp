package com.untitledkingdom.ueberapp.devices.data

sealed class DeviceDataStatus {
    data class SuccessDeviceDataReading(val reading: Reading) : DeviceDataStatus()
    data class SuccessRetrievingDate(val date: List<Byte>) : DeviceDataStatus()
    object Error : DeviceDataStatus()
}
