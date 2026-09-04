package com.ptpws.ikikasir.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isConnected(): Boolean
}
