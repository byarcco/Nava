package ir.cutte.nava.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ir.cutte.nava.model.DispatchResult
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
import ir.cutte.nava.ui.theme.NavaOutlineVariant
import ir.cutte.nava.ui.theme.NavaPrimary
import ir.cutte.nava.ui.theme.NavaPrimaryContainer
import ir.cutte.nava.ui.theme.NavaSecondary
import ir.cutte.nava.ui.theme.NavaSecondaryContainer
import ir.cutte.nava.ui.theme.NavaSuccessContainer
import ir.cutte.nava.ui.theme.NavaSurface
import ir.cutte.nava.ui.theme.NavaSurfaceVariant
import ir.cutte.nava.ui.theme.getYekanFontFamily
import ir.cutte.nava.util.toPersianDigits
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    primaryWorkerUrl: String,
    authToken: String,
    whitelistSenders: Set<String>,
    keywords: Set<String>,
    deviceId: String,
    deviceName: String,
    isServiceEnabled: Boolean,
    isHeartbeatEnabled: Boolean,
    isForwardAllEnabled: Boolean,
    onUpdatePrimaryWorkerUrl: (String) -> Unit,
    onUpdateAuthToken: (String) -> Unit,
    onUpdateDeviceName: (String) -> Unit,
    onAddWhitelist: (String) -> Unit,
    onRemoveWhitelist: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onToggleService: (Boolean) -> Unit,
    onToggleHeartbeat: (Boolean) -> Unit,
    onToggleForwardAll: (Boolean) -> Unit,
    onDispatchTestPayload: suspend () -> DispatchResult,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var primaryUrlInput by remember(primaryWorkerUrl) { mutableStateOf(primaryWorkerUrl) }
    var tokenInput by remember(authToken) { mutableStateOf(authToken) }
    var deviceNameInput by remember(deviceName) { mutableStateOf(deviceName) }
    var senderInput by remember { mutableStateOf("") }
    var keywordInput by remember { mutableStateOf("") }

    var isDispatchingTest by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<DispatchResult?>(null) }
    var showSavedMessage by remember { mutableStateOf(false) }
    var showDeviceSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NavaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنظیمات درگاه",
                        fontFamily = getYekanFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NavaOnBackground
                    )
                },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = NavaSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.ArrowForward,
                                contentDescription = "بازگشت",
                                tint = NavaOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "دریافت خودکار پیامک‌ها",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavaOnSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "پردازش آنی به محض دریافت پیام جدید در دستگاه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NavaSecondary
                                )
                            }
                            Switch(
                                checked = isServiceEnabled,
                                onCheckedChange = onToggleService,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavaOnPrimary,
                                    checkedTrackColor = NavaPrimary,
                                    uncheckedThumbColor = NavaSecondary,
                                    uncheckedTrackColor = NavaOutlineVariant
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "بررسی خودکار پایداری",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavaOnSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "پایش اتصال و سنجش دسترسی‌پذیری سرور هر ۳۰ دقیقه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NavaSecondary
                                )
                            }
                            Switch(
                                checked = isHeartbeatEnabled,
                                onCheckedChange = onToggleHeartbeat,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavaOnPrimary,
                                    checkedTrackColor = NavaPrimary,
                                    uncheckedThumbColor = NavaSecondary,
                                    uncheckedTrackColor = NavaOutlineVariant
                                )
                            )
                        }
                    }
                }
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
                        Text(
                            text = "مشخصات دستگاه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "شناسه فنی و نام نمایشی جهت تفکیک فیزیکی دستگاه‌ها در سرور",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary
                        )

                        OutlinedTextField(
                            value = deviceId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("شناسه یکتای دستگاه") },
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
                            value = deviceNameInput,
                            onValueChange = {
                                deviceNameInput = it
                                showDeviceSavedMessage = false
                            },
                            label = { Text("نام نمایشی دستگاه") },
                            placeholder = { Text("مثلاً: Samsung S23 یا گوشی دفتر") },
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

                        Button(
                            onClick = {
                                val trimmed = deviceNameInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    onUpdateDeviceName(trimmed)
                                    showDeviceSavedMessage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavaPrimary,
                                contentColor = NavaOnPrimary
                            )
                        ) {
                            Text("ذخیره نام دستگاه", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (showDeviceSavedMessage) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NavaSuccessContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "نام دستگاه با موفقیت ذخیره شد",
                                    color = NavaOnSuccessContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
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
                        Text(
                            text = "تنظیمات اتصال سرور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "آدرس دریافت پیامک و کلید احراز هویت اختصاصی",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary
                        )

                        OutlinedTextField(
                            value = primaryUrlInput,
                            onValueChange = { primaryUrlInput = it },
                            label = { Text("آدرس سرور اصلی") },
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
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            label = { Text("کلید امنیتی اتصال (Bearer Token)") },
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

                        Button(
                            onClick = {
                                onUpdatePrimaryWorkerUrl(primaryUrlInput)
                                onUpdateAuthToken(tokenInput)
                                showSavedMessage = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavaPrimary,
                                contentColor = NavaOnPrimary
                            )
                        ) {
                            Text("ذخیره تنظیمات سرور", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (showSavedMessage) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NavaSuccessContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "تنظیمات با موفقیت ذخیره شد",
                                    color = NavaOnSuccessContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "آزمایش ارتباط با سرور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "ارسال یک بسته آزمایشی برای اطمینان از صحت دسترسی به سرور اصلی و پشتیبان",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary,
                            lineHeight = 18.sp
                        )

                        Button(
                            onClick = {
                                if (!isDispatchingTest) {
                                    isDispatchingTest = true
                                    testResult = null
                                    coroutineScope.launch {
                                        val result = onDispatchTestPayload()
                                        testResult = result
                                        isDispatchingTest = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(50),
                            enabled = !isDispatchingTest,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavaPrimary,
                                contentColor = NavaOnPrimary
                            )
                        ) {
                            if (isDispatchingTest) {
                                CircularProgressIndicator(
                                    color = NavaOnPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("ارسال پیام آزمایشی به سرور", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        testResult?.let { result ->
                            val (bgColor, textColor, messageText) = if (result.isSuccess) {
                                Triple(
                                    NavaSuccessContainer,
                                    NavaOnSuccessContainer,
                                    "ارتباط برقرار شد (کد ${result.statusCode.toPersianDigits()})"
                                )
                            } else {
                                Triple(
                                    NavaErrorContainer,
                                    NavaOnErrorContainer,
                                    result.errorMessage ?: "خطا در برقراری ارتباط"
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = messageText,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "بررسی و ارسال تمامی پیام‌ها",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavaOnSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ارسال بی‌درنگ تمام پیامک‌های دریافتی دستگاه به سرور بدون نیاز به فیلتر",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NavaSecondary
                                )
                            }
                            Switch(
                                checked = isForwardAllEnabled,
                                onCheckedChange = onToggleForwardAll,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavaOnPrimary,
                                    checkedTrackColor = NavaPrimary,
                                    uncheckedThumbColor = NavaSecondary,
                                    uncheckedTrackColor = NavaOutlineVariant
                                )
                            )
                        }
                    }
                }
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
                        Text(
                            text = "فرستنده‌های معتبر",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "پیامک‌های دریافتی از این فرستنده‌ها بلافاصله به سرور فرستاده می‌شوند",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary
                        )

                        if (isForwardAllEnabled) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = NavaSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "با فعال بودن گزینه بررسی و ارسال تمامی پیام‌ها، این بخش غیرفعال است و کلیه پیامک‌ها خودکار منتقل می‌شوند.",
                                    color = NavaSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = senderInput,
                                onValueChange = { senderInput = it },
                                enabled = !isForwardAllEnabled,
                                placeholder = { Text("مثلاً: Raja.ir، 0912، 1000", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = NavaSurfaceVariant,
                                    unfocusedContainerColor = NavaSurfaceVariant,
                                    disabledContainerColor = NavaSurfaceVariant.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent
                                )
                            )

                            Button(
                                onClick = {
                                    val trimmed = senderInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onAddWhitelist(trimmed)
                                        senderInput = ""
                                    }
                                },
                                enabled = !isForwardAllEnabled,
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(vertical = 0.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary,
                                    disabledContainerColor = NavaOutlineVariant,
                                    disabledContentColor = NavaSecondary
                                )
                            ) {
                                Text("افزودن", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (whitelistSenders.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                whitelistSenders.forEach { sender ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isForwardAllEnabled) NavaSecondaryContainer.copy(alpha = 0.5f) else NavaSecondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = sender,
                                                color = NavaOnSecondaryContainer,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!isForwardAllEnabled) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .clickable { onRemoveWhitelist(sender) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = AppIcons.Close,
                                                        contentDescription = "حذف",
                                                        tint = NavaOnSecondaryContainer,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                        Text(
                            text = "کلیدواژه‌های رهگیری",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "پیامک‌هایی که شامل هر یک از این کلمات باشند دریافت و ارسال خواهند شد",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NavaSecondary
                        )

                        if (isForwardAllEnabled) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = NavaSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "با فعال بودن گزینه بررسی و ارسال تمامی پیام‌ها، نیازی به تعیین کلیدواژه نیست و تمامی پیام‌ها ارسال خواهند شد.",
                                    color = NavaSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keywordInput,
                                onValueChange = { keywordInput = it },
                                enabled = !isForwardAllEnabled,
                                placeholder = { Text("مثلاً: کد ورود، رمز موقت", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = NavaSurfaceVariant,
                                    unfocusedContainerColor = NavaSurfaceVariant,
                                    disabledContainerColor = NavaSurfaceVariant.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent
                                )
                            )

                            Button(
                                onClick = {
                                    val trimmed = keywordInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onAddKeyword(trimmed)
                                        keywordInput = ""
                                    }
                                },
                                enabled = !isForwardAllEnabled,
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(vertical = 0.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary,
                                    disabledContainerColor = NavaOutlineVariant,
                                    disabledContentColor = NavaSecondary
                                )
                            ) {
                                Text("افزودن", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (keywords.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                keywords.forEach { keyword ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isForwardAllEnabled) NavaPrimaryContainer.copy(alpha = 0.5f) else NavaPrimaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = keyword,
                                                color = NavaOnPrimaryContainer,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!isForwardAllEnabled) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .clickable { onRemoveKeyword(keyword) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = AppIcons.Close,
                                                        contentDescription = "حذف",
                                                        tint = NavaOnPrimaryContainer,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
