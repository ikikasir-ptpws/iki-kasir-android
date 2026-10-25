package com.ptpws.ikikasir.core.storage.di

import com.ptpws.ikikasir.core.storage.data.repository.StorageRepositoryImpl
import com.ptpws.ikikasir.core.storage.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageBindingModule {

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        impl: StorageRepositoryImpl
    ): StorageRepository
}
