package com.untitledkingdom.ueberapp.scanner.data

import com.juul.kable.Advertisement

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Scanning : ScanStatus()
    data class Found(val advertisement: Advertisement) : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}
