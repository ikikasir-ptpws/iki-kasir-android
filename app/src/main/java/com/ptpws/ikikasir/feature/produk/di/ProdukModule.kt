package com.ptpws.ikikasir.feature.produk.di

import com.ptpws.ikikasir.feature.produk.data.remote.datasource.ProdukRemoteDataSource
import com.ptpws.ikikasir.feature.produk.data.remote.datasource.ProdukRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.produk.data.repository.ProdukRepositoryImpl
import com.ptpws.ikikasir.feature.produk.domain.repository.ProdukRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProdukModule {

    @Binds
    @Singleton
    abstract fun bindProdukRemoteDataSource(
        produkRemoteDataSourceImpl: ProdukRemoteDataSourceImpl
    ): ProdukRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProdukRepository(
        produkRepositoryImpl: ProdukRepositoryImpl
    ): ProdukRepository
}