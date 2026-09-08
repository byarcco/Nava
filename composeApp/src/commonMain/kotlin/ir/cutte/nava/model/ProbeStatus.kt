package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProbeStatus {
    CONNECTED_PRIMARY,
    CONNECTED_BACKUP,
    INTERNET_ONLY,
    OFFLINE
}
