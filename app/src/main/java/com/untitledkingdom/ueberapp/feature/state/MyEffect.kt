package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Peripheral

sealed interface MyEffect {
    data class ShowError(val message: String) : MyEffect
    data class ConnectToDevice(val device: Peripheral) : MyEffect
}
