package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class ForwardingActivity(
    val id: String,
    val timestamp: Long,
    val normalizedSender: String,
    val matchedKeyword: String?,
    val httpStatusCode: Int,
    val isSuccess: Boolean,
    val snippet: String,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.DISPATCHED_INSTANT
)
