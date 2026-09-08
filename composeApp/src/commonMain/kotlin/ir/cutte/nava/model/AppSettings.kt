package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val primaryWorkerUrl: String = "https://sms.cutte.ir",
    val secondaryWorkerUrl: String = "https://sms-pipeline.imartrioss.workers.dev",
    val authToken: String = "85d8ecf6-5641-451a-a41d-20927eeccd28",
    val whitelistSenders: Set<String> = emptySet(),
    val keywords: Set<String> = setOf("کد ورود"),
    val isServiceEnabled: Boolean = true,
    val isHeartbeatEnabled: Boolean = true,
    val lastProbeStatus: ProbeStatus = ProbeStatus.CONNECTED_PRIMARY,
    val lastProbeTimestamp: Long = 0L,
    val totalDispatchedCount: Long = 0L,
    val serviceStartTimestamp: Long = 0L,
    val recentActivities: List<ForwardingActivity> = emptyList()
)
