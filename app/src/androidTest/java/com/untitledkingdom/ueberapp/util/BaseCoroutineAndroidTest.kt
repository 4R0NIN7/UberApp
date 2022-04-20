package com.untitledkingdom.ueberapp.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
abstract class BaseCoroutineAndroidTest(
    testDispatcher: TestDispatcher = StandardTestDispatcher()
) {
    @RegisterExtension
    @JvmField
    val scopeExtensionAndroid: AndroidMainCoroutineScopeExtension = AndroidMainCoroutineScopeExtension(testDispatcher)
    val dispatcher: TestDispatcher
        get() = scopeExtensionAndroid.dispatcher
}
