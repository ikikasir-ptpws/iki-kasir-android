package com.ptpws.ikikasir.feature.antrean.di

import com.ptpws.ikikasir.feature.antrean.data.remote.datasource.AntreanRemoteDataSource
import com.ptpws.ikikasir.feature.antrean.data.remote.datasource.AntreanRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.antrean.data.repository.AntreanRepositoryImpl
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AntreanModule {

    @Binds
    @Singleton
    abstract fun bindAntreanRemoteDataSource(
        impl: AntreanRemoteDataSourceImpl
    ): AntreanRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAntreanRepository(
        impl: AntreanRepositoryImpl
    ): AntreanRepository
}
