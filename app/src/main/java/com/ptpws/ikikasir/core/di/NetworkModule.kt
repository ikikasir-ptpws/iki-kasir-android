package com.ptpws.ikikasir.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.core.network.NetworkMonitorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitorImpl: NetworkMonitorImpl
    ): NetworkMonitor
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
