package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val workerUrl: String = "https://sms.cutte.ir",
    val whitelistSenders: Set<String> = emptySet(),
    val keywords: Set<String> = setOf("کد ورود"),
    val isServiceEnabled: Boolean = true,
    val totalDispatchedCount: Long = 0L,
    val serviceStartTimestamp: Long = 0L,
    val recentActivities: List<ForwardingActivity> = emptyList()
)
