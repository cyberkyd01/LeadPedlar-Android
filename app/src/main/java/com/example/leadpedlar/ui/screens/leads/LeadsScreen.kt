package com.example.leadpedlar.ui.screens.leads

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadpedlar.LeadPedlarApp
import com.example.leadpedlar.calling.CallManager
import com.example.leadpedlar.data.api.ApiClient
import com.example.leadpedlar.data.model.CallAppType
import com.example.leadpedlar.data.model.LeadItem
import com.example.leadpedlar.theme.BgDark
import com.example.leadpedlar.theme.Cyan400
import com.example.leadpedlar.theme.Cyan500
import com.example.leadpedlar.theme.Emerald500
import com.example.leadpedlar.theme.SurfaceBorder
import com.example.leadpedlar.theme.SurfaceCard
import com.example.leadpedlar.theme.SurfaceDark
import com.example.leadpedlar.theme.TextMuted
import com.example.leadpedlar.theme.TextPrimary
import com.example.leadpedlar.theme.TextSecondary
import com.example.leadpedlar.ui.components.CallAppSelectorBottomSheet
import com.example.leadpedlar.ui.components.LeadCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dialerPrefs = LeadPedlarApp.instance.dialerPreferences

    val preferredApp by dialerPrefs.preferredAppFlow.collectAsState(initial = CallAppType.SYSTEM_CHOOSER)
    val alwaysAsk by dialerPrefs.alwaysAskFlow.collectAsState(initial = true)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val leads = remember { mutableStateListOf<LeadItem>().apply { addAll(ApiClient.sampleLeads) } }

    // Bottom sheet state for Calling App Selector
    var activeCallLead by remember { mutableStateOf<LeadItem?>(null) }
    var showCallSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredLeads = leads.filter { lead ->
        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "INTERESTED" -> lead.status == "INTERESTED"
            "CALLBACK" -> lead.status == "CALLBACK"
            "NEW" -> lead.status == "NEW"
            "CLOSED" -> lead.status == "CLOSED"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                lead.name.contains(searchQuery, ignoreCase = true) ||
                lead.phone.contains(searchQuery, ignoreCase = true) ||
                (lead.city?.contains(searchQuery, ignoreCase = true) == true)
        matchesFilter && matchesSearch
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Assigned Leads",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${leads.size} total leads • Tap call to dial",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Default Dialer Status Banner / Quick Settings
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { onNavigateToSettings() },
                    color = SurfaceCard
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (alwaysAsk) "⚙️ Ask App" else "${preferredApp.iconEmoji} ${preferredApp.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alwaysAsk) Cyan400 else Emerald500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, city, or phone...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("ALL", "NEW", "INTERESTED", "CALLBACK", "CLOSED").forEach { filter ->
                    val isSelected = (selectedFilter == filter)
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else SurfaceDark)
                                .border(
                                    1.dp,
                                    if (isSelected) Emerald500 else SurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Emerald500 else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Leads List
            if (filteredLeads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No leads matching your search criteria",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLeads, key = { it.id }) { lead ->
                        LeadCard(
                            lead = lead,
                            preferredCallApp = if (alwaysAsk) CallAppType.SYSTEM_CHOOSER else preferredApp,
                            onCallClick = { targetLead, forceShowChooser ->
                                if (forceShowChooser || alwaysAsk || preferredApp == CallAppType.SYSTEM_CHOOSER) {
                                    activeCallLead = targetLead
                                    showCallSheet = true
                                } else {
                                    // Direct Instant Call using saved preferred app
                                    CallManager.launchCall(context, preferredApp, targetLead.phone)
                                    val idx = leads.indexOfFirst { it.id == targetLead.id }
                                    if (idx >= 0) {
                                        leads[idx] = targetLead.copy(
                                            callCount = targetLead.callCount + 1,
                                            status = if (targetLead.status == "NEW") "CONTACTED" else targetLead.status
                                        )
                                    }
                                }
                            },
                            onWhatsAppClick = { targetLead ->
                                CallManager.launchCall(context, CallAppType.WHATSAPP, targetLead.phone)
                            },
                            onStatusChange = { targetLead, newStatus ->
                                val idx = leads.indexOfFirst { it.id == targetLead.id }
                                if (idx >= 0) {
                                    leads[idx] = targetLead.copy(status = newStatus)
                                }
                            },
                            onRevealClick = { targetLead ->
                                val idx = leads.indexOfFirst { it.id == targetLead.id }
                                if (idx >= 0) {
                                    leads[idx] = targetLead.copy(isRevealed = true)
                                }
                                Toast.makeText(context, "Lead phone unmasked", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Calling App Selector Bottom Sheet
        if (showCallSheet && activeCallLead != null) {
            val lead = activeCallLead!!
            val availableApps = remember(context, preferredApp) {
                CallManager.getAvailableCallApps(context, preferredApp)
            }

            CallAppSelectorBottomSheet(
                phoneNumber = lead.phone,
                leadName = lead.name,
                availableApps = availableApps,
                sheetState = sheetState,
                onDismiss = {
                    showCallSheet = false
                    activeCallLead = null
                },
                onAppSelected = { appType, alwaysUse ->
                    showCallSheet = false
                    scope.launch {
                        dialerPrefs.saveDefaultApp(appType, alwaysUse)
                        CallManager.launchCall(context, appType, lead.phone)

                        // Update lead state
                        val idx = leads.indexOfFirst { it.id == lead.id }
                        if (idx >= 0) {
                            leads[idx] = lead.copy(
                                callCount = lead.callCount + 1,
                                status = if (lead.status == "NEW") "CONTACTED" else lead.status
                            )
                        }
                    }
                }
            )
        }
    }
}
