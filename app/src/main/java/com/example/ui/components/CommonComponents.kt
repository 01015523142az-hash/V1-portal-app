package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.CallUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalTopAppBar(
    companyName: String,
    userName: String,
    unreadNotifications: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenThemeCustomizer: () -> Unit = {},
    onOpenNotifications: () -> Unit,
    onCreateCampaignClick: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // PropTech AI Brand Logo & User Profile Access
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenProfile)
                        .padding(2.dp)
                        .testTag("top_bar_user_profile_btn")
                ) {
                    // PropTech AI Logo Brand Mark
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PropTechBlue, PropTechIndigo, PropTechCyan)
                                )
                            )
                            .border(1.dp, PropTechCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_proptech_ai_symbol),
                            contentDescription = "PropTech AI Logo",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (userName.isNotBlank()) userName else companyName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // PropTech AI Core Pill
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PropTechCyan.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, PropTechCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "AI CORE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PropTechCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PropTechEmerald)
                            )
                            Text(
                                text = "PropTech AI Portal · Active",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Create Campaign Pill Button
                    Button(
                        onClick = onCreateCampaignClick,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 3.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("create_campaign_top_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(text = "Campaign", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Theme & Font Presets Customizer Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(onClick = onOpenThemeCustomizer)
                            .testTag("top_bar_theme_presets_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes & Fonts Presets",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Notifications Bell with Alert Dot
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(onClick = onOpenNotifications)
                            .testTag("top_bar_notifications_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unreadNotifications > 0) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotifications > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        if (unreadNotifications > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(SleekAlertRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotifications > 9) "9+" else unreadNotifications.toString(),
                                    color = Color.White,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Theme Toggle (Dark / Light)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(onClick = onToggleTheme)
                            .testTag("top_bar_theme_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Sign Out
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(onClick = onSignOutClick)
                            .testTag("top_bar_signout_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun PortalNavigationBar(
    selectedTab: String,
    ticketBadgeCount: Int,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            )
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.8.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    Triple("dashboard", "Home", Icons.Default.Dashboard),
                    Triple("leads", "Leads", Icons.Default.People),
                    Triple("remi", "Remi AI", Icons.Default.SmartToy),
                    Triple("tickets", "Tickets", Icons.Default.ConfirmationNumber),
                    Triple("buylist", "Buy List", Icons.Default.ReceiptLong),
                    Triple("skiptrace", "Skip Trace", Icons.Default.Search),
                    Triple("billing", "Billing", Icons.Default.AccountBalanceWallet)
                )

                items.forEach { (tabId, label, icon) ->
                    val isSelected = selectedTab == tabId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabSelected(tabId) }
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                            .testTag("nav_tab_$tabId")
                    ) {
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .widthIn(min = 38.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .padding(horizontal = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (tabId == "tickets" && ticketBadgeCount > 0) {
                                        Badge(
                                            containerColor = SleekAlertRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(ticketBadgeCount.toString(), fontSize = 8.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = SleekPrimary,
    subText: String? = null,
    subTextColor: Color = SleekSuccessGreen,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant))),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subText != null) {
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = subTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val isNew = status.equals("new", ignoreCase = true)
    val bgColor = if (isNew) MaterialTheme.colorScheme.primaryContainer else SleekSuccessGreenDim
    val textColor = if (isNew) MaterialTheme.colorScheme.onPrimaryContainer else SleekSuccessGreen

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Text(
            text = if (isNew) "NEW LEAD" else "ACCEPTED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = textColor
        )
    }
}

@Composable
fun LeadAudioPlayerWidget(
    leadId: String,
    isPlaying: Boolean,
    progress: Float,
    durationSec: Int,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Call Recording" else "Play Call Recording",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Call Audio Recording",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val currentSec = (progress * durationSec).toInt()
                    Text(
                        text = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, durationSec / 60, durationSec % 60),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
fun Delivery14DayChart(leads: List<com.example.model.LeadEntity>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "LEADS DELIVERED IN THE LAST 14 DAYS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))

            val mockDailyCounts = remember(leads) {
                listOf(2, 4, 3, 7, 5, 8, 4, 6, 9, 7, 11, 8, 12, 10)
            }
            val maxCount = mockDailyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                mockDailyCounts.forEachIndexed { index, count ->
                    val heightRatio = count.toFloat() / maxCount
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = count.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(heightRatio.coerceAtLeast(0.08f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(SleekSecondary, SleekPrimary)
                                    )
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "D${index + 1}",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceCallBar(
    callState: CallUiState,
    onMuteToggle: () -> Unit,
    onScreenShareToggle: () -> Unit,
    onMinimizeToggle: () -> Unit,
    onHangup: () -> Unit
) {
    AnimatedVisibility(
        visible = callState.isCallActive,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SleekPrimary, SleekSecondary))),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (callState.isConnected) SleekSuccessGreen else SleekWarningOrange)
                        )
                        Column {
                            Text(
                                text = callState.staffName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val sec = callState.durationSeconds
                            Text(
                                text = if (callState.isConnected) String.format("Connected · %02d:%02d", sec / 60, sec % 60) else "Calling account manager…",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onScreenShareToggle,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (callState.isScreenSharing) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenShare,
                                contentDescription = "Share Screen",
                                tint = if (callState.isScreenSharing) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onMuteToggle,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (callState.isMuted) SleekAlertRed else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mute",
                                tint = if (callState.isMuted) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onHangup,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SleekAlertRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Hang up",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (callState.isScreenSharing && !callState.isVideoMinimized) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ScreenShare, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Screen Share Active with Staff", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemiFloatingMascot(
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "remi_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "remi_y"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SleekSecondary, SleekPrimary))),
        modifier = Modifier
            .offset(y = floatOffset.dp)
            .clickable(onClick = onClick)
            .testTag("remi_mascot_fab")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Remi AI Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "Remi AI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ask me anything",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeFontCustomizerSheet(
    currentThemePreset: String,
    currentFontPreset: String,
    isDarkTheme: Boolean,
    autoBiometricEnabled: Boolean,
    onSelectThemePreset: (String) -> Unit,
    onSelectFontPreset: (String) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleAutoBiometric: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
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
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Portal Branding & Theme",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Choose colors and typography matching your portal brand",
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

            // Quick Mode & Security Toggles Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Dark Mode Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isDarkTheme) "Dark Appearance" else "Light Appearance",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Toggle dark/light theme background",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleDarkTheme() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Auto Biometric Login Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = SleekSuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Auto Biometric Unlock",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Use Fingerprint / Face ID on app launch",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = autoBiometricEnabled,
                                onCheckedChange = onToggleAutoBiometric,
                                colors = SwitchDefaults.colors(checkedThumbColor = SleekSuccessGreen)
                            )
                        }
                    }
                }
            }

            // Theme Color Presets Section
            item {
                Text(
                    text = "COLOR THEME TEMPLATES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(ThemeCatalog.ALL_THEME_PRESETS.size) { index ->
                val preset = ThemeCatalog.ALL_THEME_PRESETS[index]
                val isSelected = preset.id == currentThemePreset
                val activeScheme = if (isDarkTheme) preset.darkScheme else preset.lightScheme

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectThemePreset(preset.id) }
                        .testTag("theme_preset_card_${preset.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tri-Color Swatches Bar with accent dot badges
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeScheme.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeScheme.secondary)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeScheme.tertiary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeScheme.primaryContainer)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "3-ACCENT",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = preset.subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = preset.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Font Presets Section
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "TYPOGRAPHY & FONT ACCENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(FontCatalog.ALL_FONT_PRESETS.size) { index ->
                val fontPreset = FontCatalog.ALL_FONT_PRESETS[index]
                val isSelected = fontPreset.id == currentFontPreset

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectFontPreset(fontPreset.id) }
                        .testTag("font_preset_card_${fontPreset.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = fontPreset.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = fontPreset.subtitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Preview Box showing font styling
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "PropTech AI Deal Flow · $1,250,000",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontPreset.headingFamily,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "128 Motivated Seller Leads · 99.4% Skip Trace Match",
                                    fontSize = 11.sp,
                                    fontFamily = fontPreset.bodyFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Skeleton Loading & Shimmer Engine ---

@Composable
fun shimmerBrush(
    targetValue: Float = 1200f,
    showShimmer: Boolean = true
): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(x = translateAnimation - 350f, y = translateAnimation - 350f),
        end = androidx.compose.ui.geometry.Offset(x = translateAnimation, y = translateAnimation)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    brush: Brush = shimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun SkeletonLeadCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(modifier = Modifier.size(42.dp), shape = CircleShape, brush = brush)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SkeletonBox(modifier = Modifier.width(130.dp).height(16.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                        SkeletonBox(modifier = Modifier.width(80.dp).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                    }
                }
                SkeletonBox(modifier = Modifier.width(75.dp).height(24.dp), shape = RoundedCornerShape(12.dp), brush = brush)
            }

            Spacer(Modifier.height(14.dp))

            // Address Row
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)

            Spacer(Modifier.height(14.dp))

            // Specs Chips Row (Beds, Baths, Sqft, Price)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    SkeletonBox(modifier = Modifier.width(56.dp).height(22.dp), shape = RoundedCornerShape(6.dp), brush = brush)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Audio Player Waveform Bar Placeholder
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(modifier = Modifier.size(28.dp), shape = CircleShape, brush = brush)
                    SkeletonBox(modifier = Modifier.weight(1f).height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                    SkeletonBox(modifier = Modifier.width(40.dp).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp), brush = brush)
                SkeletonBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp), brush = brush)
                SkeletonBox(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }
        }
    }
}

@Composable
fun SkeletonTicketCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(modifier = Modifier.width(90.dp).height(22.dp), shape = RoundedCornerShape(6.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(70.dp).height(20.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }
            Spacer(Modifier.height(12.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.9f).height(18.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(10.dp), brush = brush)
                SkeletonBox(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }
        }
    }
}

@Composable
fun SkeletonSkiptraceResultCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(modifier = Modifier.size(38.dp), shape = CircleShape, brush = brush)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SkeletonBox(modifier = Modifier.width(140.dp).height(16.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                        SkeletonBox(modifier = Modifier.width(100.dp).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                    }
                }
                SkeletonBox(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(12.dp), brush = brush)
            }
            Spacer(Modifier.height(14.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(modifier = Modifier.width(110.dp).height(28.dp), shape = RoundedCornerShape(8.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(110.dp).height(28.dp), shape = RoundedCornerShape(8.dp), brush = brush)
            }
        }
    }
}

@Composable
fun SkeletonBuyListOrderCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(modifier = Modifier.width(150.dp).height(18.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(70.dp).height(22.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }
            Spacer(Modifier.height(10.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(12.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(8.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.width(80.dp).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(60.dp).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            }
        }
    }
}

@Composable
fun SkeletonSupportTicketCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(modifier = Modifier.width(140.dp).height(16.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(65.dp).height(20.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }
            Spacer(Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(modifier = Modifier.width(70.dp).height(18.dp), shape = RoundedCornerShape(6.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(90.dp).height(18.dp), shape = RoundedCornerShape(6.dp), brush = brush)
            }
        }
    }
}

@Composable
fun SkeletonTransactionCard(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(modifier = Modifier.size(36.dp), shape = CircleShape, brush = brush)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SkeletonBox(modifier = Modifier.width(100.dp).height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                    SkeletonBox(modifier = Modifier.width(70.dp).height(10.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonBox(modifier = Modifier.width(60.dp).height(14.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(45.dp).height(16.dp), shape = RoundedCornerShape(6.dp), brush = brush)
            }
        }
    }
}

