package com.ptpws.ikikasir.feature.promo.di

import com.ptpws.ikikasir.feature.promo.data.remote.datasource.PromoRemoteDataSource
import com.ptpws.ikikasir.feature.promo.data.remote.datasource.PromoRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.promo.data.repository.PromoRepositoryImpl
import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PromoModule {

    @Binds
    @Singleton
    abstract fun bindPromoRemoteDataSource(
        impl: PromoRemoteDataSourceImpl
    ): PromoRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPromoRepository(
        impl: PromoRepositoryImpl
    ): PromoRepository
}
