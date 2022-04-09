package com.untitledkingdom.ueberapp.feature.welcome.state

import com.juul.kable.Advertisement

data class WelcomeState(
    val isScanning: Boolean = false,
    val isClickable: Boolean = true,
    val advertisements: List<Advertisement> = listOf(),
)
