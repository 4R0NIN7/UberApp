package com.untitledkingdom.ueberapp.feature.background

import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.DeviceDataStatus
import com.untitledkingdom.ueberapp.devices.data.DeviceReading
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.service.BackgroundContainer
import com.untitledkingdom.ueberapp.service.state.BackgroundEffect
import com.untitledkingdom.ueberapp.service.state.BackgroundEvent
import com.untitledkingdom.ueberapp.service.state.BackgroundState
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.functions.UtilFunctions
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@FlowPreview
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class BackgroundContainerTest : BaseCoroutineTest() {
    private val dataStorage by lazy { mockk<DataStorage>() }
    private val timeManager by lazy { mockk<TimeManager>() }
    private val repository by lazy { mockk<MainRepository>() }
    private val device = mockk<Device>()
    private val byteList = listOf(1.toByte(), 2.toByte(), 3.toByte(), 4.toByte())
    private val mainThreadSurrogate = UnconfinedTestDispatcher()
    private val backgroundContainer by lazy {
        BackgroundContainer(
            repository = repository,
            device = device,
            timeManager = timeManager,
            dispatcher = dispatcher
        )
    }
    private val localDateTime: LocalDateTime = LocalDateTime.of(
        1970,
        1,
        1,
        1,
        1,
        1
    )

    @Before
    fun before() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun initialState() = processorTest(
        processor = { backgroundContainer.processor },
        given = {
        },
        thenStates = {
            assertLast(
                BackgroundState()
            )
        }
    )

    @Test
    fun startReading() = processorTest(
        context = mainThreadSurrogate,
        processor = { backgroundContainer.processor },
        given = {
            val utilFunctions = UtilFunctions
            val deviceReading = mockk<DeviceReading>()
            mockkObject(utilFunctions)
            coEvery { dataStorage.getFromStorage(any()) } returns "00:11:22:33:AA:BB"
            coEvery { timeManager.provideCurrentLocalDateTime() } returns localDateTime
            coEvery { device.readDate(any(), any()) } returns DeviceDataStatus.SuccessDate(byteList)
            coEvery { device.write(any(), any(), any()) } returns Unit
            every { utilFunctions.toDateString(any()) } returns "111970"
            coEvery { device.observationOnDataCharacteristic() } returns flowOf(deviceReading)
            coEvery { repository.saveData(any(), any()) } returns Unit
        },
        whenEvent = BackgroundEvent.StartReading,
        thenEffects = {
            assertValues(
                BackgroundEffect.StartForegroundService,
                BackgroundEffect.SendBroadcastToActivity
            )
        }
    )

    @Test
    fun stopReading() = processorTest(
        processor = { backgroundContainer.processor },
        given = {
            coEvery { dataStorage.getFromStorage(any()) } returns "00:11:22:33:AA:BB"
            coEvery { timeManager.provideCurrentLocalDateTime() } returns localDateTime
        },
        whenEvent = BackgroundEvent.StopReading,
        thenEffects = {
            assertValues(
                BackgroundEffect.Stop
            )
        }
    )
}
