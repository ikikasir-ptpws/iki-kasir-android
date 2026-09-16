package com.ptpws.ikikasir.feature.manajemenstok.di

import com.ptpws.ikikasir.feature.manajemenstok.data.remote.datasource.StokAdjustmentRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenstok.data.remote.datasource.StokAdjustmentRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.manajemenstok.data.repository.StokAdjustmentRepositoryImpl
import com.ptpws.ikikasir.feature.manajemenstok.domain.repository.StockMovementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManajemenStokModule {

    @Binds
    @Singleton
    abstract fun bindStokAdjustmentRemoteDataSource(
        impl: StokAdjustmentRemoteDataSourceImpl
    ): StokAdjustmentRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindStockMovementRepository(
        impl: StokAdjustmentRepositoryImpl
    ): StockMovementRepository
}
