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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    primaryWorkerUrl: String,
    secondaryWorkerUrl: String,
    authToken: String,
    whitelistSenders: Set<String>,
    keywords: Set<String>,
    isServiceEnabled: Boolean,
    isHeartbeatEnabled: Boolean,
    onUpdatePrimaryWorkerUrl: (String) -> Unit,
    onUpdateSecondaryWorkerUrl: (String) -> Unit,
    onUpdateAuthToken: (String) -> Unit,
    onAddWhitelist: (String) -> Unit,
    onRemoveWhitelist: (String) -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onToggleService: (Boolean) -> Unit,
    onToggleHeartbeat: (Boolean) -> Unit,
    onDispatchTestPayload: suspend () -> DispatchResult,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var primaryUrlInput by remember(primaryWorkerUrl) { mutableStateOf(primaryWorkerUrl) }
    var secondaryUrlInput by remember(secondaryWorkerUrl) { mutableStateOf(secondaryWorkerUrl) }
    var tokenInput by remember(authToken) { mutableStateOf(authToken) }
    var senderInput by remember { mutableStateOf("") }
    var keywordInput by remember { mutableStateOf("") }

    var isDispatchingTest by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<DispatchResult?>(null) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NavaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنظیمات درگاه",
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
                            text = "تنظیمات اتصال سرور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "آدرس‌های دریافت پیامک و کلید احراز هویت اختصاصی",
                            fontSize = 12.sp,
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
                            value = secondaryUrlInput,
                            onValueChange = { secondaryUrlInput = it },
                            label = { Text("آدرس سرور پشتیبان") },
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
                                onUpdateSecondaryWorkerUrl(secondaryUrlInput)
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
                                    fontWeight = FontWeight.Medium,
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
                            text = "ارسال یک پیام نمونه برای اطمینان از سلامت شبکه و پاسخ‌دهی سرور",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )

                        FilledTonalButton(
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
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NavaPrimaryContainer,
                                contentColor = NavaOnPrimaryContainer
                            )
                        ) {
                            if (isDispatchingTest) {
                                CircularProgressIndicator(
                                    color = NavaOnPrimaryContainer,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("بررسی اتصال به سرور", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        testResult?.let { result ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (result.isSuccess) NavaSuccessContainer else NavaErrorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (result.isSuccess) "ارتباط با سرور با موفقیت برقرار شد" else "خطا در برقراری ارتباط با سرور",
                                    color = if (result.isSuccess) NavaOnSuccessContainer else NavaOnErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
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
                            text = "کلمات کلیدی مجاز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "تنها پیامک‌هایی که حاوی این عبارات باشند به سرور ارسال می‌شوند",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )

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
                                placeholder = { Text("مثلاً: کد ورود، رمز پویا", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
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
                                    val trimmed = keywordInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onAddKeyword(trimmed)
                                        keywordInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxHeight(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary
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
                                        color = NavaPrimaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = keyword,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = NavaOnPrimaryContainer
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
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
                            text = "شماره‌های معتبر فرستنده",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavaOnSurface
                        )
                        Text(
                            text = "پیامک‌های این فرستنده‌ها همواره بدون نیاز به کلمه کلیدی ارسال می‌شوند",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )

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
                                placeholder = { Text("مثلاً: BANKMELLI، 0912، 1000", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
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
                                    val trimmed = senderInput.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onAddWhitelist(trimmed)
                                        senderInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxHeight(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary
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
                                        color = NavaSecondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = sender,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = NavaOnSecondaryContainer
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
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
    }
}
