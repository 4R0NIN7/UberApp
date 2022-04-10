package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEvent {
    object WipeData : MainEvent
    object StartScanning : MainEvent
    object StopScanning : MainEvent
    object ReadCharacteristic : MainEvent
    object StopReadingCharacteristic : MainEvent
    object RefreshDeviceData : MainEvent
    object EndConnectingToDevice : MainEvent
    data class TabChanged(val newTabIndex: Int) : MainEvent
    data class OpenDetailsForDay(val date: String) : MainEvent
}
