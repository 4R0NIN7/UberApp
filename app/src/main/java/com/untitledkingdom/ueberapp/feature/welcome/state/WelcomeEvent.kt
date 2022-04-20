package com.untitledkingdom.ueberapp.feature.welcome.state

import com.juul.kable.Advertisement

sealed interface WelcomeEvent {
    object StartService : WelcomeEvent
    object RemoveScannedDevices : WelcomeEvent
    object StartScanning : WelcomeEvent
    object StopScanning : WelcomeEvent
    data class StartConnectingToDevice(val advertisement: Advertisement) : WelcomeEvent
    data class SetScanningTo(val scanningTo: Boolean) : WelcomeEvent
    data class SetIsClickable(val isClickable: Boolean) : WelcomeEvent
}
