package com.circadianx.sleepsense.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.circadianx.sleepsense.data.model.ApneaEvent
import com.circadianx.sleepsense.data.model.HeartbeatPacket
import com.circadianx.sleepsense.data.model.PacketType
import com.circadianx.sleepsense.data.model.toPacketType
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Standard Bluetooth SPP UUID */
private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

/** The ESP32 advertises itself with this name */
const val DEVICE_NAME = "SleepSense_HW"

sealed class BtState {
    data object Disconnected : BtState()
    data object Scanning : BtState()
    data object Connecting : BtState()
    data object Connected : BtState()
    data class Error(val message: String) : BtState()
}

sealed class HardwarePacket {
    data class Apnea(val event: ApneaEvent, val sessionId: Long) : HardwarePacket()
    data class Heartbeat(val packet: HeartbeatPacket) : HardwarePacket()
}

@Singleton
class BluetoothManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val gson: Gson
) {
    private val _btState = MutableStateFlow<BtState>(BtState.Disconnected)
    val btState: StateFlow<BtState> = _btState.asStateFlow()

    private var socket: BluetoothSocket? = null

    /** Find already-paired SleepSense_HW device */
    @SuppressLint("MissingPermission")
    fun findPairedDevice(): BluetoothDevice? =
        bluetoothAdapter?.bondedDevices?.find { it.name == DEVICE_NAME }

    /** Connect to device and emit parsed packets as a Flow */
    @SuppressLint("MissingPermission")
    fun connectAndReceive(device: BluetoothDevice, sessionId: Long): Flow<HardwarePacket> = flow {
        _btState.value = BtState.Connecting
        try {
            val sock = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            sock.connect()
            socket = sock
            _btState.value = BtState.Connected

            val reader = BufferedReader(InputStreamReader(sock.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.trim()?.let { json ->
                    parsePacket(json, sessionId)?.let { emit(it) }
                }
            }
        } catch (e: Exception) {
            _btState.value = BtState.Error(e.message ?: "Connection failed")
        } finally {
            disconnect()
        }
    }.flowOn(Dispatchers.IO)

    private fun parsePacket(json: String, sessionId: Long): HardwarePacket? = try {
        val root = JsonParser.parseString(json).asJsonObject
        when (root["type"]?.asString?.toPacketType()) {
            PacketType.APNEA_EVENT -> HardwarePacket.Apnea(
                event = ApneaEvent(
                    sessionId          = sessionId,
                    timestamp          = root["timestamp"].asLong,
                    durationSec        = root["duration_sec"].asInt,
                    preSnoreDb         = root["pre_snore_db"].asFloat,
                    dominantFreqHz     = root["dominant_freq_hz"].asInt,
                    tempC              = root["temp_c"].asFloat,
                    humidityPct        = root["humidity_pct"].asFloat,
                    movementDetected   = root["movement_detected"].asBoolean
                ),
                sessionId = sessionId
            )
            PacketType.HEARTBEAT -> HardwarePacket.Heartbeat(
                gson.fromJson(json, HeartbeatPacket::class.java)
            )
            else -> null
        }
    } catch (_: Exception) { null }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        _btState.value = BtState.Disconnected
    }

    /** Send a configuration command to the ESP32 (e.g. change apnea threshold) */
    suspend fun sendCommand(command: String) = withContext(Dispatchers.IO) {
        try {
            socket?.outputStream?.write((command + "\n").toByteArray())
        } catch (_: Exception) {}
    }
}
