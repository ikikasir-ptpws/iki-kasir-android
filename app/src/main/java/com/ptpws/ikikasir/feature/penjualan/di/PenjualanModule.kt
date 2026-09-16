package com.ptpws.ikikasir.feature.penjualan.di

import com.ptpws.ikikasir.feature.penjualan.data.remote.datasource.PenjualanRemoteDataSource
import com.ptpws.ikikasir.feature.penjualan.data.remote.datasource.PenjualanRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.penjualan.data.repository.PenjualanRepositoryImpl
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PenjualanModule {

    @Binds
    @Singleton
    abstract fun bindPenjualanRemoteDataSource(
        impl: PenjualanRemoteDataSourceImpl
    ): PenjualanRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPenjualanRepository(
        impl: PenjualanRepositoryImpl
    ): PenjualanRepository
}
