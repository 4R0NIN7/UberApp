package com.untitledkingdom.ueberapp.feature.welcome.state

import android.bluetooth.le.ScanResult
import com.untitledkingdom.ueberapp.feature.welcome.data.ScannedDevice

data class WelcomeState(
    val isScanning: Boolean = false,
    val scanResults: List<ScanResult> = listOf(),
    val selectedDeviceToConnect: ScannedDevice = ScannedDevice(address = "", name = "")
)
