package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEffect {
    data class ShowError(val message: String) : MainEffect
    data class ShowData(val data: String) : MainEffect
    object GoToWelcome : MainEffect
}
