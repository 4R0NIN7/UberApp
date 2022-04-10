package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEvent {
    object WipeData : MainEvent
    object StartScanning : MainEvent
    object StopScanning : MainEvent
    object ReadCharacteristic : MainEvent
    object StopReadingCharacteristic : MainEvent
    object RefreshDeviceData : MainEvent
    object EndConnectingToDevice : MainEvent
    object GoToDetails : MainEvent
    data class TabChanged(val newTabIndex: Int) : MainEvent
    data class SetSelectedDate(val date: String) : MainEvent
}
