package com.circadianx.sleepsense.data.model

enum class RiskLevel(val emoji: String, val label: String) {
    LOW("😊", "Low risk"),
    MEDIUM("😐", "Medium risk"),
    HIGH("😟", "High risk");

    companion object {
        fun fromAhi(ahi: Float): RiskLevel = when {
            ahi < 5f  -> LOW
            ahi < 15f -> MEDIUM
            else      -> HIGH
        }
    }
}
