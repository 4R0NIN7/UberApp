package com.untitledkingdom.ueberapp.datastore

import kotlinx.coroutines.flow.Flow

interface DataStorage {
    suspend fun saveToStorage(key: String, value: String)
    suspend fun getFromStorage(key: String): String
    fun observeMacAddress(): Flow<String>
}
