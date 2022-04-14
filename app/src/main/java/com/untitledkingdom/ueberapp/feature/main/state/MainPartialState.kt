package com.untitledkingdom.ueberapp.feature.main.state

import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState
import com.untitledkingdom.ueberapp.devices.data.BleData

interface MainPartialState : PartialState<MainState> {
    data class TabChanged(val newTabIndex: Int) : MainPartialState {
        override fun reduce(oldState: MainState): MainState = oldState.copy(tabIndex = newTabIndex)
    }

    data class SetAdvertisement(val advertisement: Advertisement?, val isPreparing: Boolean) :
        MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(advertisement = advertisement, isPreparing = false)
    }

    data class SetValues(val values: List<BleData>, val isPreparing: Boolean) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(values = values, isPreparing = isPreparing)
    }

    data class SetSelectedDate(val selectedDate: String) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(selectedDate = selectedDate)
    }

    data class SetIsScanning(val isScanning: Boolean) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(isScanning = isScanning)
    }
}
