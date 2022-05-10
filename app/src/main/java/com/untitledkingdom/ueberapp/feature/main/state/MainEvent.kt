package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEvent {
    object StartCollectingData : MainEvent
    object StopCollectingData : MainEvent
    object StartScanning : MainEvent
    object EndConnectingToDevice : MainEvent
    data class TabChanged(val newTabIndex: Int) : MainEvent
    data class SetSelectedDate(val date: String) : MainEvent
}
