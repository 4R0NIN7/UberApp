package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.untitledkingdom.ueberapp.feature.data.BleData
import com.untitledkingdom.ueberapp.feature.data.BleDevice

data class MyState(
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val isClickable: Boolean = true,
    val advertisements: List<Advertisement> = listOf(),
    val selectedAdvertisement: Advertisement? = null,
    val device: BleDevice? = null,
    val readValues: List<BleData> = listOf()
)
