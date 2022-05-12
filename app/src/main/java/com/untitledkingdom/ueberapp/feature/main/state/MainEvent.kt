package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEvent {
    object ResetValues : MainEvent
    object StartScanning : MainEvent
    object EndConnectingToDevice : MainEvent
    data class TabChanged(val newTabIndex: Int) : MainEvent
    data class OpenDetails(val date: String) : MainEvent
}
