package com.untitledkingdom.ueberapp.feature.welcome.state

import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState

sealed interface WelcomePartialState : PartialState<WelcomeState> {
    object RemoveAdvertisements : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(advertisements = listOf())
    }

    data class SetIsScanning(val isScanning: Boolean) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetAdvertisements(val newAdvertisement: List<Advertisement>) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(advertisements = newAdvertisement)
    }

    data class SetIsClickable(val isClickable: Boolean) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(isClickable = isClickable)
    }

    data class SetIsConnecting(val isConnecting: Boolean) : WelcomePartialState {
        override fun reduce(oldState: WelcomeState): WelcomeState =
            oldState.copy(isConnecting = isConnecting)
    }
}
