package com.ptpws.ikikasir.core.storage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_images")
data class PendingImageEntity(
    @PrimaryKey val id: String,
    val localPath: String,
    val storageFolder: String,
    val collection: String,
    val documentId: String,
    val firestoreField: String,
    val createdAt: Long = System.currentTimeMillis()
)
