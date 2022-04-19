package com.untitledkingdom.ueberapp.service

import com.tomcz.ellipse.Processor
import com.tomcz.ellipse.common.processor
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

typealias BackgroundProcessor = Processor<BackgroundEvent, Nothing, BackgroundEffect>

class BackgroundContainer @Inject constructor(
    private val scope: CoroutineScope,
    private val repository: MainRepository,
    private val dataStorage: DataStorage,
    private val timeManager: TimeManager
) {

    val processor: BackgroundProcessor = scope.processor(onEvent = { event ->
        when (event) {
            BackgroundEvent.StartReading -> TODO()
        }
    })
}

sealed interface BackgroundEvent {
    object StartReading : BackgroundEvent
}

sealed interface BackgroundEffect {
    object SendBroadcastToActivity : BackgroundEffect
    object StartForegroundService : BackgroundEffect
}
