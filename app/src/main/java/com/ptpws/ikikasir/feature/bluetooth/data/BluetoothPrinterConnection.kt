package com.ptpws.ikikasir.feature.bluetooth.data

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

object BluetoothPrinterConnection {
    private val mutex = Mutex()
    @Volatile
    private var socket: BluetoothSocket? = null
    @Volatile
    private var connectedAddress: String? = null

    suspend fun connect(context: Context, address: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (isConnected(address)) return@withLock
                closeConnection()

                val appContext = context.applicationContext
                val bluetoothManager =
                    appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                        ?: error("Bluetooth tidak tersedia di perangkat ini.")
                val adapter = bluetoothManager.adapter
                    ?: error("Bluetooth tidak tersedia di perangkat ini.")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    throw SecurityException("Izin Bluetooth diperlukan untuk menghubungkan printer.")
                }
                check(adapter.isEnabled) { "Aktifkan Bluetooth terlebih dahulu." }

                @Suppress("MissingPermission")
                val device = adapter.getRemoteDevice(address)
                val connectedSocket = openSocket(device)
                socket = connectedSocket
                connectedAddress = address
            }
        }
    }

    suspend fun send(context: Context, address: String, data: ByteArray) {
        connect(context, address)
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val activeSocket = socket
                if (connectedAddress != address || activeSocket?.isConnected != true) {
                    closeConnection()
                    error("Koneksi printer terputus. Hubungkan ulang printer sebelum mencetak.")
                }
                activeSocket.outputStream.write(data)
                activeSocket.outputStream.flush()
            }
        }
    }

    fun isConnected(address: String): Boolean =
        connectedAddress == address && socket?.isConnected == true

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            mutex.withLock { closeConnection() }
        }
    }

    private fun openSocket(device: BluetoothDevice): BluetoothSocket {
        val secureSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        try {
            secureSocket.connect()
            return secureSocket
        } catch (error: IOException) {
            try {
                secureSocket.close()
            } catch (closeError: IOException) {
                error.addSuppressed(closeError)
            }
        }

        val insecureSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
        try {
            insecureSocket.connect()
            return insecureSocket
        } catch (error: IOException) {
            try {
                insecureSocket.close()
            } catch (closeError: IOException) {
                error.addSuppressed(closeError)
            }
            throw IOException(
                "Gagal tersambung ke printer. Pastikan printer menyala, sudah dipasangkan, " +
                    "dan tidak sedang terhubung ke perangkat lain.",
                error
            )
        }
    }

    private fun closeConnection() {
        try {
            socket?.close()
        } finally {
            socket = null
            connectedAddress = null
        }
    }

    private val SPP_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
}
