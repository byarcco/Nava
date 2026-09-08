package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsPayload(
    val sender: String,
    val body: String,
    val timestamp: Long,
    val simSlot: Int,
    val matchedKeyword: String?,
    val deviceInfo: String,
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false
)
