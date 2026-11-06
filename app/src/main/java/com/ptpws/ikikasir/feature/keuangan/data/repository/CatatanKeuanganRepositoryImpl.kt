package com.ptpws.ikikasir.feature.keuangan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.core.util.FirebaseStorageHelper
import com.ptpws.ikikasir.feature.keuangan.data.local.dao.CatatanKeuanganDao
import com.ptpws.ikikasir.feature.keuangan.data.remote.datasource.CatatanKeuanganRemoteDataSource
import com.ptpws.ikikasir.feature.keuangan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.keuangan.data.remote.dto.toDomain
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.domain.model.toDomain
import com.ptpws.ikikasir.feature.keuangan.domain.model.toEntity
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatatanKeuanganRepositoryImpl @Inject constructor(
    private val dao: CatatanKeuanganDao,
    private val remoteDataSource: CatatanKeuanganRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : CatatanKeuanganRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO)
    @Volatile private var isRemoteListenerStarted = false

    init {
        syncScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    syncPending()
                    startRemoteListener()
                }
            }
        }
    }

    override fun getAllCatatanFlow(): Flow<List<CatatanKeuangan>> {
        if (networkMonitor.isConnected()) startRemoteListener()

        return dao.getAllCatatanFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCatatanById(id: String): CatatanKeuangan? {
        return dao.getCatatanById(id)?.toDomain()
    }

    override fun getCatatanByIdFlow(id: String): Flow<CatatanKeuangan?> {
        return dao.getCatatanByIdFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertCatatan(catatan: CatatanKeuangan) {
        dao.insertCatatan(catatan.toEntity(isSynced = false))
        if (networkMonitor.isConnected()) syncPending()
    }

    override suspend fun updateCatatan(catatan: CatatanKeuangan) {
        dao.insertCatatan(catatan.toEntity(isSynced = false))
        if (networkMonitor.isConnected()) syncPending()
    }

    override suspend fun deleteCatatanById(id: String) {
        dao.deleteCatatanById(id)
        try {
            remoteDataSource.deleteCatatan(id)
        } catch (_: Exception) { }
    }

    private fun startRemoteListener() {
        if (isRemoteListenerStarted) return
        isRemoteListenerStarted = true
        syncScope.launch {
            try {
                remoteDataSource.getCatatanFlow().collect { dtos ->
                    val unsyncedIds = dao.getUnsyncedCatatan().map { it.id }.toSet()
                    dtos.asSequence()
                        .filterNot { it.id in unsyncedIds }
                        .map { it.toDomain().toEntity(isSynced = true) }
                        .forEach { dao.insertCatatan(it) }
                }
            } catch (error: Exception) {
                isRemoteListenerStarted = false
                Log.e("CatatanKeuanganRepo", "Realtime sync failed: ${error.message}", error)
            }
        }
    }

    private suspend fun syncPending() {
        if (!networkMonitor.isConnected()) return
        for (entity in dao.getUnsyncedCatatan()) {
            try {
                val localUri = entity.buktiUri.orEmpty()
                val uploadedUri = FirebaseStorageHelper.uploadLocalImage(
                    imageReference = localUri,
                    folderName = "financial_record_payment_proofs"
                )
                val syncedEntity = entity.copy(buktiUri = uploadedUri, isSynced = true)
                remoteDataSource.saveCatatan(syncedEntity.toDomain().toDto())
                dao.insertCatatan(syncedEntity)
            } catch (error: Exception) {
                Log.e("CatatanKeuanganRepo", "Failed syncing record ${entity.id}: ${error.message}", error)
            }
        }
    }
}
