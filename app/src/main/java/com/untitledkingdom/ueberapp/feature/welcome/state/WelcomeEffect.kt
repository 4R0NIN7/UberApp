package com.untitledkingdom.ueberapp.feature.welcome.state

import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice

sealed interface WelcomeEffect {
    object ScanDevices : WelcomeEffect
    object StopScanDevices : WelcomeEffect
    data class ConnectToDevice(val selectedDevice: ScannedDevice) : WelcomeEffect
}
