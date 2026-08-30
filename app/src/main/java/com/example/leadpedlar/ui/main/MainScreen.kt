package com.example.leadpedlar.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadpedlar.LeadPedlarApp
import com.example.leadpedlar.calling.CallManager
import com.example.leadpedlar.data.model.CallAppType
import com.example.leadpedlar.data.model.LeadItem
import com.example.leadpedlar.theme.BgDark
import com.example.leadpedlar.theme.Cyan400
import com.example.leadpedlar.theme.Cyan500
import com.example.leadpedlar.theme.Emerald500
import com.example.leadpedlar.theme.SurfaceBorder
import com.example.leadpedlar.theme.SurfaceDark
import com.example.leadpedlar.theme.TextPrimary
import com.example.leadpedlar.theme.TextSecondary
import com.example.leadpedlar.ui.components.CallAppSelectorBottomSheet
import com.example.leadpedlar.ui.components.LeadPedlarWebView
import com.example.leadpedlar.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPrefs = LeadPedlarApp.instance.appPreferences
    val dialerPrefs = LeadPedlarApp.instance.dialerPreferences

    val serverUrl by appPrefs.serverUrlFlow.collectAsState(initial = "http://10.0.2.2:3000")
    val preferredApp by dialerPrefs.preferredAppFlow.collectAsState(initial = CallAppType.SYSTEM_CHOOSER)
    val alwaysAsk by dialerPrefs.alwaysAskFlow.collectAsState(initial = true)

    var currentCallingLead by remember { mutableStateOf<LeadItem?>(null) }
    var showCallingSheet by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var webKey by remember { mutableStateOf(0) }
    var pageTitle by remember { mutableStateOf("LeadPedlar") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Trigger call selector or direct call
    fun triggerCall(phoneNumber: String, leadName: String) {
        val lead = LeadItem(
            id = "call-target",
            name = leadName,
            phone = phoneNumber,
            city = "",
            status = "NEW",
            rowIndex = 1
        )

        // Check if there is an active default dialer and user didn't request "Always Ask"
        if (!alwaysAsk && preferredApp != CallAppType.SYSTEM_CHOOSER) {
            Toast.makeText(context, "Dialing via ${preferredApp.displayName}...", Toast.LENGTH_SHORT).show()
            CallManager.launchCall(context, preferredApp, phoneNumber)
            return
        }

        currentCallingLead = lead
        showCallingSheet = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BgDark,
        topBar = {
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo & App Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { webKey++ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald500.copy(alpha = 0.2f))
                                .border(1.dp, Emerald500.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LeadPedlar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (serverUrl.contains("10.0.2.2")) "Emulator Server" else if (serverUrl.contains("leadpedlar.xyz")) "Live Production" else "Custom Server",
                                fontSize = 10.sp,
                                color = Emerald500
                            )
                        }
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Calling Preference Indicator badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cyan500.copy(alpha = 0.15f))
                                .border(1.dp, Cyan500.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { showSettingsModal = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val label = if (alwaysAsk || preferredApp == CallAppType.SYSTEM_CHOOSER) "⚙️ Ask App" else "📞 ${preferredApp.displayName.take(8)}"
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Refresh button
                        IconButton(
                            onClick = { webKey++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Settings button
                        IconButton(
                            onClick = { showSettingsModal = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Web Application Container with JavaScript Bridge
            LeadPedlarWebView(
                url = serverUrl,
                onOpenCallSelector = { phone, name ->
                    triggerCall(phone, name)
                },
                onTitleChange = { pageTitle = it }
            )
        }

        // Native Calling App Selector Bottom Sheet Modal
        if (showCallingSheet && currentCallingLead != null) {
            val availableApps = remember(context, preferredApp) {
                CallManager.getAvailableCallApps(context, preferredApp)
            }

            CallAppSelectorBottomSheet(
                phoneNumber = currentCallingLead!!.phone,
                leadName = currentCallingLead!!.name,
                availableApps = availableApps,
                sheetState = sheetState,
                onDismiss = { showCallingSheet = false },
                onAppSelected = { appType, alwaysUse ->
                    showCallingSheet = false
                    coroutineScope.launch {
                        dialerPrefs.saveDefaultApp(appType, alwaysUseThisApp = alwaysUse)
                        if (alwaysUse) {
                            Toast.makeText(context, "Saved ${appType.displayName} as default dialer", Toast.LENGTH_SHORT).show()
                        }
                    }
                    CallManager.launchCall(context, appType, currentCallingLead!!.phone)
                }
            )
        }

        // Native Settings Modal
        if (showSettingsModal) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsModal = false },
                sheetState = sheetState,
                containerColor = BgDark,
                contentColor = TextPrimary
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    SettingsScreen(
                        onLogout = {
                            showSettingsModal = false
                            webKey++
                        }
                    )
                }
            }
        }
    }
}
