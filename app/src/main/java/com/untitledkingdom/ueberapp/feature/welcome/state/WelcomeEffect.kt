package com.untitledkingdom.ueberapp.feature.welcome.state

sealed interface WelcomeEffect {
    data class ShowError(val message: String) : WelcomeEffect
    data class ShowData(val data: String) : WelcomeEffect
    object GoToMain : WelcomeEffect
    object StartService : WelcomeEffect
}
