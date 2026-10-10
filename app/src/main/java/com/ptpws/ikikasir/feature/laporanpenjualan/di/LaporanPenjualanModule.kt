package com.ptpws.ikikasir.feature.laporanpenjualan.di

import com.ptpws.ikikasir.feature.laporanpenjualan.data.repository.LaporanPenjualanRepositoryImpl
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.repository.LaporanPenjualanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LaporanPenjualanModule {

    @Binds
    @Singleton
    abstract fun bindLaporanPenjualanRepository(
        impl: LaporanPenjualanRepositoryImpl
    ): LaporanPenjualanRepository
}
