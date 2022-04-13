package com.untitledkingdom.ueberapp.utils

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.untitledkingdom.ueberapp.BuildConfig
import com.untitledkingdom.ueberapp.api.ApiConst
import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.KableServiceImpl
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.DatabaseConstants
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConstants
import com.untitledkingdom.ueberapp.datastore.DataStorageImpl
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.feature.main.MainRepositoryImpl
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.date.TimeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Modules {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DataStorageConstants.DATA_STORE_NAME
    )

    @Provides
    @Singleton
    fun provideMockRestApiClient(): ApiService {
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .client(getMockRetrofitClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BuildConfig.URL)
            .build()
        return retrofit.create(ApiService::class.java)
    }

    private fun getMockRetrofitClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(ApiConst.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConst.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConst.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideDataBase(context: Application): Database =
        Room.databaseBuilder(
            context,
            Database::class.java,
            DatabaseConstants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideDataStorage(dataStorageImpl: DataStorageImpl): DataStorage {
        return dataStorageImpl
    }

    @Provides
    @Singleton
    fun provideDataStore(context: Application): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideCoroutine(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface BindModules {
    @Binds
    fun bindKableService(kableServiceImpl: KableServiceImpl): KableService

    @Binds
    fun bindMainRepository(mainRepositoryImpl: MainRepositoryImpl): MainRepository

    @Binds
    fun bindTimeManager(
        timeManagerImpl: TimeManagerImpl,
    ): TimeManager
}
