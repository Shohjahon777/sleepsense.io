package com.circadianx.sleepsense.data.model

import com.google.gson.annotations.SerializedName

/**
 * Heartbeat packet sent by ESP32 every 60 seconds to confirm it's alive.
 *
 * JSON:
 * {
 *   "type": "HEARTBEAT",
 *   "avg_snore_db": 45.2,
 *   "temp_c": 22.1,
 *   "humidity_pct": 48.5,
 *   "status_code": 0
 * }
 */
data class HeartbeatPacket(
    @SerializedName("type")          val type: String,
    @SerializedName("avg_snore_db")  val avgSnoreDb: Float,
    @SerializedName("temp_c")        val tempC: Float,
    @SerializedName("humidity_pct")  val humidityPct: Float,
    @SerializedName("status_code")   val statusCode: Int
)

/** Incoming Bluetooth packet type discriminator */
enum class PacketType { APNEA_EVENT, HEARTBEAT, UNKNOWN }

fun String.toPacketType(): PacketType = when (this) {
    "APNEA_EVENT" -> PacketType.APNEA_EVENT
    "HEARTBEAT"   -> PacketType.HEARTBEAT
    else          -> PacketType.UNKNOWN
}
