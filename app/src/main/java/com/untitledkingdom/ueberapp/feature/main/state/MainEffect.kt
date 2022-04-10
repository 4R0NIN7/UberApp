package com.untitledkingdom.ueberapp.feature.main.state

sealed interface MainEffect {
    object GoToWelcome : MainEffect
    data class OpenDetailsForDay(val date: String) : MainEffect
    data class ShowError(val message: String) : MainEffect
    data class ShowData(val data: String) : MainEffect
}
