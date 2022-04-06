package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral

data class MyState(
    val tabIndex: Int = 0,
    val isScanning: Boolean = false,
    val isClickable: Boolean = true,
    val advertisements: List<Advertisement> = listOf(),
    val advertisement: Advertisement? = null,
    val peripheral: Peripheral? = null,
    val services: List<DiscoveredService> = listOf()
)
