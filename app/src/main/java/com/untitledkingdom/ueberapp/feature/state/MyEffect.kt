package com.untitledkingdom.ueberapp.feature.state

sealed interface MyEffect {
    data class ShowError(val message: String) : MyEffect
    data class ShowData(val data: String) : MyEffect
    object GoToMain : MyEffect
    object GoToWelcome : MyEffect
}
