package com.example.leadpedlar.ui.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadpedlar.LeadPedlarApp
import com.example.leadpedlar.calling.CallManager
import com.example.leadpedlar.data.model.CallAppType
import com.example.leadpedlar.data.model.LeadItem
import com.example.leadpedlar.theme.BgDark
import com.example.leadpedlar.theme.Emerald500
import com.example.leadpedlar.theme.SurfaceBorder
import com.example.leadpedlar.theme.SurfaceDark
import com.example.leadpedlar.theme.TextMuted
import com.example.leadpedlar.theme.TextPrimary
import com.example.leadpedlar.ui.components.CallAppSelectorBottomSheet
import com.example.leadpedlar.ui.components.LeadPedlarWebView
import com.example.leadpedlar.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

enum class WebNavTab(val title: String, val path: String, val icon: @Composable () -> Unit) {
    HOME(
        title = "Home",
        path = "/",
        icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) }
    ),
    MARKETPLACE(
        title = "Marketplace",
        path = "/marketplace",
        icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace", modifier = Modifier.size(20.dp)) }
    ),
    LEADS(
        title = "My Leads",
        path = "/agent/leads",
        icon = { Icon(Icons.Default.ListAlt, contentDescription = "My Leads", modifier = Modifier.size(20.dp)) }
    ),
    ESCROW(
        title = "Escrow",
        path = "/agent/escrow",
        icon = { Icon(Icons.Default.Handshake, contentDescription = "Escrow", modifier = Modifier.size(20.dp)) }
    ),
    SIP_PHONE(
        title = "SIP Dialer",
        path = "/agent/sip",
        icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = "SIP Dialer", modifier = Modifier.size(20.dp)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPrefs = LeadPedlarApp.instance.appPreferences
    val dialerPrefs = LeadPedlarApp.instance.dialerPreferences

    val serverUrl by appPrefs.serverUrlFlow.collectAsState(initial = "https://www.leadpedlar.xyz")
    val preferredApp by dialerPrefs.preferredAppFlow.collectAsState(initial = CallAppType.SYSTEM_CHOOSER)
    val alwaysAsk by dialerPrefs.alwaysAskFlow.collectAsState(initial = true)

    var currentCallingLead by remember { mutableStateOf<LeadItem?>(null) }
    var showCallingSheet by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf(WebNavTab.HOME) }
    var activeWebUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BgDark,
        bottomBar = {
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    WebNavTab.entries.forEach { tab ->
                        val isSelected = (currentTab == tab)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                val base = serverUrl.trimEnd('/')
                                activeWebUrl = if (tab.path == "/") base else "$base${tab.path}"
                            },
                            icon = tab.icon,
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald500,
                                selectedTextColor = Emerald500,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Emerald500.copy(alpha = 0.15f)
                            )
                        )
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
            // Full-Screen High Performance Web View with Native Calling Bridge
            LeadPedlarWebView(
                url = activeWebUrl,
                onOpenCallSelector = { phone, name ->
                    val lead = LeadItem(
                        id = "call-target",
                        name = name,
                        phone = phone,
                        city = "",
                        status = "NEW",
                        rowIndex = 1
                    )
                    // Check if there is an active default dialer and user didn't request "Always Ask"
                    if (!alwaysAsk && preferredApp != CallAppType.SYSTEM_CHOOSER) {
                        Toast.makeText(context, "Dialing via ${preferredApp.displayName}...", Toast.LENGTH_SHORT).show()
                        CallManager.launchCall(context, preferredApp, phone)
                    } else {
                        currentCallingLead = lead
                        showCallingSheet = true
                    }
                },
                onLaunchSpecificApp = { appId, phone ->
                    val appType = CallAppType.fromId(appId)
                    CallManager.launchCall(context, appType, phone)
                },
                onResetDefaults = {
                    coroutineScope.launch {
                        dialerPrefs.resetToAlwaysAsk()
                        Toast.makeText(context, "Dialer defaults reset to Always Ask", Toast.LENGTH_SHORT).show()
                    }
                }
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

        // Native Settings & Dialer Preferences Modal
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
                            activeWebUrl = "$serverUrl/login"
                        }
                    )
                }
            }
        }
    }
}
