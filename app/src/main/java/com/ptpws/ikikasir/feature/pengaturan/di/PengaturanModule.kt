package com.ptpws.ikikasir.feature.pengaturan.di

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.core.database.AppDatabase
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.NotaSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.NotaSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.NotaSettingRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.pengaturan.data.repository.NotaSettingRepositoryImpl
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.NotaSettingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PengaturanModule {

    @Provides
    @Singleton
    fun provideNotaSettingDao(database: AppDatabase): NotaSettingDao {
        return database.notaSettingDao
    }

    @Provides
    @Singleton
    fun provideNotaSettingRemoteDataSource(firestore: FirebaseFirestore): NotaSettingRemoteDataSource {
        return NotaSettingRemoteDataSourceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideNotaSettingRepository(repositoryImpl: NotaSettingRepositoryImpl): NotaSettingRepository {
        return repositoryImpl
    }

    @Provides
    @Singleton
    fun provideTaxSettingDao(database: AppDatabase): com.ptpws.ikikasir.feature.pengaturan.data.local.dao.TaxSettingDao {
        return database.taxSettingDao
    }

    @Provides
    @Singleton
    fun provideTaxSettingRemoteDataSource(firestore: FirebaseFirestore): com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.TaxSettingRemoteDataSource {
        return com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.TaxSettingRemoteDataSourceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideTaxSettingRepository(repositoryImpl: com.ptpws.ikikasir.feature.pengaturan.data.repository.TaxSettingRepositoryImpl): com.ptpws.ikikasir.feature.pengaturan.domain.repository.TaxSettingRepository {
        return repositoryImpl
    }
}
