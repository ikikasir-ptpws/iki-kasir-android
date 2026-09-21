package com.ptpws.ikikasir.feature.manajemenpengguna.domain.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val roleId: String = "Kasir",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    val inisial: String
        get() {
            if (fullName.isBlank()) return "U"
            val parts = fullName.trim().split("\\s+".toRegex())
            return if (parts.size >= 2) {
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            } else {
                "${fullName.first().uppercaseChar()}"
            }
        }

    val roleDisplayName: String
        get() = roleId.uppercase()
}
