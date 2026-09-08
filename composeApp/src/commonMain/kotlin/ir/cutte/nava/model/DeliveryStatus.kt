package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
enum class DeliveryStatus {
    DISPATCHED_INSTANT,
    QUEUED_OFFLINE,
    DISPATCHED_REPLAYED,
    FAILED_EXPIRED
}
