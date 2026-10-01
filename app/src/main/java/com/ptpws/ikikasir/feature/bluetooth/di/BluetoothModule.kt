package com.ptpws.ikikasir.feature.bluetooth.di

import android.content.Context
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.data.repository.BluetoothPrinterRepositoryImpl
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothPrinterPreferences(
        @ApplicationContext context: Context
    ): BluetoothPrinterPreferences {
        return BluetoothPrinterPreferences(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothPrinterRepository(
        repositoryImpl: BluetoothPrinterRepositoryImpl
    ): BluetoothPrinterRepository {
        return repositoryImpl
    }
}
