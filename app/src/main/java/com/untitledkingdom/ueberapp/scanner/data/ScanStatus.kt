package com.untitledkingdom.ueberapp.scanner.data

import com.juul.kable.Advertisement

sealed interface ScanStatus {
    object Stopped : ScanStatus
    object Scanning : ScanStatus
    object Omit : ScanStatus
    data class ConnectToPreviouslyConnectedDevice(val advertisement: Advertisement) : ScanStatus
    data class Found(val advertisement: Advertisement) : ScanStatus
    data class Failed(val message: CharSequence) : ScanStatus
}
