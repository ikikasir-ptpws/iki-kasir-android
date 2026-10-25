package com.ptpws.ikikasir.feature.catatankeuangan.di

import com.ptpws.ikikasir.core.database.AppDatabase
import com.ptpws.ikikasir.feature.catatankeuangan.data.local.dao.CatatanKeuanganDao
import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.datasource.CatatanKeuanganRemoteDataSource
import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.datasource.CatatanKeuanganRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.catatankeuangan.data.repository.CatatanKeuanganRepositoryImpl
import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatatanKeuanganModule {

    @Binds
    @Singleton
    abstract fun bindCatatanKeuanganRemoteDataSource(
        impl: CatatanKeuanganRemoteDataSourceImpl
    ): CatatanKeuanganRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCatatanKeuanganRepository(
        impl: CatatanKeuanganRepositoryImpl
    ): CatatanKeuanganRepository

}
