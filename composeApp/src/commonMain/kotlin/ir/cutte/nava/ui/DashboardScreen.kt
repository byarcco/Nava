package ir.cutte.nava.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.cutte.nava.engine.IngestionOutcome
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.ProbeStatus
import ir.cutte.nava.ui.theme.AppIcons
import ir.cutte.nava.ui.theme.NavaBackground
import ir.cutte.nava.ui.theme.NavaErrorContainer
import ir.cutte.nava.ui.theme.NavaOnBackground
import ir.cutte.nava.ui.theme.NavaOnErrorContainer
import ir.cutte.nava.ui.theme.NavaOnPrimary
import ir.cutte.nava.ui.theme.NavaOnPrimaryContainer
import ir.cutte.nava.ui.theme.NavaOnSecondaryContainer
import ir.cutte.nava.ui.theme.NavaOnSuccessContainer
import ir.cutte.nava.ui.theme.NavaOnSurface
import ir.cutte.nava.ui.theme.NavaOnSurfaceVariant
import ir.cutte.nava.ui.theme.NavaOnWarningContainer
import ir.cutte.nava.ui.theme.NavaOutlineVariant
import ir.cutte.nava.ui.theme.NavaPrimary
import ir.cutte.nava.ui.theme.NavaPrimaryContainer
import ir.cutte.nava.ui.theme.NavaSecondary
import ir.cutte.nava.ui.theme.NavaSecondaryContainer
import ir.cutte.nava.ui.theme.NavaSuccessContainer
import ir.cutte.nava.ui.theme.NavaSurface
import ir.cutte.nava.ui.theme.NavaSurfaceVariant
import ir.cutte.nava.ui.theme.NavaWarningContainer
import ir.cutte.nava.ui.theme.getYekanFontFamily
import ir.cutte.nava.util.toPersianDigits
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isServiceEnabled: Boolean,
    probeStatus: ProbeStatus,
    uptimeText: String,
    totalDispatchedCount: Long,
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isOemAutostartConfigured: Boolean,
    isForegroundRunning: Boolean,
    recentActivities: List<ForwardingActivity>,
    onToggleService: (Boolean) -> Unit,
    onRequestSmsPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onLaunchOemAutostart: () -> Unit,
    onConfirmOemAutostart: () -> Unit,
    onToggleForegroundService: () -> Unit,
    onSimulateSms: suspend (String, String) -> IngestionOutcome,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        containerColor = NavaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نــوا",
                            fontFamily = getYekanFontFamily(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = NavaOnBackground
                        )
                        Text(
                            text = "درگاه هوشمند پیامک",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = NavaSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = "تنظیمات",
                                tint = NavaOnSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavaBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            item {
                ServiceStatusOverviewCard(
                    isServiceEnabled = isServiceEnabled,
                    uptimeText = uptimeText,
                    totalDispatchedCount = totalDispatchedCount,
                    onToggleService = onToggleService
                )
            }

            item {
                SimulatorCard(onSimulateSms = onSimulateSms)
            }

            item {
                SectionHeader(
                    title = "آماده‌سازی و دسترسی‌ها",
                    subtitle = "پیش‌نیازهای پایداری و فعالیت پیوسته در پس‌زمینه"
                )
            }

            item {
                PermissionCard(
                    stepNumber = "۱",
                    title = "دسترسی به پیامک‌ها",
                    description = "برای دریافت پیامک‌های حاوی کد ورود و انتقال خودکار آنها به سرور",
                    isGranted = hasSmsPermission,
                    actionButtonText = "اعطای دسترسی پیامک",
                    onAction = onRequestSmsPermission
                )
            }

            item {
                PermissionCard(
                    stepNumber = "۲",
                    title = "اعلان‌های سامانه",
                    description = "برای حفظ سرویس فعال و آگاهی از وضعیت لحظه‌ای سیستم",
                    isGranted = hasNotificationPermission,
                    actionButtonText = "اعطای دسترسی اعلان",
                    onAction = onRequestNotificationPermission
                )
            }

            item {
                PermissionCard(
                    stepNumber = "۳",
                    title = "بهینه‌سازی باتری",
                    description = "جلوگیری از بسته‌شدن خودکار برنامه توسط مدیریت مصرف انرژی سیستم‌عامل",
                    isGranted = isBatteryOptimizationIgnored,
                    actionButtonText = "حذف محدودیت مصرف باتری",
                    onAction = onRequestBatteryOptimization
                )
            }

            item {
                OemAutostartCard(
                    isConfigured = isOemAutostartConfigured,
                    onLaunch = {
                        onLaunchOemAutostart()
                        onConfirmOemAutostart()
                    }
                )
            }

            item {
                SectionHeader(
                    title = "سرویس پیش‌زمینه پایدار",
                    subtitle = "تضمین دریافت آنی پیامک‌ها حتی در صورت بسته‌بودن برنامه"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سرویس پس‌زمینه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavaOnSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isForegroundRunning) NavaSuccessContainer else NavaSecondaryContainer
                            ) {
                                Text(
                                    text = if (isForegroundRunning) "فعال و پایدار" else "غیرفعال",
                                    color = if (isForegroundRunning) NavaOnSuccessContainer else NavaOnSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Text(
                            text = "برای پایداری فرآیند دریافت پیامک‌ها در دستگاه‌های مختلف، فعال بودن این سرویس ضروری است.",
                            fontSize = 13.sp,
                            color = NavaSecondary,
                            lineHeight = 19.sp
                        )

                        if (!isForegroundRunning) {
                            Button(
                                onClick = onToggleForegroundService,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary
                                )
                            ) {
                                Text("شروع سرویس پیش‌زمینه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "فعالیت‌های اخیر",
                    subtitle = "گزارش آخرین پیامک‌های پردازش‌شده"
                )
            }

            if (recentActivities.isEmpty()) {
                item {
                    EmptyActivityCard()
                }
            } else {
                items(recentActivities, key = { it.id }) { activity ->
                    ActivityItemCard(activity)
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusOverviewCard(
    isServiceEnabled: Boolean,
    uptimeText: String,
    totalDispatchedCount: Long,
    onToggleService: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseAnimation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isServiceEnabled) NavaPrimary else NavaSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isServiceEnabled) Color(0xFFE0F2FE).copy(alpha = pulseAlpha) else NavaSecondary
                            )
                    )
                    Text(
                        text = if (isServiceEnabled) "درگاه پیامک فعال است" else "درگاه پیامک متوقف است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isServiceEnabled) NavaOnPrimary else NavaOnSurface
                    )
                }

                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NavaPrimary,
                        checkedTrackColor = NavaOnPrimary,
                        uncheckedThumbColor = NavaSecondary,
                        uncheckedTrackColor = NavaOutlineVariant
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "مدت فعالیت",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServiceEnabled) NavaOnPrimary.copy(alpha = 0.85f) else NavaSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uptimeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServiceEnabled) NavaOnPrimary else NavaOnSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "پیام‌های ارسال‌شده",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServiceEnabled) NavaOnPrimary.copy(alpha = 0.85f) else NavaSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalDispatchedCount.toPersianDigits(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServiceEnabled) NavaOnPrimary else NavaOnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorCard(
    onSimulateSms: suspend (String, String) -> IngestionOutcome
) {
    var isExpanded by remember { mutableStateOf(false) }
    var senderInput by remember { mutableStateOf("Raja.ir") }
    var bodyInput by remember { mutableStateOf("کد تایید شما: 123456") }
    var isProcessing by remember { mutableStateOf(false) }
    var simulationOutcome by remember { mutableStateOf<IngestionOutcome?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "شبیه‌ساز پیامک آزمایشی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavaOnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "بررسی فرآیند دریافت و ارسال بدون سیم‌کارت",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NavaSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = NavaSurfaceVariant
                ) {
                    Text(
                        text = if (isExpanded) "بستن" else "آزمایش",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavaPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = senderInput,
                        onValueChange = { senderInput = it },
                        label = { Text("شماره یا نام فرستنده") },
                        placeholder = { Text("مثلاً: Raja.ir یا 09123456789") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavaSurfaceVariant,
                            unfocusedContainerColor = NavaSurfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    OutlinedTextField(
                        value = bodyInput,
                        onValueChange = { bodyInput = it },
                        label = { Text("متن پیامک") },
                        placeholder = { Text("مثلاً: کد تایید شما: 123456") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavaSurfaceVariant,
                            unfocusedContainerColor = NavaSurfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Button(
                        onClick = {
                            if (!isProcessing && senderInput.isNotBlank() && bodyInput.isNotBlank()) {
                                isProcessing = true
                                simulationOutcome = null
                                coroutineScope.launch {
                                    val outcome = onSimulateSms(senderInput.trim(), bodyInput.trim())
                                    simulationOutcome = outcome
                                    isProcessing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavaPrimary,
                            contentColor = NavaOnPrimary
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = NavaOnPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("ارسال آزمایشی پیام", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    simulationOutcome?.let { outcome ->
                        val (bgColor, textColor, messageText) = when {
                            outcome.isDispatched -> Triple(
                                NavaSuccessContainer,
                                NavaOnSuccessContainer,
                                "پیام با موفقیت به سرور ارسال شد (کد ${outcome.httpStatusCode.toPersianDigits()})"
                            )
                            outcome.isMatched -> Triple(
                                NavaWarningContainer,
                                NavaOnWarningContainer,
                                "پیام در صف ارسال آفلاین قرار گرفت"
                            )
                            else -> Triple(
                                NavaErrorContainer,
                                NavaOnErrorContainer,
                                outcome.statusDescription
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bgColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = messageText,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = NavaOnBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NavaSecondary
        )
    }
}

@Composable
private fun PermissionCard(
    stepNumber: String,
    title: String,
    description: String,
    isGranted: Boolean,
    actionButtonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isGranted) NavaSuccessContainer else NavaSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stepNumber.toPersianDigits(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isGranted) NavaOnSuccessContainer else NavaOnSecondaryContainer
                            )
                        }
                    }

                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavaOnSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isGranted) NavaSuccessContainer else NavaErrorContainer
                ) {
                    Text(
                        text = if (isGranted) "تأیید شد" else "لازم است",
                        color = if (isGranted) NavaOnSuccessContainer else NavaOnErrorContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NavaSecondary,
                lineHeight = 19.sp
            )

            if (!isGranted) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavaPrimary,
                        contentColor = NavaOnPrimary
                    )
                ) {
                    Text(text = actionButtonText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun OemAutostartCard(
    isConfigured: Boolean,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NavaSuccessContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "۴".toPersianDigits(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavaOnSuccessContainer
                            )
                        }
                    }

                    Text(
                        text = "اجازه شروع خودکار",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavaOnSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = NavaSuccessContainer
                ) {
                    Text(
                        text = if (isConfigured) "تأیید شد" else "پیشنهادی",
                        color = NavaOnSuccessContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "فعال‌سازی شروع خودکار در تنظیمات اختصاصی دستگاه (مانند شیائومی، سامسونگ یا هواوی)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NavaSecondary,
                lineHeight = 19.sp
            )

            if (!isConfigured) {
                FilledTonalButton(
                    onClick = onLaunch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = NavaPrimaryContainer,
                        contentColor = NavaOnPrimaryContainer
                    )
                ) {
                    Text("باز کردن تنظیمات دستگاه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "هنوز پیامی ثبت نشده است",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavaOnSurface
            )
            Text(
                text = "پیامک‌های دریافتی مطابق با فیلترها در این بخش نمایش داده می‌شوند.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NavaSecondary
            )
        }
    }
}

@Composable
private fun ActivityItemCard(activity: ForwardingActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(
                    text = activity.normalizedSender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavaOnSurface
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (activity.isSuccess) NavaSuccessContainer else NavaErrorContainer
                ) {
                    Text(
                        text = if (activity.isSuccess) {
                            if (activity.httpStatusCode > 0) "ارسال شد (${activity.httpStatusCode.toPersianDigits()})" else "ارسال شد"
                        } else {
                            "در انتظار / خطا"
                        },
                        color = if (activity.isSuccess) NavaOnSuccessContainer else NavaOnErrorContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = activity.snippet,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NavaOnSurfaceVariant,
                maxLines = 2,
                lineHeight = 18.sp
            )

            val keyword = activity.matchedKeyword
            if (!keyword.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NavaPrimaryContainer
                ) {
                    Text(
                        text = "کلیدواژه: $keyword",
                        color = NavaOnPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
