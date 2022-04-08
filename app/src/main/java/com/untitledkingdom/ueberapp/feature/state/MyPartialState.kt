package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.tomcz.ellipse.PartialState
import com.untitledkingdom.ueberapp.feature.data.BleData
import com.untitledkingdom.ueberapp.feature.data.BleDevice

sealed interface MyPartialState : PartialState<MyState> {
    object RemoveAdvertisements : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = listOf())
    }

    object ReleaseState : MyPartialState {
        override fun reduce(oldState: MyState): MyState = MyState()
    }

    data class SetIsScanning(val isScanning: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetAdvertisements(val newAdvertisement: List<Advertisement>) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = newAdvertisement)
    }

    data class SetConnectedToBleDevice(val bleDevice: BleDevice?) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(device = bleDevice)
    }

    data class TabChanged(val newTabIndex: Int) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(tabIndex = newTabIndex)
    }

    data class SetAdvertisement(val advertisement: Advertisement?) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(selectedAdvertisement = advertisement)
    }

    data class AddValue(val values: List<BleData>) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(readValues = oldState.readValues + values)
    }

    data class SetIsClickable(val isClickable: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(isClickable = isClickable)
    }
}
