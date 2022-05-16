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
import com.untitledkingdom.ueberapp.api.FakeApi
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.DatabaseConst
import com.untitledkingdom.ueberapp.database.TimeConverter
import com.untitledkingdom.ueberapp.datastore.DataStorage
import com.untitledkingdom.ueberapp.datastore.DataStorageConst
import com.untitledkingdom.ueberapp.datastore.DataStorageImpl
import com.untitledkingdom.ueberapp.feature.main.MainRepository
import com.untitledkingdom.ueberapp.feature.main.MainRepositoryImpl
import com.untitledkingdom.ueberapp.scanner.ScanService
import com.untitledkingdom.ueberapp.scanner.ScanServiceImpl
import com.untitledkingdom.ueberapp.service.ReadingRepository
import com.untitledkingdom.ueberapp.service.ReadingRepositoryImpl
import com.untitledkingdom.ueberapp.service.ReadingService
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.date.TimeManagerImpl
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@Module
@InstallIn(SingletonComponent::class)
object AppModules {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DataStorageConst.DATA_STORE_NAME
    )

    @Provides
    @Singleton
    fun provideRestApiClient(): ApiService {
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .client(getMockRetrofitClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BuildConfig.URL)
            .build()
        // return retrofit.create(ApiService::class.java)
        return FakeApi()
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
    fun provideDataBase(context: Application, timeManager: TimeManager): Database =
        Room.databaseBuilder(
            context,
            Database::class.java,
            DatabaseConst.DATABASE_NAME
        ).addTypeConverter(TimeConverter(timeManager = timeManager))
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDataStore(context: Application): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideCoroutine(@IoDispatcher dispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(SupervisorJob() + dispatcher)
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class DefaultDispatcher

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class IoDispatcher

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class MainDispatcher

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class MainImmediateDispatcher

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @MainImmediateDispatcher
    @Provides
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class ReadingScope

    @ReadingScope
    @Provides
    @Singleton
    fun provideScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@Module
@InstallIn(SingletonComponent::class)
interface Binds {
    @Binds
    fun bindKableService(kableServiceImpl: ScanServiceImpl): ScanService

    @Binds
    @Singleton
    fun bindMainRepository(mainRepositoryImpl: MainRepositoryImpl): MainRepository

    @Binds
    @Singleton
    fun bindReadingRepository(readingRepositoryImpl: ReadingRepositoryImpl): ReadingRepository

    @Binds
    @Singleton
    fun bindTimeManager(
        timeManagerImpl: TimeManagerImpl,
    ): TimeManager

    @Binds
    @Singleton
    fun bindDispatcher(androidDispatcherProvider: AndroidDispatchersProvider): DispatchersProvider

    @Binds
    @Singleton
    fun bindDataStorage(dataStorageImpl: DataStorageImpl): DataStorage
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@InstallIn(SingletonComponent::class)
@EntryPoint
interface ContainerDependencies {
    fun getRepository(): ReadingRepository
    fun getDataStorage(): DataStorage
    fun getTimeManager(): TimeManager
    @AppModules.IoDispatcher
    fun getDispatcher(): CoroutineDispatcher
}

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
@FlowPreview
@InstallIn(SingletonComponent::class)
@EntryPoint
interface ScopeProviderEntryPoint {
    @AppModules.ReadingScope
    fun scope(): CoroutineScope
}

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
@Component(dependencies = [ContainerDependencies::class])
interface ContainerComponent {
    fun inject(service: ReadingService)

    @Component.Builder
    interface Builder {
        fun scope(@AppModules.ReadingScope @BindsInstance scope: CoroutineScope): Builder
        fun dependencies(containerDependencies: ContainerDependencies): Builder
        fun build(): ContainerComponent
    }
}
