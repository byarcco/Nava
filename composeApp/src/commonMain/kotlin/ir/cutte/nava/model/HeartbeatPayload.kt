package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatPayload(
    val status: String = "alive",
    val batteryLevel: Int,
    val isCharging: Boolean,
    val deviceInfo: String,
    val timestamp: Long
)
