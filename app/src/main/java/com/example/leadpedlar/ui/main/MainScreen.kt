package com.example.leadpedlar.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadpedlar.LeadPedlarApp
import com.example.leadpedlar.data.model.User
import com.example.leadpedlar.theme.BgDark
import com.example.leadpedlar.theme.Emerald500
import com.example.leadpedlar.theme.SurfaceBorder
import com.example.leadpedlar.theme.SurfaceDark
import com.example.leadpedlar.theme.TextMuted
import com.example.leadpedlar.theme.TextPrimary
import com.example.leadpedlar.ui.screens.admin.AdminOverviewScreen
import com.example.leadpedlar.ui.screens.auth.LoginScreen
import com.example.leadpedlar.ui.screens.leads.LeadsScreen
import com.example.leadpedlar.ui.screens.marketplace.MarketplaceScreen
import com.example.leadpedlar.ui.screens.settings.SettingsScreen

enum class AppTab(val title: String, val icon: @Composable () -> Unit, val adminOnly: Boolean = false) {
    ADMIN(
        title = "Admin",
        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", modifier = Modifier.size(22.dp)) },
        adminOnly = true
    ),
    LEADS(
        title = "Leads",
        icon = { Icon(Icons.Default.Phone, contentDescription = "Leads", modifier = Modifier.size(22.dp)) }
    ),
    MARKETPLACE(
        title = "Marketplace",
        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Marketplace", modifier = Modifier.size(22.dp)) }
    ),
    SETTINGS(
        title = "Settings",
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(22.dp)) }
    )
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val appPrefs = LeadPedlarApp.instance.appPreferences
    val isLoggedIn by appPrefs.isLoggedInFlow.collectAsState(initial = false)
    val user by appPrefs.userFlow.collectAsState(initial = User())

    val isAdmin = (user.role == "SUPER_ADMIN" || user.role == "ADMIN")
    val availableTabs = remember(isAdmin) {
        AppTab.entries.filter { !it.adminOnly || isAdmin }
    }

    var currentTab by remember(isAdmin) {
        mutableStateOf(if (isAdmin) AppTab.ADMIN else AppTab.LEADS)
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                currentTab = if (isAdmin) AppTab.ADMIN else AppTab.LEADS
            }
        )
    } else {
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
                        availableTabs.forEach { tab ->
                            val isSelected = (currentTab == tab)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = tab.icon,
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
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
                when (currentTab) {
                    AppTab.ADMIN -> AdminOverviewScreen()
                    AppTab.LEADS -> LeadsScreen(onNavigateToSettings = { currentTab = AppTab.SETTINGS })
                    AppTab.MARKETPLACE -> MarketplaceScreen()
                    AppTab.SETTINGS -> SettingsScreen(onLogout = { currentTab = AppTab.LEADS })
                }
            }
        }
    }
}
