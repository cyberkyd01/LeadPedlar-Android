package com.example.leadpedlar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leadpedlar.calling.CallAppInfo
import com.example.leadpedlar.data.model.CallAppType
import com.example.leadpedlar.theme.Cyan500
import com.example.leadpedlar.theme.Emerald500
import com.example.leadpedlar.theme.SurfaceBorder
import com.example.leadpedlar.theme.SurfaceCard
import com.example.leadpedlar.theme.SurfaceDark
import com.example.leadpedlar.theme.TextMuted
import com.example.leadpedlar.theme.TextPrimary
import com.example.leadpedlar.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallAppSelectorBottomSheet(
    phoneNumber: String,
    leadName: String,
    availableApps: List<CallAppInfo>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAppSelected: (appType: CallAppType, alwaysUse: Boolean) -> Unit
) {
    var selectedAppType by remember {
        mutableStateOf(
            availableApps.firstOrNull { it.isInstalled && it.appType != CallAppType.SYSTEM_CHOOSER }?.appType
                ?: CallAppType.SYSTEM_DIALER
        )
    }
    var alwaysUseThisApp by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Choose Calling App",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Calling $leadName ($phoneNumber)",
                        fontSize = 13.sp,
                        color = Cyan500
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Emerald500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Selection List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableApps) { info ->
                    val isSelected = (info.appType == selectedAppType)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Emerald500 else SurfaceBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedAppType = info.appType
                            },
                        color = if (isSelected) SurfaceCard else SurfaceDark
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji Icon
                            Text(
                                text = info.appType.iconEmoji,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            // Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = info.appType.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (info.isInstalled) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Emerald500.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Installed",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald500
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = info.appType.description,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            // Selection Indicator
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Emerald500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remember choice checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard.copy(alpha = 0.6f))
                    .clickable { alwaysUseThisApp = !alwaysUseThisApp }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = alwaysUseThisApp,
                    onCheckedChange = { alwaysUseThisApp = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Emerald500,
                        uncheckedColor = TextMuted,
                        checkmarkColor = Color.Black
                    )
                )
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "Always use this app to make calls",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "You can change or reset this preference anytime in Settings",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onAppSelected(selectedAppType, alwaysUseThisApp)
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (alwaysUseThisApp) "Save & Call" else "Call Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
