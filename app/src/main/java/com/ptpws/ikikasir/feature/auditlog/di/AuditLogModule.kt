package com.ptpws.ikikasir.feature.auditlog.di

import com.ptpws.ikikasir.feature.auditlog.data.remote.datasource.AuditLogRemoteDataSource
import com.ptpws.ikikasir.feature.auditlog.data.remote.datasource.AuditLogRemoteDataSourceImpl
import com.ptpws.ikikasir.feature.auditlog.data.repository.AuditLogRepositoryImpl
import com.ptpws.ikikasir.feature.auditlog.domain.repository.AuditLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuditLogModule {

    @Binds
    @Singleton
    abstract fun bindAuditLogRemoteDataSource(
        impl: AuditLogRemoteDataSourceImpl
    ): AuditLogRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(
        impl: AuditLogRepositoryImpl
    ): AuditLogRepository
}
