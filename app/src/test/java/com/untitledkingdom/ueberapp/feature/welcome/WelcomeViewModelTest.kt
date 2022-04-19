package com.untitledkingdom.ueberapp.feature.welcome

import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

@ExperimentalCoroutinesApi
class WelcomeViewModelTest : BaseCoroutineTest() {
    private val dataStorage by lazy { mockk<DataStorage>() }
    private val kableService by lazy { mockk<KableService>() }
    private val viewModel: WelcomeViewModel by lazy { WelcomeViewModel(kableService, dataStorage) }

    @Test
    fun `initial state`() = processorTest(
        processor = { viewModel.processor },
        thenStates = {
            assertLast(
                WelcomeState()
            )
        }
    )
}
