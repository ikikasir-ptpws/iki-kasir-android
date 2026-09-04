package com.ptpws.ikikasir.feature.kategori.di

import com.ptpws.ikikasir.feature.kategori.data.remote.datasource.KategoriRemoteDataSource
import com.ptpws.ikikasir.feature.kategori.data.remote.datasource.KategoriRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.kategori.data.repository.KategoriRepositoryImpl
import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KategoriModule {

    @Binds
    @Singleton
    abstract fun bindKategoriRemoteDataSource(
        kategoriRemoteDataSourceImpl: KategoriRemoteDataSourceImpl
    ): KategoriRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindKategoriRepository(
        kategoriRepositoryImpl: KategoriRepositoryImpl
    ): KategoriRepository
}
