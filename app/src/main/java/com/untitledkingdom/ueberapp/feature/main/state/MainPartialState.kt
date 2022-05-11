package com.untitledkingdom.ueberapp.feature.main.state

import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState
import com.untitledkingdom.ueberapp.database.data.BleDataCharacteristics
import com.untitledkingdom.ueberapp.devices.data.DeviceReading

interface MainPartialState : PartialState<MainState> {
    data class TabChanged(val newTabIndex: Int) : MainPartialState {
        override fun reduce(oldState: MainState): MainState = oldState.copy(tabIndex = newTabIndex)
    }

    data class SetAdvertisement(val advertisement: Advertisement?) :
        MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(advertisement = advertisement)
    }

    data class SetValues(val values: List<DeviceReading>, val date: String) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(values = values, selectedDate = date)
    }

    data class SetSelectedDate(val selectedDate: String) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(selectedDate = selectedDate)
    }

    data class SetIsScanning(val isScanning: Boolean) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetLastBleData(val lastDeviceReading: DeviceReading?) : MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(lastDeviceReading = lastDeviceReading)
    }

    data class SetDataCharacteristics(val dataCharacteristics: List<BleDataCharacteristics>) :
        MainPartialState {
        override fun reduce(oldState: MainState): MainState =
            oldState.copy(dataCharacteristics = dataCharacteristics)
    }
}
