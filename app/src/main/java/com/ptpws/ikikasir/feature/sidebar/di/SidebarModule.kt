package com.ptpws.ikikasir.feature.sidebar.di

import com.ptpws.ikikasir.feature.sidebar.data.repository.SidebarRepositoryImpl
import com.ptpws.ikikasir.feature.sidebar.domain.repository.SidebarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SidebarModule {

    @Binds
    @Singleton
    abstract fun bindSidebarRepository(
        impl: SidebarRepositoryImpl
    ): SidebarRepository
}
