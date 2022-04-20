package com.untitledkingdom.ueberapp.util

import com.untitledkingdom.ueberapp.utils.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher

@ExperimentalCoroutinesApi
class AndroidTestDispatcherProvider(
    val testDispatcher: TestDispatcher
) : DispatchersProvider {

    override val main: CoroutineDispatcher
        get() = testDispatcher

    override val default: CoroutineDispatcher
        get() = testDispatcher

    override val io: CoroutineDispatcher
        get() = testDispatcher

    override val unconfined: CoroutineDispatcher
        get() = testDispatcher
}
