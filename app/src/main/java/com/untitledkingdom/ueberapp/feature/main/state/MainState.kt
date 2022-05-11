package com.untitledkingdom.ueberapp.feature.main.state

import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.devices.data.DeviceReading

data class MainState(
    val selectedDate: String = "",
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val advertisement: Advertisement? = null,
    val dataCharacteristics: List<BleDataCharacteristics> = listOf(),
    val values: List<DeviceReading> = listOf(),
    val lastDeviceReading: DeviceReading? = null,
)
