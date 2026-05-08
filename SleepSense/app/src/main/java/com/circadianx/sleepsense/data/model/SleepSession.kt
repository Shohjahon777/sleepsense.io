package com.circadianx.sleepsense.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One full night of recording.
 * AHI = (apnea events / total sleep hours).
 */
@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Recording start — unix epoch millis */
    val startTime: Long,
    /** Recording end — unix epoch millis */
    val endTime: Long,
    /** Computed AHI for this session */
    val ahi: Float,
    /** Total apnea events detected */
    val eventCount: Int,
    /** Average room temperature °C */
    val avgTempC: Float,
    /** Average relative humidity % */
    val avgHumidityPct: Float,
    /** AI insight text generated for this session */
    val aiInsight: String = ""
) {
    val sleepDurationMs: Long get() = endTime - startTime
    val riskLevel: RiskLevel get() = RiskLevel.fromAhi(ahi)
}
