package com.untitledkingdom.ueberapp.feature.welcome

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEffect
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeEvent
import com.untitledkingdom.ueberapp.feature.welcome.state.WelcomeState
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.data.ScanStatus
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import com.untitledkingdom.ueberapp.utils.interval.FlowInterval
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

@ExperimentalCoroutinesApi
class WelcomeViewModelTest : BaseCoroutineTest() {
    private val dataStorage by lazy { mockk<DataStorage>() }
    private val kableService by lazy { mockk<ScanService>() }
    private val flowInterval by lazy { mockk<FlowInterval>() }
    private val viewModel: WelcomeViewModel by lazy {
        WelcomeViewModel(
            kableService,
            dataStorage,
            flowInterval
        )
    }
    private val advertisement = mockk<Advertisement>()
    private val peripheral = mockk<Peripheral>()
    private val defaultDelay = 1000L

    @Test
    fun `initial state`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf()
            coEvery { flowInterval.start(any()) } returns flowOf()
            every { flowInterval.defaultDelay } returns defaultDelay
        },
        thenStates = {
            assertLast(
                WelcomeState()
            )
        }
    )

    @Test
    fun `start scanning get advertisements`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf(ScanStatus.Found(advertisement))
            coEvery { advertisement.address } returns "ADDRESS"
            coEvery { flowInterval.start(any()) } returns flowOf()
            every { flowInterval.defaultDelay } returns defaultDelay
        },
        whenEvent = WelcomeEvent.StartScanning,
        thenStates = {
            assertLast(
                WelcomeState(advertisements = listOf(advertisement))
            )
        }
    )

    @Test
    fun `connect to previously connected device`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf(
                ScanStatus.ConnectToPreviouslyConnectedDevice(
                    advertisement
                )
            )
            every { flowInterval.defaultDelay } returns defaultDelay
            coEvery { flowInterval.start(any()) } returns flowOf()
            coEvery { dataStorage.getFromStorage(any()) } returns "ADDRESS"
            coEvery { kableService.returnPeripheral(any(), any()) } returns peripheral
            coEvery { peripheral.connect() } returns Unit
            coEvery { advertisement.address } returns "ADDRESS"
            coEvery { dataStorage.saveToStorage(any(), any()) } returns Unit
        },
        thenEffects = {
            assertLast(WelcomeEffect.GoToMain)
        }
    )

    @Test
    fun `don't start service due to lack of address`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf(ScanStatus.Found(advertisement))
            coEvery { dataStorage.getFromStorage(any()) } returns ""
            coEvery { flowInterval.start(any()) } returns flowOf()
            every { flowInterval.defaultDelay } returns defaultDelay
        },
        thenEffects = {
            assertEmpty()
        }
    )

    @Test
    fun `start connecting to device`() = processorTest(
        processor = { viewModel.processor },
        given = {
            coEvery { kableService.scan() } returns flowOf(ScanStatus.Found(advertisement))
            coEvery { kableService.returnPeripheral(any(), any()) } returns peripheral
            coEvery { peripheral.connect() } returns Unit
            coEvery { advertisement.address } returns "ADDRESS"
            coEvery { dataStorage.saveToStorage(any(), any()) } returns Unit
            coEvery { flowInterval.start(any()) } returns flowOf()
            every { flowInterval.defaultDelay } returns defaultDelay
        },
        whenEvent = WelcomeEvent.StartConnectingToDevice(advertisement),
        thenEffects = {
            assertLast(WelcomeEffect.GoToMain)
        }
    )
}
