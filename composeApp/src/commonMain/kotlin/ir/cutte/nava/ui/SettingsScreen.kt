package ir.cutte.nava.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.cutte.nava.model.DispatchResult
import ir.cutte.nava.ui.theme.NavaError
import ir.cutte.nava.ui.theme.NavaOnPrimary
import ir.cutte.nava.ui.theme.NavaOutline
import ir.cutte.nava.ui.theme.NavaPrimary
import ir.cutte.nava.ui.theme.NavaSecondary
import ir.cutte.nava.ui.theme.NavaSuccess
import ir.cutte.nava.ui.theme.NavaSurfaceVariant
import ir.cutte.nava.ui.theme.NavaTertiary
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gateway Configuration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = NavaPrimary
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            text = "Back",
                            fontWeight = FontWeight.Bold,
                            color = NavaPrimary
                        )
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
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = NavaSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SMS Ingestion Engine",
                                    fontWeight = FontWeight.Bold,
                                    color = NavaPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Enable automated background message capture",
                                    fontSize = 12.sp,
                                    color = NavaSecondary
                                )
                            }
                            Switch(
                                checked = isServiceEnabled,
                                onCheckedChange = onToggleService,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavaPrimary,
                                    checkedTrackColor = NavaTertiary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Periodic Telemetry Heartbeat",
                                    fontWeight = FontWeight.Bold,
                                    color = NavaPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Probes cloud worker health every 30 minutes",
                                    fontSize = 12.sp,
                                    color = NavaSecondary
                                )
                            }
                            Switch(
                                checked = isHeartbeatEnabled,
                                onCheckedChange = onToggleHeartbeat,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NavaPrimary,
                                    checkedTrackColor = NavaTertiary
                                )
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavaSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Dual-Endpoint Failover Architecture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavaPrimary
                        )

                        Text(
                            text = "Primary Target (Custom Domain)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavaSecondary
                        )
                        OutlinedTextField(
                            value = primaryUrlInput,
                            onValueChange = { primaryUrlInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavaPrimary,
                                unfocusedBorderColor = NavaOutline,
                                focusedTextColor = NavaPrimary,
                                unfocusedTextColor = NavaPrimary
                            )
                        )

                        Text(
                            text = "Secondary Failover Target (Workers.dev)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavaSecondary
                        )
                        OutlinedTextField(
                            value = secondaryUrlInput,
                            onValueChange = { secondaryUrlInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavaPrimary,
                                unfocusedBorderColor = NavaOutline,
                                focusedTextColor = NavaPrimary,
                                unfocusedTextColor = NavaPrimary
                            )
                        )

                        Text(
                            text = "Bearer Authorization Secret Token",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavaSecondary
                        )
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Optional shared secret") },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavaPrimary,
                                unfocusedBorderColor = NavaOutline,
                                focusedTextColor = NavaPrimary,
                                unfocusedTextColor = NavaPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onUpdatePrimaryWorkerUrl(primaryUrlInput)
                                    onUpdateSecondaryWorkerUrl(secondaryUrlInput)
                                    onUpdateAuthToken(tokenInput)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaPrimary,
                                    contentColor = NavaOnPrimary
                                )
                            ) {
                                Text("Save Endpoints & Secret", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavaSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Diagnostic Dispatcher",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavaPrimary
                        )
                        Text(
                            text = "Dispatches an active test payload testing failover resilience",
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NavaTertiary,
                                contentColor = NavaPrimary
                            ),
                            enabled = !isDispatchingTest
                        ) {
                            if (isDispatchingTest) {
                                CircularProgressIndicator(
                                    color = NavaPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            } else {
                                Text("Dispatch Test Payload", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        testResult?.let { result ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (result.isSuccess) NavaSuccess.copy(alpha = 0.15f) else NavaError.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (result.isSuccess) {
                                        "Success: HTTP ${result.statusCode} Accepted"
                                    } else {
                                        "Error: ${result.errorMessage ?: ("HTTP " + result.statusCode)}"
                                    },
                                    color = if (result.isSuccess) NavaSuccess else NavaError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavaSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Keyword Filter Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavaPrimary
                        )
                        Text(
                            text = "Any SMS containing these substrings will be forwarded automatically",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keywordInput,
                                onValueChange = { keywordInput = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("e.g. کد ورود, OTP, رمز") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavaPrimary,
                                    unfocusedBorderColor = NavaOutline
                                )
                            )
                            Button(
                                onClick = {
                                    if (keywordInput.isNotBlank()) {
                                        onAddKeyword(keywordInput.trim())
                                        keywordInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaSecondary,
                                    contentColor = NavaOnPrimary
                                )
                            ) {
                                Text("Add")
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            keywords.forEach { keyword ->
                                FilterChip(
                                    selected = true,
                                    onClick = { onRemoveKeyword(keyword) },
                                    label = { Text("$keyword  ✕", fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavaTertiary,
                                        selectedLabelColor = NavaPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavaSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Sender Whitelist Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavaPrimary
                        )
                        Text(
                            text = "All messages from these senders bypass keyword filtering",
                            fontSize = 12.sp,
                            color = NavaSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = senderInput,
                                onValueChange = { senderInput = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("0912..., BANKMELLI, 1000...") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavaPrimary,
                                    unfocusedBorderColor = NavaOutline
                                )
                            )
                            Button(
                                onClick = {
                                    if (senderInput.isNotBlank()) {
                                        onAddWhitelist(senderInput.trim())
                                        senderInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NavaSecondary,
                                    contentColor = NavaOnPrimary
                                )
                            ) {
                                Text("Add")
                            }
                        }

                        if (whitelistSenders.isEmpty()) {
                            Text(
                                text = "No senders whitelisted",
                                fontSize = 12.sp,
                                color = NavaSecondary
                            )
                        } else {
                            whitelistSenders.forEach { sender ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sender,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        color = NavaPrimary,
                                        fontSize = 14.sp
                                    )
                                    TextButton(onClick = { onRemoveWhitelist(sender) }) {
                                        Text("Remove", color = NavaError, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
