package ir.cutte.nava.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmsPayload(
    val code: String,
    val timestamp: Long,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_name")
    val deviceName: String,
    val sender: String = "",
    val body: String = "",
    val simSlot: Int = 0,
    val matchedKeyword: String? = null,
    val deviceInfo: String = "",
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false
)
