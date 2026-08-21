package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientAccount
import com.example.model.LeadActivityEntity
import com.example.model.LeadEntity
import com.example.ui.components.Delivery14DayChart
import com.example.ui.components.StatCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    account: ClientAccount?,
    leads: List<LeadEntity>,
    activities: List<LeadActivityEntity>,
    onLeadClick: (String) -> Unit,
    onCreateCampaignClick: () -> Unit,
    onNavigateTab: (String) -> Unit,
    onSimulateAlert: (Int) -> Unit
) {
    val totalLeads = leads.size
    val todayStart = remember {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        cal.timeInMillis
    }
    val weekAgo = remember { System.currentTimeMillis() - 7L * 86400000L }
    val monthAgo = remember { System.currentTimeMillis() - 30L * 86400000L }

    val todayCount = leads.count { it.submittedAt >= todayStart }
    val thisWeekCount = leads.count { it.submittedAt >= weekAgo }
    val thisMonthCount = leads.count { it.submittedAt >= monthAgo }
    val avgPerDay = if (leads.isNotEmpty()) String.format(Locale.US, "%.1f", thisMonthCount / 30f) else "0.0"

    // Group leads by campaign for distribution
    val campaignCounts = remember(leads) {
        leads.groupingBy { it.campaignName }.eachCount().toList().sortedByDescending { it.second }
    }
    val maxCampCount = campaignCounts.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    var selectedActivityFilter by remember { mutableStateOf("all") }
    val filteredActivities = remember(activities, selectedActivityFilter) {
        when (selectedActivityFilter) {
            "intake" -> activities.filter { it.activityType == "lead_intake" }
            "audits" -> activities.filter { it.activityType == "recording_ready" }
            "status" -> activities.filter { it.activityType == "status_change" }
            "skiptrace" -> activities.filter { it.activityType == "skip_traced" }
            else -> activities
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card (Sleek Theme Primary Blue Hero Card)
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_sleek_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Plan",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (account?.accountType == "full_service") "Full Service Enterprise" else "Skip Trace Pro",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Lead Pipeline Volume",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "$totalLeads Leads",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Skip Trace Credits",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "${account?.skiptraceCredits ?: 2450}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Hero Dual Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCreateCampaignClick,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = SleekPrimaryDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("hero_create_campaign_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Campaign", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onNavigateTab("billing") },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSecondary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Manage Plan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Mock Push Notification Simulator Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "Mock Push Notification Handler",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Live Simulator",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = SleekPrimary
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Simulate incoming real-time alerts from callers, quality auditors, and data sync engines:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { onSimulateAlert(0) },
                                label = { Text("🎯 Inbound Lead Alert", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekPrimary) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { onSimulateAlert(1) },
                                label = { Text("🎟️ Ticket Review Alert", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekSecondary) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { onSimulateAlert(2) },
                                label = { Text("🔍 Skip Trace Ready", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekSuccessGreen) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { onSimulateAlert(3) },
                                label = { Text("🎙️ Caller Call Completed", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekInfoCyan) }
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Remi AI & Templates Quick Tile
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(PropTechCyan.copy(alpha = 0.5f), SleekPrimary.copy(alpha = 0.5f)))),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab("remi") }
                        .testTag("quick_tile_remi_ai")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PropTechCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PropTechCyan, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Remi AI & Templates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Underwriting, Scripts, MAO",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                // Order Niche Lists Quick Tile
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab("buylist") }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Order Niche Lists",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Absentee & Foreclosures",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                // Batch Skip Tracing Quick Tile
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab("skiptrace") }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SleekSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Batch Skip Trace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Phones, Emails & DNC",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // KPI Stat Cards Grid (2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Today's Intake",
                        value = todayCount.toString(),
                        icon = Icons.Default.Today,
                        accentColor = SleekPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "This Week",
                        value = thisWeekCount.toString(),
                        icon = Icons.Default.TrendingUp,
                        accentColor = SleekSuccessGreen,
                        subText = "↑ 18% vs last week",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "This Month",
                        value = thisMonthCount.toString(),
                        icon = Icons.Default.CalendarMonth,
                        accentColor = SleekInfoCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg / Day",
                        value = avgPerDay,
                        icon = Icons.Default.Speed,
                        accentColor = SleekSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Real-Time Lead Activity Feed Component
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_activity_feed_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SleekSuccessGreen)
                            )
                            Text(
                                text = "Real-Time Lead Activity Feed",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${filteredActivities.size} Events",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Activity Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "all" to "All",
                            "intake" to "Intake",
                            "audits" to "Audits",
                            "status" to "Status",
                            "skiptrace" to "Skip Trace"
                        ).forEach { (key, label) ->
                            val isSel = selectedActivityFilter == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedActivityFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (filteredActivities.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No live activities for selected filter.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        filteredActivities.take(6).forEachIndexed { idx, activity ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }

                            ActivityFeedItemRow(
                                activity = activity,
                                onClick = {
                                    if (activity.leadId.isNotEmpty()) {
                                        onLeadClick(activity.leadId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 14-Day Delivery Trend Column Chart
        item {
            Delivery14DayChart(leads = leads)
        }

        // Leads by Campaign Distribution Bars
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LEADS BY CAMPAIGN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    campaignCounts.forEach { (name, count) ->
                        val ratio = count.toFloat() / maxCampCount
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = count.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityFeedItemRow(
    activity: LeadActivityEntity,
    onClick: () -> Unit
) {
    val (badgeIcon, badgeBg, badgeTint) = when (activity.activityType) {
        "lead_intake" -> Triple(Icons.Default.PhoneCallback, SleekPrimary.copy(alpha = 0.15f), SleekPrimary)
        "recording_ready" -> Triple(Icons.Default.GraphicEq, SleekInfoCyan.copy(alpha = 0.15f), SleekInfoCyan)
        "status_change" -> Triple(Icons.Default.CheckCircle, SleekSuccessGreen.copy(alpha = 0.15f), SleekSuccessGreen)
        "skip_traced" -> Triple(Icons.Default.PersonSearch, SleekSecondary.copy(alpha = 0.15f), SleekSecondary)
        else -> Triple(Icons.Default.Notifications, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface)
    }

    val relativeTime = formatRelativeTime(activity.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(badgeIcon, contentDescription = null, tint = badgeTint, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = relativeTime,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = activity.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actor Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${activity.actorRole} · ${activity.actorName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (activity.leadAddress.isNotEmpty()) {
                    Text(
                        text = activity.leadAddress,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun formatRelativeTime(timeMs: Long): String {
    val diff = System.currentTimeMillis() - timeMs
    val minutes = diff / 60000L
    val hours = diff / 3600000L
    val days = diff / 86400000L

    return when {
        diff < 60000L -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timeMs))
    }
}
