package com.untitledkingdom.ueberapp.feature.main.state

import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.devices.data.BleData

data class MainState(
    val selectedDate: String = "",
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val advertisement: Advertisement? = null,
    val values: List<BleData> = listOf(),
    val firstIdSend: Int = 0,
    val lastIdSend: Int = 0
)
