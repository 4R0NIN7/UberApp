package com.untitledkingdom.ueberapp.feature.welcome.state

import com.juul.kable.Advertisement

data class WelcomeState(
    val isScanning: Boolean = false,
    val isClickable: Boolean = true,
    val isConnecting: Boolean = false,
    val advertisements: List<Advertisement> = listOf(),
    val advertisementsRssiMap: Map<Advertisement, List<Int>> = mapOf()
)
