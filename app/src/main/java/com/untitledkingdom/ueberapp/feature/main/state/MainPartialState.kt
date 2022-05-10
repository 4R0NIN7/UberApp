package com.untitledkingdom.ueberapp.feature.main.state

import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState
import com.untitledkingdom.ueberapp.devices.data.BleData

interface MainPartialState : PartialState<MainState> {
    data class TabChanged(val newTabIndex: Int) : MainPartialState {
        override fun reduce(oldState: MainState): MainState = oldState.copy(tabIndex = newTabIndex)
    }

    data class SetAdvertisement(val advertisement: Advertisement?) :
        MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(advertisement = advertisement)
    }

    data class SetValues(val values: List<BleData>) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(values = values)
    }

    data class SetSelectedDate(val selectedDate: String) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(selectedDate = selectedDate)
    }

    data class SetIsScanning(val isScanning: Boolean) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetLastIdSend(val lastIdSend: Int) : MainPartialState {
        override fun reduce(oldState: MainState): MainState = oldState.copy(lastIdSend = lastIdSend)
    }

    data class SetLastBleData(val lastBleData: BleData) : MainPartialState {
        override fun reduce(oldState: MainState): MainState = oldState.copy(lastData = lastBleData)
    }

    data class SetFirstIdSend(val firstIdSend: Int) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(firstIdSend = firstIdSend)
    }
}
