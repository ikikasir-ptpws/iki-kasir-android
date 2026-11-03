package com.ptpws.ikikasir.feature.keuangan.data.repository

import com.ptpws.ikikasir.feature.keuangan.data.local.dao.CatatanKeuanganDao
import com.ptpws.ikikasir.feature.keuangan.data.remote.datasource.CatatanKeuanganRemoteDataSource
import com.ptpws.ikikasir.feature.keuangan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.domain.model.toDomain
import com.ptpws.ikikasir.feature.keuangan.domain.model.toEntity
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatatanKeuanganRepositoryImpl @Inject constructor(
    private val dao: CatatanKeuanganDao,
    private val remoteDataSource: CatatanKeuanganRemoteDataSource
) : CatatanKeuanganRepository {

    override fun getAllCatatanFlow(): Flow<List<CatatanKeuangan>> {
        return dao.getAllCatatanFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCatatanById(id: String): CatatanKeuangan? {
        return dao.getCatatanById(id)?.toDomain()
    }

    override suspend fun insertCatatan(catatan: CatatanKeuangan) {
        dao.insertCatatan(catatan.toEntity())
        try {
            remoteDataSource.saveCatatan(catatan.toDto())
        } catch (_: Exception) { }
    }

    override suspend fun updateCatatan(catatan: CatatanKeuangan) {
        dao.updateCatatan(catatan.toEntity())
        try {
            remoteDataSource.saveCatatan(catatan.toDto())
        } catch (_: Exception) { }
    }

    override suspend fun deleteCatatanById(id: String) {
        dao.deleteCatatanById(id)
        try {
            remoteDataSource.deleteCatatan(id)
        } catch (_: Exception) { }
    }
}
