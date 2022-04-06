package com.untitledkingdom.ueberapp.ble.data

import com.juul.kable.Advertisement

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Scanning : ScanStatus()
    data class Found(val advertisement: Advertisement) : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}
