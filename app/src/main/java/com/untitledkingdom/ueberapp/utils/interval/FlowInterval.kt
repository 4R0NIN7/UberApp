package com.untitledkingdom.ueberapp.utils.interval

import kotlinx.coroutines.flow.Flow

interface FlowInterval {
    val defaultDelay: Long
    fun start(delay: Long = defaultDelay): Flow<Unit>
}
