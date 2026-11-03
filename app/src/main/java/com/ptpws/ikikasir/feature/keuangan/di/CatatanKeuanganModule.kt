package com.ptpws.ikikasir.feature.keuangan.di

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.core.database.AppDatabase
import com.ptpws.ikikasir.feature.keuangan.data.local.dao.CatatanKeuanganDao
import com.ptpws.ikikasir.feature.keuangan.data.remote.datasource.CatatanKeuanganRemoteDataSource
import com.ptpws.ikikasir.feature.keuangan.data.remote.datasource.CatatanKeuanganRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.keuangan.data.repository.CatatanKeuanganRepositoryImpl
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
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
    abstract fun bindCatatanKeuanganRepository(
        impl: CatatanKeuanganRepositoryImpl
    ): CatatanKeuanganRepository

    @Binds
    @Singleton
    abstract fun bindCatatanKeuanganRemoteDataSource(
        impl: CatatanKeuanganRemoteDataSourceImpl
    ): CatatanKeuanganRemoteDataSource

    companion object {
        @Provides
        @Singleton
        fun provideCatatanKeuanganDao(
            database: AppDatabase
        ): CatatanKeuanganDao {
            return database.catatanKeuanganDao
        }
    }
}
