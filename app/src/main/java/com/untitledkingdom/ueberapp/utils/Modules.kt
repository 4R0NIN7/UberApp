package com.untitledkingdom.ueberapp.utils

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.untitledkingdom.ueberapp.BuildConfig
import com.untitledkingdom.ueberapp.api.ApiService
import com.untitledkingdom.ueberapp.ble.KableService
import com.untitledkingdom.ueberapp.ble.KableServiceImpl
import com.untitledkingdom.ueberapp.database.Database
import com.untitledkingdom.ueberapp.database.DatabaseConstants
import com.untitledkingdom.ueberapp.feature.Repository
import com.untitledkingdom.ueberapp.feature.RepositoryImpl
import com.untitledkingdom.ueberapp.utils.date.TimeManager
import com.untitledkingdom.ueberapp.utils.date.TimeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Modules {
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
}

@Module
@InstallIn(SingletonComponent::class)
interface BindModules {
    @Binds
    fun bindKableService(kableServiceImpl: KableServiceImpl): KableService

    @Binds
    fun bindRepository(repositoryImpl: RepositoryImpl): Repository

    @Binds
    fun bindTimeManager(
        timeManagerImpl: TimeManagerImpl,
    ): TimeManager
}
