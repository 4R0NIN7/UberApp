package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import java.util.*

sealed interface MyEvent {
    object RemoveScannedDevices : MyEvent
    object StartScanning : MyEvent
    object StopScanning : MyEvent
    data class TabChanged(val newTabIndex: Int) : MyEvent
    data class ShowCharacteristics(val service: UUID) : MyEvent
    data class StartConnectingToDevice(val advertisement: Advertisement) : MyEvent
    data class EndConnectingToDevice(val device: Peripheral) : MyEvent
    data class SetScanningTo(val scanningTo: Boolean) : MyEvent
}
