package com.ptpws.ikikasir.feature.role.di

import com.ptpws.ikikasir.feature.role.data.remote.datasource.RoleRemoteDataSource
import com.ptpws.ikikasir.feature.role.data.remote.datasource.RoleRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.role.data.repository.RoleRepositoryImpl
import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoleModule {

    @Binds
    @Singleton
    abstract fun bindRoleRemoteDataSource(
        roleRemoteDataSourceImpl: RoleRemoteDataSourceImpl
    ): RoleRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindRoleRepository(
        roleRepositoryImpl: RoleRepositoryImpl
    ): RoleRepository
}
