package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.feature.data.BleDevice

sealed interface MyEvent {
    object RemoveScannedDevices : MyEvent
    object StartScanning : MyEvent
    object StopScanning : MyEvent
    object ReadCharacteristic : MyEvent
    object StopReadingCharacteristic : MyEvent
    object ReadDataInLoop : MyEvent
    object RefreshDeviceData : MyEvent
    data class SetIsClickable(val isClickable: Boolean) : MyEvent
    data class TabChanged(val newTabIndex: Int) : MyEvent
    data class StartConnectingToDevice(val advertisement: Advertisement) : MyEvent
    data class EndConnectingToDevice(val device: BleDevice) : MyEvent
    data class SetScanningTo(val scanningTo: Boolean) : MyEvent
}
