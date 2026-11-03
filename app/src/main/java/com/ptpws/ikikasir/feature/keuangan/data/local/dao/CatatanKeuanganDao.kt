package com.ptpws.ikikasir.feature.keuangan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ptpws.ikikasir.feature.keuangan.data.local.entity.CatatanKeuanganEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatatanKeuanganDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatatan(entity: CatatanKeuanganEntity)

    @Update
    suspend fun updateCatatan(entity: CatatanKeuanganEntity)

    @Query("DELETE FROM catatan_keuangan WHERE id = :id")
    suspend fun deleteCatatanById(id: String)

    @Query("SELECT * FROM catatan_keuangan ORDER BY tanggalMillis DESC, createdAtMillis DESC")
    fun getAllCatatanFlow(): Flow<List<CatatanKeuanganEntity>>

    @Query("SELECT * FROM catatan_keuangan WHERE id = :id LIMIT 1")
    suspend fun getCatatanById(id: String): CatatanKeuanganEntity?

    @Query("SELECT * FROM catatan_keuangan WHERE id = :id LIMIT 1")
    fun getCatatanByIdFlow(id: String): Flow<CatatanKeuanganEntity?>
}
