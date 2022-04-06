package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral

data class MyState(
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val advertisements: List<Advertisement> = listOf(),
    val device: Peripheral? = null
)
