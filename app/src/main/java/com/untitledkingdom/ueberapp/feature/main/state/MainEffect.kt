package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEffect {
    object GoBack : MainEffect
    object GoToWelcome : MainEffect
    object OpenDetailsForDay : MainEffect
    data class ShowError(val message: String) : MainEffect
    data class ShowData(val data: String) : MainEffect
}
