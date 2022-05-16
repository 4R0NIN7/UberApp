package com.untitledkingdom.ueberapp.feature.background

import com.tomcz.ellipse.test.processorTest
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.devices.Device
import com.untitledkingdom.ueberapp.devices.data.Reading
import com.untitledkingdom.ueberapp.service.ReadingContainer
import com.untitledkingdom.ueberapp.service.ReadingRepository
import com.untitledkingdom.ueberapp.service.state.ReadingEffect
import com.untitledkingdom.ueberapp.service.state.ReadingEvent
import com.untitledkingdom.ueberapp.util.BaseCoroutineTest
import com.untitledkingdom.ueberapp.utils.functions.DateConverter
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@FlowPreview
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
class ReadingContainerTest : BaseCoroutineTest() {
    private val dataStorage by lazy { mockk<DataStorage>() }
    private val repository by lazy { mockk<ReadingRepository>() }
    private val device = mockk<Device>()
    private val mainThreadSurrogate = UnconfinedTestDispatcher()
    private val reading = mockk<Reading>()
    private val backgroundContainer by lazy {
        ReadingContainer(
            repository = repository,
            device = device,
            scope = CoroutineScope(SupervisorJob() + dispatcher)
        )
    }

    @Before
    fun before() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun startReading() = processorTest(
        context = mainThreadSurrogate,
        processor = { backgroundContainer.processor },
        given = {
            val utilFunctions = DateConverter
            mockkObject(utilFunctions)
            coEvery { dataStorage.getFromStorage(any()) } returns "00:11:22:33:AA:BB"
            coEvery { device.observationOnDataCharacteristic() } returns flowOf(reading)
            coEvery { repository.saveData(any(), any()) } returns Unit
            coEvery { repository.start(any()) } returns Unit
        },
        whenEvent = ReadingEvent.StartReading,
        thenEffects = {
            assertValues(
                ReadingEffect.SendBroadcastToActivity,
                ReadingEffect.StartNotifying(reading),
            )
        }
    )

    @Test
    fun stopReading() = processorTest(
        processor = { backgroundContainer.processor },
        given = {
            coEvery { dataStorage.getFromStorage(any()) } returns "00:11:22:33:AA:BB"
            coEvery { repository.stop() } returns Unit
        },
        whenEvent = ReadingEvent.StopReading,
        thenEffects = {
            assertLast(
                ReadingEffect.Stop
            )
        }
    )
}
