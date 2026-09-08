package ir.cutte.nava.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.ui.theme.NavaError
import ir.cutte.nava.ui.theme.NavaOnPrimary
import ir.cutte.nava.ui.theme.NavaOnTertiary
import ir.cutte.nava.ui.theme.NavaPrimary
import ir.cutte.nava.ui.theme.NavaSecondary
import ir.cutte.nava.ui.theme.NavaSuccess
import ir.cutte.nava.ui.theme.NavaSurfaceVariant
import ir.cutte.nava.ui.theme.NavaTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isServiceEnabled: Boolean,
    uptimeText: String,
    totalDispatchedCount: Long,
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isForegroundRunning: Boolean,
    recentActivities: List<ForwardingActivity>,
    onToggleService: (Boolean) -> Unit,
    onRequestSmsPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onLaunchOemAutostart: () -> Unit,
    onToggleForegroundService: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nava",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = NavaPrimary
                        )
                        Text(
                            text = "Automated SMS Gateway",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NavaTertiary,
                            contentColor = NavaPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Settings", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ServiceStatusBanner(
                    isServiceEnabled = isServiceEnabled,
                    uptimeText = uptimeText,
                    totalDispatchedCount = totalDispatchedCount,
                    isHealthy = hasSmsPermission && hasNotificationPermission && isBatteryOptimizationIgnored,
                    onToggleService = onToggleService
                )
            }

            item {
                Text(
                    text = "Permission & Resilience Wizard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavaPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                WizardStepCard(
                    stepNumber = "1",
                    title = "SMS Access",
                    description = "Required to ingest and parse incoming SMS messages",
                    isGranted = hasSmsPermission,
                    actionLabel = "Grant SMS",
                    onAction = onRequestSmsPermission
                )
            }

            item {
                WizardStepCard(
                    stepNumber = "2",
                    title = "Notifications",
                    description = "Allows status alerts and ongoing gateway monitoring",
                    isGranted = hasNotificationPermission,
                    actionLabel = "Grant Notification",
                    onAction = onRequestNotificationPermission
                )
            }

            item {
                WizardStepCard(
                    stepNumber = "3",
                    title = "Battery Optimization",
                    description = "Prevents OS from suspending ingestion when idle",
                    isGranted = isBatteryOptimizationIgnored,
                    actionLabel = "Exempt Battery",
                    onAction = onRequestBatteryOptimization
                )
            }

            item {
                WizardStepCard(
                    stepNumber = "4",
                    title = "OEM Autostart Protection",
                    description = "Deep link to MIUI, EMUI, ColorOS, or OneUI power policies",
                    isGranted = false,
                    isOptionalNotice = true,
                    actionLabel = "Launch OEM Panel",
                    onAction = onLaunchOemAutostart
                )
            }

            item {
                WizardStepCard(
                    stepNumber = "5",
                    title = "Foreground Service",
                    description = "Active data-sync foreground notification for Android 14+",
                    isGranted = isForegroundRunning,
                    actionLabel = if (isForegroundRunning) "Restart Service" else "Start Service",
                    onAction = onToggleForegroundService
                )
            }

            item {
                Text(
                    text = "Recent Forwarding Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavaPrimary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (recentActivities.isEmpty()) {
                item {
                    EmptyActivityCard()
                }
            } else {
                items(recentActivities, key = { it.id }) { activity ->
                    ActivityItemCard(activity = activity)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ServiceStatusBanner(
    isServiceEnabled: Boolean,
    uptimeText: String,
    totalDispatchedCount: Long,
    isHealthy: Boolean,
    onToggleService: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavaPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isServiceEnabled -> NavaError
                                    isHealthy -> NavaSuccess
                                    else -> NavaTertiary
                                }
                            )
                    )
                    Text(
                        text = if (isServiceEnabled) "SYSTEM ACTIVE" else "SYSTEM STOPPED",
                        color = NavaOnPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 14.sp
                    )
                }
                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NavaPrimary,
                        checkedTrackColor = NavaTertiary,
                        uncheckedThumbColor = NavaSecondary,
                        uncheckedTrackColor = NavaSurfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "UPTIME",
                        fontSize = 11.sp,
                        color = NavaTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isServiceEnabled) uptimeText else "Offline",
                        fontSize = 18.sp,
                        color = NavaOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DISPATCHED",
                        fontSize = 11.sp,
                        color = NavaTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = totalDispatchedCount.toString(),
                        fontSize = 18.sp,
                        color = NavaOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardStepCard(
    stepNumber: String,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    isOptionalNotice: Boolean = false,
    onAction: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = NavaSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NavaSecondary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stepNumber,
                                color = NavaOnPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavaPrimary
                    )
                }

                if (!isOptionalNotice) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGranted) NavaSuccess.copy(alpha = 0.15f) else NavaError.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isGranted) "Granted" else "Required",
                            color = if (isGranted) NavaSuccess else NavaError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavaTertiary.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Recommended",
                            color = NavaOnTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = description,
                fontSize = 13.sp,
                color = NavaSecondary,
                lineHeight = 18.sp
            )

            if (!isGranted || isOptionalNotice) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavaSecondary,
                        contentColor = NavaOnPrimary
                    )
                ) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = NavaSurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "No Forwarding Events Yet",
                    fontWeight = FontWeight.Bold,
                    color = NavaPrimary,
                    fontSize = 15.sp
                )
                Text(
                    text = "Matched incoming SMS payloads will appear here in real-time",
                    fontSize = 12.sp,
                    color = NavaSecondary
                )
            }
        }
    }
}

@Composable
private fun ActivityItemCard(activity: ForwardingActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavaSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.normalizedSender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavaPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (activity.isSuccess) NavaSuccess.copy(alpha = 0.2f) else NavaError.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (activity.httpStatusCode > 0) "HTTP ${activity.httpStatusCode}" else "FAILED",
                        color = if (activity.isSuccess) NavaSuccess else NavaError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = activity.snippet,
                fontSize = 13.sp,
                color = NavaPrimary,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activity.matchedKeyword?.let { keyword ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NavaTertiary.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = keyword,
                            fontSize = 11.sp,
                            color = NavaOnTertiary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTimestamp(activity.timestamp),
                    fontSize = 11.sp,
                    color = NavaSecondary
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val seconds = (timestamp / 1000) % 60
    val minutes = (timestamp / (1000 * 60)) % 60
    val hours = (timestamp / (1000 * 60 * 60)) % 24
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
