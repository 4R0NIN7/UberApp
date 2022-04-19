package com.untitledkingdom.ueberapp.device

import com.untitledkingdom.ueberapp.utils.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher

class TestDispatcherProvider constructor(private val dispatcher: CoroutineDispatcher) :
    DispatchersProvider {
    override val io: CoroutineDispatcher
        get() = dispatcher
    override val main: CoroutineDispatcher
        get() = dispatcher
}
