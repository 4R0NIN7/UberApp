package com.untitledkingdom.ueberapp.utils.interval

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FlowIntervalImpl @Inject constructor() : FlowInterval {

    override fun start(delay: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(delay)
        }
    }.cancellable().buffer()

    override val defaultDelay: Long
        get() = 30000L
}
