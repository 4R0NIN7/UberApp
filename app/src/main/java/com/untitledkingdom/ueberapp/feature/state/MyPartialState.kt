package com.untitledkingdom.ueberapp.feature.state

import com.juul.kable.Advertisement
import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral
import com.tomcz.ellipse.PartialState

sealed interface MyPartialState : PartialState<MyState> {
    object RemoveAdvertisements : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = listOf())
    }

    data class SetIsScanning(val isScanning: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(isScanning = isScanning)
    }

    data class SetAdvertisements(val newAdvertisement: List<Advertisement>) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisements = newAdvertisement)
    }

    data class SetConnectedToPeripheral(val peripheral: Peripheral?) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(peripheral = peripheral)
    }

    data class SetServicesFromPeripheral(val services: List<DiscoveredService>) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(services = services)
    }

    data class SetConnectedToAdvertisement(val advertisement: Advertisement?) : MyPartialState {
        override fun reduce(oldState: MyState): MyState =
            oldState.copy(advertisement = advertisement)
    }

    data class TabChanged(val newTabIndex: Int) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(tabIndex = newTabIndex)
    }

    data class SetIsClickable(val isClickable: Boolean) : MyPartialState {
        override fun reduce(oldState: MyState): MyState = oldState.copy(isClickable = isClickable)
    }
}
