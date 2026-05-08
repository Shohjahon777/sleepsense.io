package com.circadianx.sleepsense.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One apnea event as sent by the ESP32 over Bluetooth.
 *
 * JSON from hardware:
 * {
 *   "type": "APNEA_EVENT",
 *   "timestamp": 1741200000,
 *   "duration_sec": 14,
 *   "pre_snore_db": 62.4,
 *   "dominant_freq_hz": 340,
 *   "temp_c": 22.1,
 *   "humidity_pct": 48.5,
 *   "movement_detected": false
 * }
 */
@Entity(
    tableName = "apnea_events",
    foreignKeys = [
        ForeignKey(
            entity = SleepSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ApneaEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    /** Unix epoch seconds, from ESP32 RTC */
    val timestamp: Long,
    /** Pause duration in seconds (≥ 10 by definition) */
    val durationSec: Int,
    /** Pre-event snore loudness in dBFS */
    val preSnoreDb: Float,
    /** Dominant snore frequency in Hz */
    val dominantFreqHz: Int,
    /** Room temperature °C at event time */
    val tempC: Float,
    /** Relative humidity % at event time */
    val humidityPct: Float,
    /** Whether the MPU-6050 detected body movement during this event */
    val movementDetected: Boolean
)
