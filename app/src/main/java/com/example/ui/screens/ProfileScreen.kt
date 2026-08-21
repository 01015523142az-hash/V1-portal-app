package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.QuietHoursSettings
import com.example.model.ClientAccount
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    account: ClientAccount?,
    isDarkTheme: Boolean,
    quietHoursSettings: QuietHoursSettings = QuietHoursSettings(),
    onToggleTheme: () -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onUpdateNotificationPreferences: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateQuietHours: (QuietHoursSettings) -> Unit = {},
    onUpdateBiometric: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Notification states initialized from account
    var notifyLeads by remember(account) { mutableStateOf(account?.notifyLeads ?: true) }
    var notifyTickets by remember(account) { mutableStateOf(account?.notifyTickets ?: true) }
    var notifyBilling by remember(account) { mutableStateOf(account?.notifyBilling ?: true) }
    var notifyOrders by remember(account) { mutableStateOf(account?.notifyOrders ?: true) }
    var notifySms by remember(account) { mutableStateOf(account?.notifySms ?: false) }
    var biometricEnabled by remember(account) { mutableStateOf(account?.biometricEnabled ?: true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header with Close Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Account Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Manage your portal credentials & preferences",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // User Identity Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CoralPrimary, GoldAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = (account?.fullName ?: "Alex Morgan")
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                            Text(
                                text = initials.ifBlank { "AM" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = account?.fullName ?: "Alex Morgan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = account?.email ?: "01015523142az@gmail.com",
                            fontSize = 13.sp,
                            color = CoralPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (account?.accountType == "skiptrace") "Skip Trace Client" else "Full-Service Dedicated Tier",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekSuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SleekSuccessGreen)
                                    )
                                    Text(
                                        text = "Active Account",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekSuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Account Details Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMPANY & CONTACT INFO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { showEditProfileDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = CoralPrimary)
                                Spacer(Modifier.width(4.dp))
                                Text("Edit Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        ProfileDetailRow(icon = Icons.Outlined.Business, label = "Company Name", value = account?.companyName ?: "Apex Real Estate Partners")
                        ProfileDetailRow(icon = Icons.Outlined.Email, label = "Login Email", value = account?.email ?: "01015523142az@gmail.com")
                        ProfileDetailRow(icon = Icons.Outlined.Phone, label = "Phone Number", value = account?.phone ?: "+1 555-234-8921")
                        ProfileDetailRow(
                            icon = Icons.Outlined.MonetizationOn,
                            label = "Skip Trace Credits",
                            value = "${account?.skiptraceCredits ?: 2450} Credits ($${String.format("%.2f", (account?.creditBalanceCents ?: 7350) / 100f)})"
                        )
                        ProfileDetailRow(
                            icon = Icons.Outlined.AccessTime,
                            label = "Last Portal Session",
                            value = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(account?.lastLoginAt ?: System.currentTimeMillis()))
                        )
                    }
                }
            }

            // Notification Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "NOTIFICATION PREFERENCES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        NotificationToggleRow(
                            title = "Real-Time Lead Alerts",
                            subtitle = "Immediate push notifications when cold callers qualify motivated sellers",
                            checked = notifyLeads,
                            onCheckedChange = {
                                notifyLeads = it
                                onUpdateNotificationPreferences(notifyLeads, notifyTickets, notifyBilling, notifyOrders, notifySms)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        NotificationToggleRow(
                            title = "Deal Review Tickets",
                            subtitle = "Alerts for new incoming seller review tickets waiting for your review",
                            checked = notifyTickets,
                            onCheckedChange = {
                                notifyTickets = it
                                onUpdateNotificationPreferences(notifyLeads, notifyTickets, notifyBilling, notifyOrders, notifySms)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        NotificationToggleRow(
                            title = "Billing & Receipts",
                            subtitle = "Monthly campaign invoices, skip trace credit charges, and payment confirmations",
                            checked = notifyBilling,
                            onCheckedChange = {
                                notifyBilling = it
                                onUpdateNotificationPreferences(notifyLeads, notifyTickets, notifyBilling, notifyOrders, notifySms)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        NotificationToggleRow(
                            title = "Data & Skip Trace Orders",
                            subtitle = "Alert when niche lists and batch skip trace matches are ready for download",
                            checked = notifyOrders,
                            onCheckedChange = {
                                notifyOrders = it
                                onUpdateNotificationPreferences(notifyLeads, notifyTickets, notifyBilling, notifyOrders, notifySms)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        NotificationToggleRow(
                            title = "Urgent SMS Dispatch",
                            subtitle = "Deliver high-intent hot deals directly to your mobile phone via SMS",
                            checked = notifySms,
                            onCheckedChange = {
                                notifySms = it
                                onUpdateNotificationPreferences(notifyLeads, notifyTickets, notifyBilling, notifyOrders, notifySms)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // --- Quiet Hours Configuration Section ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (quietHoursSettings.isEnabled) CoralPrimaryDim else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Icon(
                                        Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = if (quietHoursSettings.isEnabled) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(6.dp).size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Quiet Hours",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Mute push notification alerts during user-defined time slots",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = quietHoursSettings.isEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateQuietHours(quietHoursSettings.copy(isEnabled = enabled))
                                },
                                modifier = Modifier.testTag("quiet_hours_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CoralPrimary
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = quietHoursSettings.isEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                // Live Active Status Pill
                                val isCurrentlyActive = quietHoursSettings.isCurrentlyInQuietHours()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isCurrentlyActive) SleekSuccessGreen else MaterialTheme.colorScheme.outline)
                                        )
                                        Text(
                                            text = if (isCurrentlyActive) "ACTIVE RIGHT NOW" else "SCHEDULED DAILY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isCurrentlyActive) SleekSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrentlyActive) SleekSuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (isCurrentlyActive) "🔕 Muted" else "🔔 Sound On",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentlyActive) SleekSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = if (isCurrentlyActive)
                                        "All incoming notifications will be delivered silently without sounds or vibration until ${QuietHoursSettings.formatHourMinute(quietHoursSettings.endHour, quietHoursSettings.endMinute)}."
                                    else
                                        "Push notifications will automatically silence between ${quietHoursSettings.formattedTimeRange()}.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )

                                Spacer(Modifier.height(10.dp))

                                // Time Slot Selector Card
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showTimePickerDialog = true }
                                        .testTag("quiet_hours_time_selector")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "ACTIVE TIME WINDOW",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = quietHoursSettings.formattedTimeRange(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CoralPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Change",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CoralPrimary
                                            )
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Quiet Hours",
                                                tint = CoralPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Quick Presets
                                Text(
                                    text = "QUICK PRESETS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 10 PM - 7 AM
                                    val isPreset1 = quietHoursSettings.startHour == 22 && quietHoursSettings.endHour == 7
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPreset1) CoralPrimary else MaterialTheme.colorScheme.surface,
                                        border = if (!isPreset1) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onUpdateQuietHours(quietHoursSettings.copy(startHour = 22, startMinute = 0, endHour = 7, endMinute = 0))
                                            }
                                            .testTag("quiet_preset_standard")
                                    ) {
                                        Text(
                                            text = "10 PM – 7 AM",
                                            fontSize = 10.sp,
                                            fontWeight = if (isPreset1) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPreset1) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }

                                    // 9 PM - 8 AM
                                    val isPreset2 = quietHoursSettings.startHour == 21 && quietHoursSettings.endHour == 8
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPreset2) CoralPrimary else MaterialTheme.colorScheme.surface,
                                        border = if (!isPreset2) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onUpdateQuietHours(quietHoursSettings.copy(startHour = 21, startMinute = 0, endHour = 8, endMinute = 0))
                                            }
                                            .testTag("quiet_preset_extended")
                                    ) {
                                        Text(
                                            text = "9 PM – 8 AM",
                                            fontSize = 10.sp,
                                            fontWeight = if (isPreset2) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPreset2) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }

                                    // 11 PM - 6 AM
                                    val isPreset3 = quietHoursSettings.startHour == 23 && quietHoursSettings.endHour == 6
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPreset3) CoralPrimary else MaterialTheme.colorScheme.surface,
                                        border = if (!isPreset3) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onUpdateQuietHours(quietHoursSettings.copy(startHour = 23, startMinute = 0, endHour = 6, endMinute = 0))
                                            }
                                            .testTag("quiet_preset_night")
                                    ) {
                                        Text(
                                            text = "11 PM – 6 AM",
                                            fontSize = 10.sp,
                                            fontWeight = if (isPreset3) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPreset3) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Security & App Preferences Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "SECURITY & STORAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        NotificationToggleRow(
                            title = "Biometric Authentication",
                            subtitle = "Unlock portal with Fingerprint / Face ID on app launch",
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                onUpdateBiometric(it)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        NotificationToggleRow(
                            title = "Global Dark Mode",
                            subtitle = "Persist theme preference with Preferences DataStore",
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleTheme() },
                            testTag = "dark_mode_toggle_switch"
                        )

                        Spacer(Modifier.height(12.dp))

                        // Offline Room Database Status Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "Room Local Database Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Leads, tickets, and billing are cached locally for full offline access.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sign Out Button
            item {
                Button(
                    onClick = { showSignOutConfirmation = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_sign_out_btn")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out of Account", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Edit Profile Information Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = account?.fullName ?: "Alex Morgan",
            currentCompany = account?.companyName ?: "Apex Real Estate Partners",
            currentPhone = account?.phone ?: "+1 555-234-8921",
            onSave = { name, company, phone ->
                onUpdateProfile(name, company, phone)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // Quiet Hours Custom Time Picker Dialog
    if (showTimePickerDialog) {
        QuietHoursTimePickerDialog(
            currentStartHour = quietHoursSettings.startHour,
            currentStartMinute = quietHoursSettings.startMinute,
            currentEndHour = quietHoursSettings.endHour,
            currentEndMinute = quietHoursSettings.endMinute,
            onSave = { startH, startM, endH, endM ->
                onUpdateQuietHours(
                    quietHoursSettings.copy(
                        startHour = startH,
                        startMinute = startM,
                        endHour = endH,
                        endMinute = endM
                    )
                )
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of the client portal? You will need to log in again to view new leads and campaign alerts.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmation = false
                        onDismiss()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = CoralPrimary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CoralPrimary
            )
        )
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentCompany: String,
    currentPhone: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var company by remember { mutableStateOf(currentCompany) }
    var phone by remember { mutableStateOf(currentPhone) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Account Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), company.trim(), phone.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuietHoursTimePickerDialog(
    currentStartHour: Int,
    currentStartMinute: Int,
    currentEndHour: Int,
    currentEndMinute: Int,
    onSave: (Int, Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var startHour by remember { mutableIntStateOf(currentStartHour) }
    var startMinute by remember { mutableIntStateOf(currentStartMinute) }
    var endHour by remember { mutableIntStateOf(currentEndHour) }
    var endMinute by remember { mutableIntStateOf(currentEndMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Set Quiet Hours Window",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Select when notifications should be muted and delivered silently without sound or vibrations.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Start Time Selector
                TimeSelectorSection(
                    label = "START TIME (SILENCE FROM)",
                    hour = startHour,
                    minute = startMinute,
                    onHourChange = { startHour = it },
                    onMinuteChange = { startMinute = it }
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(14.dp))

                // End Time Selector
                TimeSelectorSection(
                    label = "END TIME (RESUME SOUND AT)",
                    hour = endHour,
                    minute = endMinute,
                    onHourChange = { endHour = it },
                    onMinuteChange = { endMinute = it }
                )

                Spacer(Modifier.height(16.dp))

                // Summary Preview Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralPrimaryDim,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🌙 Schedule: ${QuietHoursSettings.formatHourMinute(startHour, startMinute)} → ${QuietHoursSettings.formatHourMinute(endHour, endMinute)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(startHour, startMinute, endHour, endMinute)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quiet_hours_save_btn")
                    ) {
                        Text("Save Window", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSelectorSection(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val isPm = hour >= 12
    val display12Hour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val newHour = if (hour == 0) 23 else hour - 1
                        onHourChange(newHour)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Hour", tint = CoralPrimary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(42.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", display12Hour),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        val newHour = if (hour == 23) 0 else hour + 1
                        onHourChange(newHour)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Hour", tint = CoralPrimary)
                }
            }

            Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Minute controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val newMinute = if (minute == 0) 45 else (minute - 15 + 60) % 60
                        onMinuteChange(newMinute)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Minute", tint = CoralPrimary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(42.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", minute),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        val newMinute = (minute + 15) % 60
                        onMinuteChange(newMinute)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Minute", tint = CoralPrimary)
                }
            }

            // AM / PM Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isPm) CoralPrimary else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isPm) onHourChange(hour - 12)
                        }
                ) {
                    Text(
                        text = "AM",
                        fontSize = 11.sp,
                        fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isPm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPm) CoralPrimary else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!isPm) onHourChange(hour + 12)
                        }
                ) {
                    Text(
                        text = "PM",
                        fontSize = 11.sp,
                        fontWeight = if (isPm) FontWeight.Bold else FontWeight.Normal,
                        color = if (isPm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
