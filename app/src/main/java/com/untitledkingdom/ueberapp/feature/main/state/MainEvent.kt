package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEvent {
    object StartScanning : MainEvent
    object StopScanning : MainEvent
    object ReadCharacteristic : MainEvent
    object StopReadingCharacteristic : MainEvent
    object RefreshDeviceData : MainEvent
    data class TabChanged(val newTabIndex: Int) : MainEvent
    object EndConnectingToDevice : MainEvent
}
