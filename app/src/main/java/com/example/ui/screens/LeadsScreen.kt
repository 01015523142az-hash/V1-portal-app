package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.LeadAudioPlayerWidget
import com.example.ui.components.LeadsMapView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    leads: List<LeadEntity>,
    activities: List<LeadActivityEntity> = emptyList(),
    campaignGuides: List<CampaignGuideItem>,
    disputes: List<DisputeThreadEntity>,
    selectedSubtab: String,
    onSubtabSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedCampaign: String,
    onCampaignFilterChanged: (String) -> Unit,
    selectedStatus: String,
    onStatusFilterChanged: (String) -> Unit,
    leadSortOption: String = "date_added",
    onLeadSortOptionChanged: (String) -> Unit = {},
    leadSortAscending: Boolean = false,
    onToggleSortDirection: () -> Unit = {},
    recentlyQualifiedLeadId: String? = null,
    leadStatusFeedbackMessage: String? = null,
    onUpdateLeadStatus: (String, String) -> Unit = { _, _ -> },
    onOpenVoiceNoteModal: (String) -> Unit = {},
    selectedLeadDetailId: String?,
    onOpenLeadDetail: (String) -> Unit,
    onCloseLeadDetail: () -> Unit,
    activePlayingLeadId: String?,
    isAudioPlaying: Boolean,
    audioProgress: Float,
    audioDurationSec: Int,
    onToggleAudio: (String, Int) -> Unit,
    onSeekAudio: (Float) -> Unit,
    isTranscriptVisible: Boolean,
    onToggleTranscript: () -> Unit,
    onOpenDispute: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onProposeGuideEdit: (Long, String) -> Unit,
    onSendDisputeReply: (String, String) -> Unit,
    isMapView: Boolean = false,
    onToggleMapView: (Boolean) -> Unit = {},
    onLogCallMade: (String) -> Unit = {},
    isOnline: Boolean = true,
    lastSyncTimestamp: Long = System.currentTimeMillis(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    syncStatusMessage: String? = null
) {
    if (selectedLeadDetailId != null) {
        val detailLead = leads.find { it.id == selectedLeadDetailId }
        if (detailLead != null) {
            val leadActivities = activities.filter { it.leadId == detailLead.id }
            LeadDetailScreen(
                lead = detailLead,
                leadActivities = leadActivities,
                onBack = onCloseLeadDetail,
                activePlayingLeadId = activePlayingLeadId,
                isAudioPlaying = isAudioPlaying,
                audioProgress = audioProgress,
                audioDurationSec = audioDurationSec,
                onToggleAudio = onToggleAudio,
                onSeekAudio = onSeekAudio,
                isTranscriptVisible = isTranscriptVisible,
                onToggleTranscript = onToggleTranscript,
                onOpenDispute = onOpenDispute,
                onOpenFeedback = onOpenFeedback,
                onUpdateStatus = onUpdateLeadStatus,
                onOpenVoiceNoteModal = onOpenVoiceNoteModal,
                onLogCallMade = onLogCallMade
            )
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        // Subtabs: My Leads | Lead Analytics | Campaign Guide | Comments & Disputes
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subtabs = listOf(
                Pair("my_leads", "My Leads"),
                Pair("lead_analytics", "Lead Analytics"),
                Pair("campaign_guide", "Campaign Guide"),
                Pair("leads_comments", "Comments & Disputes")
            )
            items(subtabs) { (id, label) ->
                val isSelected = selectedSubtab == id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CoralPrimary else Color.Transparent,
                    modifier = Modifier
                        .clickable { onSubtabSelected(id) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when (selectedSubtab) {
            "my_leads" -> {
                MyLeadsContent(
                    leads = leads,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged,
                    selectedCampaign = selectedCampaign,
                    onCampaignFilterChanged = onCampaignFilterChanged,
                    selectedStatus = selectedStatus,
                    onStatusFilterChanged = onStatusFilterChanged,
                    leadSortOption = leadSortOption,
                    onLeadSortOptionChanged = onLeadSortOptionChanged,
                    leadSortAscending = leadSortAscending,
                    onToggleSortDirection = onToggleSortDirection,
                    recentlyQualifiedLeadId = recentlyQualifiedLeadId,
                    leadStatusFeedbackMessage = leadStatusFeedbackMessage,
                    onUpdateLeadStatus = onUpdateLeadStatus,
                    onOpenVoiceNoteModal = onOpenVoiceNoteModal,
                    onLeadClick = onOpenLeadDetail,
                    activePlayingLeadId = activePlayingLeadId,
                    isAudioPlaying = isAudioPlaying,
                    audioProgress = audioProgress,
                    audioDurationSec = audioDurationSec,
                    onToggleAudio = onToggleAudio,
                    onSeekAudio = onSeekAudio,
                    onOpenDispute = onOpenDispute,
                    onOpenFeedback = onOpenFeedback,
                    isMapView = isMapView,
                    onToggleMapView = onToggleMapView,
                    onLogCallMade = onLogCallMade,
                    isOnline = isOnline,
                    lastSyncTimestamp = lastSyncTimestamp,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    syncStatusMessage = syncStatusMessage
                )
            }
            "lead_analytics" -> {
                LeadAnalyticsScreen(leads = leads)
            }
            "campaign_guide" -> {
                CampaignGuideContent(
                    guides = campaignGuides,
                    onProposeEdit = onProposeGuideEdit
                )
            }
            "leads_comments" -> {
                LeadsCommentsContent(
                    disputes = disputes,
                    onReply = onSendDisputeReply
                )
            }
        }
    }
}

@Composable
fun MyLeadsContent(
    leads: List<LeadEntity>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedCampaign: String,
    onCampaignFilterChanged: (String) -> Unit,
    selectedStatus: String,
    onStatusFilterChanged: (String) -> Unit,
    leadSortOption: String,
    onLeadSortOptionChanged: (String) -> Unit,
    leadSortAscending: Boolean,
    onToggleSortDirection: () -> Unit,
    recentlyQualifiedLeadId: String?,
    leadStatusFeedbackMessage: String?,
    onUpdateLeadStatus: (String, String) -> Unit,
    onOpenVoiceNoteModal: (String) -> Unit,
    onLeadClick: (String) -> Unit,
    activePlayingLeadId: String?,
    isAudioPlaying: Boolean,
    audioProgress: Float,
    audioDurationSec: Int,
    onToggleAudio: (String, Int) -> Unit,
    onSeekAudio: (Float) -> Unit,
    onOpenDispute: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    isMapView: Boolean = false,
    onToggleMapView: (Boolean) -> Unit = {},
    onLogCallMade: (String) -> Unit = {},
    isOnline: Boolean = true,
    lastSyncTimestamp: Long = System.currentTimeMillis(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    syncStatusMessage: String? = null
) {
    var isSortMenuOpen by remember { mutableStateOf(false) }

    val filteredAndSortedLeads = remember(
        leads,
        searchQuery,
        selectedCampaign,
        selectedStatus,
        leadSortOption,
        leadSortAscending
    ) {
        val filtered = leads.filter { lead ->
            val matchSearch = searchQuery.isBlank() ||
                    lead.sellerName.contains(searchQuery, ignoreCase = true) ||
                    lead.sellerPhone.contains(searchQuery, ignoreCase = true) ||
                    lead.propertyAddress.contains(searchQuery, ignoreCase = true)

            val matchCampaign = selectedCampaign.isBlank() || selectedCampaign == "All Campaigns" ||
                    lead.campaignName.equals(selectedCampaign, ignoreCase = true)

            val matchStatus = selectedStatus == "all" || lead.status.equals(selectedStatus, ignoreCase = true)

            matchSearch && matchCampaign && matchStatus
        }

        when (leadSortOption) {
            "alphabetical" -> {
                if (leadSortAscending) filtered.sortedBy { it.sellerName.lowercase() }
                else filtered.sortedByDescending { it.sellerName.lowercase() }
            }
            "last_interaction" -> {
                if (leadSortAscending) filtered.sortedBy { it.reviewedAt ?: it.submittedAt }
                else filtered.sortedByDescending { it.reviewedAt ?: it.submittedAt }
            }
            else -> { // "date_added"
                if (leadSortAscending) filtered.sortedBy { it.submittedAt }
                else filtered.sortedByDescending { it.submittedAt }
            }
        }
    }

    if (isMapView) {
        var selectedMapLeadId by remember { mutableStateOf<String?>(null) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // View Mode Selector Header
            LeadsViewModeToggleRow(
                isMapView = isMapView,
                onToggleMapView = onToggleMapView,
                totalCount = leads.size
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search seller, phone, or address…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leads_search_input")
            )

            // Google Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                LeadsMapView(
                    leads = filteredAndSortedLeads,
                    selectedLeadId = selectedMapLeadId,
                    onLeadSelected = { selectedMapLeadId = it },
                    onOpenLeadDetail = onLeadClick,
                    onOpenVoiceNote = onOpenVoiceNoteModal,
                    onLogCallMade = onLogCallMade,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // View Mode Selector Header
            item {
                LeadsViewModeToggleRow(
                    isMapView = isMapView,
                    onToggleMapView = onToggleMapView,
                    totalCount = leads.size,
                    isOnline = isOnline,
                    lastSyncTimestamp = lastSyncTimestamp,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }

            // Offline Mode Notice Banner
            if (!isOnline) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralPrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("leads_offline_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline Mode (Local Cache Active)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPrimary
                                )
                                Text(
                                    text = "Last synced ${formatRelativeSyncTime(lastSyncTimestamp)}. Leads, calls, and voice notes will sync automatically once reconnected.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (syncStatusMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth().testTag("leads_sync_message_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = syncStatusMessage,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Status Update Celebration Toast Banner
            if (leadStatusFeedbackMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SleekSuccessGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekSuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(20.dp))
                            Text(
                                text = leadStatusFeedbackMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekSuccessGreen
                            )
                        }
                    }
                }
            }

            // Info Note Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Disputes must be filed within 2 US business days of delivery. Record voice memos or adjust lead status with instant AI notes.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Toolbar: Search input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search seller, phone, or address…", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leads_search_input")
                )
            }

            // Header Sort Toggle & Status Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sort Selector Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sort, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "SORT LEADS BY:",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onToggleSortDirection,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("sort_direction_toggle_btn")
                            ) {
                                Icon(
                                    if (leadSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Toggle Ascending/Descending",
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Sort Option Segmented Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sortOptions = listOf(
                            Triple("date_added", "Date Added", Icons.Default.CalendarToday),
                            Triple("last_interaction", "Last Interaction", Icons.Default.History),
                            Triple("alphabetical", "Alphabetical (A-Z)", Icons.Default.SortByAlpha)
                        )

                        sortOptions.forEach { (key, label, icon) ->
                            val isSelected = leadSortOption == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CoralPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onLeadSortOptionChanged(key) }
                                    .testTag("sort_opt_$key")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Status filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusList = listOf(
                            "all" to "All Leads",
                            "new" to "New",
                            "qualified" to "Qualified",
                            "accepted" to "Accepted"
                        )
                        items(statusList) { (key, label) ->
                            val isSelected = selectedStatus == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onStatusFilterChanged(key) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralPrimaryDim,
                                    selectedLabelColor = CoralPrimary
                                )
                            )
                        }
                    }
                }
            }

            if (filteredAndSortedLeads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No leads match your active filters or search.",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredAndSortedLeads, key = { it.id }) { lead ->
                    val isRecentlyPromoted = recentlyQualifiedLeadId == lead.id
                    LeadCard(
                        lead = lead,
                        isRecentlyPromoted = isRecentlyPromoted,
                        onClick = { onLeadClick(lead.id) },
                        onUpdateStatus = { newStatus -> onUpdateLeadStatus(lead.id, newStatus) },
                        onRecordVoiceNote = { onOpenVoiceNoteModal(lead.id) },
                        onLogCallMade = onLogCallMade,
                        activePlayingLeadId = activePlayingLeadId,
                        isAudioPlaying = isAudioPlaying,
                        audioProgress = audioProgress,
                        audioDurationSec = audioDurationSec,
                        onToggleAudio = onToggleAudio,
                        onSeekAudio = onSeekAudio,
                        onOpenDispute = onOpenDispute,
                        onOpenFeedback = onOpenFeedback
                    )
                }
            }
        }

        // Floating Action Button for Quick Lead Sorting / Ordering Toggle
        FloatingActionButton(
            onClick = {
                val nextSort = when (leadSortOption) {
                    "date_added" -> "last_interaction"
                    "last_interaction" -> "alphabetical"
                    else -> "date_added"
                }
                onLeadSortOptionChanged(nextSort)
            },
            containerColor = CoralPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 4.dp)
                .testTag("lead_sort_fab")
        ) {
            Icon(Icons.Default.SwapVert, contentDescription = "Instant Sort Leads", modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun LeadCard(
    lead: LeadEntity,
    isRecentlyPromoted: Boolean = false,
    onClick: () -> Unit,
    onUpdateStatus: (String) -> Unit = {},
    onRecordVoiceNote: () -> Unit = {},
    onLogCallMade: (String) -> Unit = {},
    activePlayingLeadId: String?,
    isAudioPlaying: Boolean,
    audioProgress: Float,
    audioDurationSec: Int,
    onToggleAudio: (String, Int) -> Unit,
    onSeekAudio: (Float) -> Unit,
    onOpenDispute: (String) -> Unit,
    onOpenFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val isPlayingThis = activePlayingLeadId == lead.id && isAudioPlaying

    // Animated glow border effect when lead is promoted to Qualified
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val animatedGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    var isStatusMenuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isRecentlyPromoted) {
            androidx.compose.foundation.BorderStroke(
                width = 2.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        SleekSuccessGreen.copy(alpha = animatedGlowAlpha),
                        GoldAccent.copy(alpha = animatedGlowAlpha)
                    )
                )
            )
        } else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lead_card_${lead.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Promoted celebration badge banner
            if (isRecentlyPromoted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SleekSuccessGreen.copy(alpha = 0.18f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = SleekSuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "🎉 Promoted to Qualified Deal!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekSuccessGreen
                        )
                    }
                }
            }

            // Header: Avatar, Name, Campaign, Status Pill with Animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isRecentlyPromoted) SleekSuccessGreen.copy(alpha = 0.2f) else CoralPrimaryDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lead.sellerName.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isRecentlyPromoted) SleekSuccessGreen else CoralPrimary
                        )
                    }
                    Column {
                        Text(
                            text = lead.sellerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lead.campaignName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive Status Pill with Dropdown Menu
                Box {
                    AnimatedContent(
                        targetState = lead.status,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f) togetherWith
                                    fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.1f)
                        },
                        label = "statusPillTransition"
                    ) { currentStatus ->
                        Box(modifier = Modifier.clickable { isStatusMenuOpen = true }) {
                            StatusPill(currentStatus)
                        }
                    }

                    DropdownMenu(
                        expanded = isStatusMenuOpen,
                        onDismissRequest = { isStatusMenuOpen = false }
                    ) {
                        listOf("new" to "Mark as New", "qualified" to "Promote to Qualified ✨", "accepted" to "Accept Deal", "archived" to "Archive").forEach { (statusKey, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = if (statusKey == "qualified") FontWeight.Bold else FontWeight.Normal,
                                        color = if (statusKey == "qualified") SleekSuccessGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onUpdateStatus(statusKey)
                                    isStatusMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Property & Contact Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.sellerPhone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = lead.sellerPhone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    Text(
                        text = "${lead.propertyCity}, ${lead.propertyState}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Property Address & Asking Price
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Property Address", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lead.propertyAddress, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Asking Price", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lead.askingPrice, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
                    }
                }
            }

            // AI Second Opinion Chip
            if (lead.aiSummary != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VioletAccentDim,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✨ AI Summary:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                        Text(lead.aiSummary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Audio Player
            if (lead.hasRecording) {
                Spacer(Modifier.height(10.dp))
                LeadAudioPlayerWidget(
                    leadId = lead.id,
                    isPlaying = isPlayingThis,
                    progress = if (activePlayingLeadId == lead.id) audioProgress else 0f,
                    durationSec = lead.audioDurationSec,
                    onTogglePlay = { onToggleAudio(lead.id, lead.audioDurationSec) },
                    onSeek = onSeekAudio
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons Footer: View Details, Voice Memo Button, Dispute, Feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("View Full Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Click-to-Call Primary Action Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.sellerPhone}"))
                            context.startActivity(intent)
                            onLogCallMade(lead.id)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("lead_call_button_${lead.id}")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call Seller", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Voice Note Mic Button
                    IconButton(
                        onClick = onRecordVoiceNote,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CoralPrimaryDim)
                            .testTag("voice_note_mic_btn_${lead.id}")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = CoralPrimary, modifier = Modifier.size(16.dp))
                    }

                    if (lead.disputeStatus == "open") {
                        Surface(shape = RoundedCornerShape(20.dp), color = WarningOrangeDim) {
                            Text("Dispute Open", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningOrange, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onOpenDispute(lead.id) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Dispute", fontSize = 11.sp, color = CoralPrimary)
                        }
                    }

                    OutlinedButton(
                        onClick = { onOpenFeedback(lead.id) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Feedback", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun LeadDetailScreen(
    lead: LeadEntity,
    leadActivities: List<LeadActivityEntity> = emptyList(),
    onBack: () -> Unit,
    activePlayingLeadId: String?,
    isAudioPlaying: Boolean,
    audioProgress: Float,
    audioDurationSec: Int,
    onToggleAudio: (String, Int) -> Unit,
    onSeekAudio: (Float) -> Unit,
    isTranscriptVisible: Boolean,
    onToggleTranscript: () -> Unit,
    onOpenDispute: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onUpdateStatus: (String, String) -> Unit = { _, _ -> },
    onOpenVoiceNoteModal: (String) -> Unit = {},
    onLogCallMade: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isPlayingThis = activePlayingLeadId == lead.id && isAudioPlaying
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back Navigation Button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Leads", tint = CoralPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back to Leads", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
            }
        }

        // Hero Lead Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(CoralPrimaryDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lead.sellerName.take(2).uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = CoralPrimary
                                )
                            }
                            Column {
                                Text(lead.sellerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(lead.campaignName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusPill(lead.status)
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.sellerPhone}"))
                                context.startActivity(intent)
                                onLogCallMade(lead.id)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lead_detail_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Call (${lead.sellerPhone})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOpenVoiceNoteModal(lead.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Record Note", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Status Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!lead.status.equals("qualified", ignoreCase = true)) {
                            Button(
                                onClick = { onUpdateStatus(lead.id, "qualified") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekSuccessGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mark Qualified ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!lead.status.equals("accepted", ignoreCase = true)) {
                            Button(
                                onClick = { onUpdateStatus(lead.id, "accepted") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept Deal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF241E14))
                            }
                        }
                    }
                }
            }
        }

        // Property Fact Grid
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DEAL SPECIFICATIONS & PROPERTY FACTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = GoldAccent
                    )
                    Spacer(Modifier.height(14.dp))

                    val facts = listOf(
                        Pair("Property Address", "${lead.propertyAddress}, ${lead.propertyCity}, ${lead.propertyState} ${lead.propertyZip}"),
                        Pair("Asking Price", lead.askingPrice),
                        Pair("Estimated Market Value", lead.marketValue),
                        Pair("Valuation Source", lead.marketValueSource),
                        Pair("Motivated Reason to Sell", lead.whySell),
                        Pair("Seller's Words", lead.reasonSell),
                        Pair("Closing Timeline", lead.whenSell),
                        Pair("Call Notes", lead.notes.ifBlank { "Verified clear title with single homeowner authority." })
                    )

                    facts.forEach { (label, value) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(label.uppercase(), fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Call Audio Recording & Interactive Transcript Player
        if (lead.hasRecording) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CALL RECORDING & AI TRANSCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        LeadAudioPlayerWidget(
                            leadId = lead.id,
                            isPlaying = isPlayingThis,
                            progress = if (activePlayingLeadId == lead.id) audioProgress else 0f,
                            durationSec = lead.audioDurationSec,
                            onTogglePlay = { onToggleAudio(lead.id, lead.audioDurationSec) },
                            onSeek = onSeekAudio
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onToggleTranscript,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isTranscriptVisible) "Hide Call Transcript" else "View AI Call Transcript", fontSize = 12.sp)
                        }

                        if (isTranscriptVisible) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "[Caller]: Hello ${lead.sellerName}, calling regarding your property at ${lead.propertyAddress}.\n\n[Seller]: Yes, I'm open to selling if we can close cash without realtor commissions.\n\n[Caller]: Excellent. What asking price did you have in mind?\n\n[Seller]: ${lead.askingPrice}. Looking to close within 30-45 days.",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lead Real-time Activity Feed & Attached Voice Notes
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                            text = "ACTIVITY FEED & VOICE NOTES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            color = CoralPrimary
                        )
                        IconButton(
                            onClick = { onOpenVoiceNoteModal(lead.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Voice Note", tint = CoralPrimary)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (leadActivities.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Lead Delivered via Cold Call Campaign",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Submitted at ${dateFormat.format(Date(lead.submittedAt))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Tap 'Record Note' above to attach your first audio memo and Gemini transcription.",
                                    fontSize = 11.sp,
                                    color = CoralPrimary
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            leadActivities.forEach { act ->
                                val isCallActivity = act.activityType.contains("call")
                                val isVoiceNote = act.activityType == "voice_note"

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isCallActivity -> SleekSuccessGreen.copy(alpha = 0.12f)
                                        isVoiceNote -> CoralPrimaryDim
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = when {
                                        isCallActivity -> androidx.compose.foundation.BorderStroke(1.dp, SleekSuccessGreen.copy(alpha = 0.4f))
                                        isVoiceNote -> androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.5f))
                                        else -> null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    when {
                                                        isCallActivity -> Icons.Default.PhoneCallback
                                                        isVoiceNote -> Icons.Default.Mic
                                                        else -> Icons.Default.Circle
                                                    },
                                                    contentDescription = null,
                                                    tint = when {
                                                        isCallActivity -> SleekSuccessGreen
                                                        isVoiceNote -> CoralPrimary
                                                        else -> GoldAccent
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = act.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = dateFormat.format(Date(act.timestamp)),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = act.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (isCallActivity) {
                                            Spacer(Modifier.height(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = SleekSuccessGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "📞 Outgoing Native Call Logged",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SleekSuccessGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (act.audioDurationSec != null) {
                                            Spacer(Modifier.height(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = CoralPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "🎙️ Voice Memo (${act.audioDurationSec}s) · AI Transcribed",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CoralPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

        // Dispute / Feedback Action Box
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LEAD QUALITY & ACCOUNTABILITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = CoralPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "If this lead does not meet qualification standards, dispute it within 2 business days. General notes can be left via feedback.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenDispute(lead.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dispute Lead", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onOpenFeedback(lead.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Leave Feedback", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignGuideContent(
    guides: List<CampaignGuideItem>,
    onProposeEdit: (Long, String) -> Unit
) {
    var editingItemId by remember { mutableStateOf<Long?>(null) }
    var editProposedText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Campaign Qualification Criteria",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Review what parameters our cold callers follow for each campaign. You can propose adjustments directly.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        items(guides, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.campaignDisplayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CoralPrimary
                        )
                        if (item.isPendingApproval) {
                            Surface(shape = RoundedCornerShape(20.dp), color = WarningOrangeDim) {
                                Text("Pending Staff Approval", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WarningOrange, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(item.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(item.value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (editingItemId == item.id) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editProposedText,
                            onValueChange = { editProposedText = it },
                            label = { Text("Proposed adjustment") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (editProposedText.isNotBlank()) {
                                        onProposeEdit(item.id, editProposedText)
                                        editingItemId = null
                                        editProposedText = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Text("Submit for Review", color = Color(0xFF241E14), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { editingItemId = null }) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    } else if (!item.isPendingApproval) {
                        Spacer(Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                editingItemId = item.id
                                editProposedText = item.value
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Propose Edit →", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadsCommentsContent(
    disputes: List<DisputeThreadEntity>,
    onReply: (String, String) -> Unit
) {
    var activeThreadId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Disputes & Feedback Threads",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ongoing accountability and quality inquiries between you and our quality management team.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (disputes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No open disputes or feedback threads on file.",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(disputes, key = { it.id }) { thread ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(thread.sellerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(thread.sellerPhone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val badgeColor = if (thread.status == "open") WarningOrange else SuccessGreen
                            Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.copy(alpha = 0.2f)) {
                                Text(
                                    text = "${thread.type.uppercase()} · ${thread.status.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Reason: ${thread.reason}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    activeThreadId = if (activeThreadId == thread.id) null else thread.id
                                }
                            ) {
                                Text(if (activeThreadId == thread.id) "Hide Conversation" else "View Conversation →", fontSize = 12.sp, color = CoralPrimary)
                            }
                        }

                        if (activeThreadId == thread.id) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "You: Filed dispute regarding asking price.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Staff (Quality Lead): Under review. We are listening to the full call recording.",
                                        fontSize = 11.sp,
                                        color = CoralPrimary
                                    )

                                    if (thread.status == "open") {
                                        Spacer(Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Write a reply…", fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                if (replyText.isNotBlank()) {
                                                    onReply(thread.id, replyText)
                                                    replyText = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                                        ) {
                                            Text("Send Reply", fontSize = 11.sp)
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

@Composable
fun LeadsViewModeToggleRow(
    isMapView: Boolean,
    onToggleMapView: (Boolean) -> Unit,
    totalCount: Int,
    isOnline: Boolean = true,
    lastSyncTimestamp: Long = System.currentTimeMillis(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (isMapView) Icons.Default.Map else Icons.Default.ViewList,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isMapView) "MAP VIEW ($totalCount LEADS)" else "DIRECTORY ($totalCount LEADS)",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sync & Refresh Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isRefreshing) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isRefreshing) { onRefresh() }
                    .testTag("leads_sync_refresh_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Sync Leads",
                        tint = if (isRefreshing) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(13.dp)
                            .then(if (isRefreshing) Modifier.rotate(spinAngle) else Modifier)
                    )
                    Text(
                        text = if (isRefreshing) "Syncing..." else if (!isOnline) "Offline" else "Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRefreshing) CoralPrimary else if (!isOnline) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Segmented List vs Map Mode Toggle
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.testTag("leads_view_mode_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isMapView) CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleMapView(false) }
                            .testTag("leads_list_view_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = "List View",
                                tint = if (!isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "List",
                                fontSize = 10.sp,
                                fontWeight = if (!isMapView) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMapView) CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleMapView(true) }
                            .testTag("leads_map_view_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "Map View",
                                tint = if (isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Map",
                                fontSize = 10.sp,
                                fontWeight = if (isMapView) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatRelativeSyncTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        diff < 60_000 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

