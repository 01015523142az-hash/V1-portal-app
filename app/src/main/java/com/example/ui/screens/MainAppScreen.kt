package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.PortalViewModel

@Composable
fun MainAppScreen(
    portalViewModel: PortalViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by portalViewModel.uiState.collectAsState()
    val account by portalViewModel.account.collectAsState()
    val leads by portalViewModel.leads.collectAsState()
    val activities by portalViewModel.leadActivities.collectAsState()
    val tickets by portalViewModel.tickets.collectAsState()
    val supportTickets by portalViewModel.supportTickets.collectAsState()
    val campaignGuides by portalViewModel.campaignGuides.collectAsState()
    val disputes by portalViewModel.disputes.collectAsState()
    val listOrders by portalViewModel.listOrders.collectAsState()
    val skiptraceOrders by portalViewModel.skiptraceOrders.collectAsState()
    val skiptraceResults by portalViewModel.skiptraceResults.collectAsState()
    val subscriptions by portalViewModel.subscriptions.collectAsState()
    val invoices by portalViewModel.invoices.collectAsState()
    val transactions by portalViewModel.transactions.collectAsState()
    val paymentMethods by portalViewModel.paymentMethods.collectAsState()
    val notifications by portalViewModel.notifications.collectAsState()
    val unreadNotifications by portalViewModel.unreadNotificationsCount.collectAsState()
    val chatMessages by portalViewModel.chatMessages.collectAsState()
    val quietHoursSettings by portalViewModel.quietHoursSettings.collectAsState()
    val isOnline by portalViewModel.isOnline.collectAsState()
    val lastSyncTimestamp by portalViewModel.lastSyncTimestamp.collectAsState()

    Scaffold(
        topBar = {
            PortalTopAppBar(
                companyName = account?.companyName ?: "PropTech AI Portal",
                userName = account?.fullName ?: "",
                unreadNotifications = unreadNotifications,
                isDarkTheme = uiState.isDarkTheme,
                onToggleTheme = portalViewModel::toggleTheme,
                onOpenNotifications = portalViewModel::toggleNotificationCenter,
                onCreateCampaignClick = portalViewModel::openCreateCampaignModal,
                onOpenProfile = portalViewModel::openProfileModal,
                onToggleRemiClick = { portalViewModel.selectTab("remi") },
                onSignOutClick = authViewModel::signOut
            )
        },
        bottomBar = {
            PortalNavigationBar(
                selectedTab = uiState.selectedTab,
                ticketBadgeCount = tickets.size,
                onTabSelected = portalViewModel::selectTab
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Live Voice Call Active Bar (if call is ongoing)
                VoiceCallBar(
                    callState = uiState.callState,
                    onMuteToggle = portalViewModel::toggleCallMute,
                    onScreenShareToggle = portalViewModel::toggleScreenSharing,
                    onMinimizeToggle = portalViewModel::toggleCallVideoMinimized,
                    onHangup = portalViewModel::endVoiceCall
                )

                // Tab Content
                when (uiState.selectedTab) {
                    "dashboard" -> {
                        DashboardScreen(
                            account = account,
                            leads = leads,
                            activities = activities,
                            onLeadClick = { leadId ->
                                portalViewModel.openLeadDetail(leadId)
                                portalViewModel.selectTab("leads")
                            },
                            onCreateCampaignClick = portalViewModel::openCreateCampaignModal,
                            onNavigateTab = portalViewModel::selectTab,
                            onSimulateAlert = portalViewModel::triggerQuickAlertSimulation
                        )
                    }

                    "leads" -> {
                        LeadsScreen(
                            leads = leads,
                            activities = activities,
                            campaignGuides = campaignGuides,
                            disputes = disputes,
                            selectedSubtab = uiState.selectedLeadsSubtab,
                            onSubtabSelected = portalViewModel::selectLeadsSubtab,
                            searchQuery = uiState.leadSearchQuery,
                            onSearchQueryChanged = portalViewModel::onLeadSearchQueryChanged,
                            selectedCampaign = uiState.selectedCampaignFilter,
                            onCampaignFilterChanged = portalViewModel::onCampaignFilterChanged,
                            selectedStatus = uiState.selectedStatusFilter,
                            onStatusFilterChanged = portalViewModel::onStatusFilterChanged,
                            leadSortOption = uiState.leadSortOption,
                            onLeadSortOptionChanged = portalViewModel::setLeadSortOption,
                            leadSortAscending = uiState.leadSortAscending,
                            onToggleSortDirection = portalViewModel::toggleLeadSortDirection,
                            recentlyQualifiedLeadId = uiState.recentlyQualifiedLeadId,
                            leadStatusFeedbackMessage = uiState.leadStatusFeedbackMessage,
                            onUpdateLeadStatus = portalViewModel::updateLeadStatus,
                            onOpenVoiceNoteModal = portalViewModel::openVoiceNoteModal,
                            selectedLeadDetailId = uiState.selectedLeadDetailId,
                            onOpenLeadDetail = portalViewModel::openLeadDetail,
                            onCloseLeadDetail = portalViewModel::closeLeadDetail,
                            activePlayingLeadId = uiState.activePlayingLeadId,
                            isAudioPlaying = uiState.isAudioPlaying,
                            audioProgress = uiState.audioProgress,
                            audioDurationSec = uiState.audioDurationSec,
                            onToggleAudio = portalViewModel::toggleAudioPlayback,
                            onSeekAudio = portalViewModel::seekAudio,
                            isTranscriptVisible = uiState.isTranscriptVisible,
                            onToggleTranscript = portalViewModel::toggleTranscript,
                            onOpenDispute = portalViewModel::openDisputeModal,
                            onOpenFeedback = portalViewModel::openFeedbackModal,
                            onProposeGuideEdit = portalViewModel::proposeGuideEdit,
                            onSendDisputeReply = { _, _ -> },
                            isMapView = uiState.isLeadsMapView,
                            onToggleMapView = portalViewModel::setLeadsMapView,
                            onLogCallMade = portalViewModel::logLeadCallMade,
                            isOnline = isOnline,
                            lastSyncTimestamp = lastSyncTimestamp,
                            isRefreshing = uiState.isRefreshingLeads,
                            onRefresh = portalViewModel::refreshLeads,
                            syncStatusMessage = uiState.syncStatusMessage
                        )
                    }

                    "remi" -> {
                        ChatAssistantView(
                            messages = chatMessages,
                            inputText = uiState.chatInputText,
                            onInputChanged = portalViewModel::onChatInputChanged,
                            onSendMessage = portalViewModel::sendChatMessage,
                            onSendVoiceMessage = portalViewModel::sendVoiceMessage,
                            isPeerTyping = uiState.isPeerTyping,
                            onStartVoiceCall = portalViewModel::startVoiceCall,
                            onClose = null
                        )
                    }

                    "tickets" -> {
                        TicketsScreen(
                            tickets = tickets,
                            supportTickets = supportTickets,
                            onAcceptTicket = { id -> portalViewModel.answerTicket(id, true) },
                            onDeclineTicket = { id -> portalViewModel.answerTicket(id, false) },
                            onSubmitSupportTicket = portalViewModel::submitSupportTicket,
                            onReplySupportTicket = portalViewModel::addSupportTicketReply,
                            onUpdateSupportTicketStatus = portalViewModel::updateSupportTicketStatus
                        )
                    }

                    "buylist" -> {
                        BuyListScreen(
                            listOrders = listOrders,
                            selectedSubtab = uiState.selectedBuylistSubtab,
                            onSubtabSelected = portalViewModel::selectBuylistSubtab,
                            onSubmitOrder = portalViewModel::createListOrder
                        )
                    }

                    "skiptrace" -> {
                        SkiptraceScreen(
                            account = account,
                            skiptraceOrders = skiptraceOrders,
                            skiptraceResults = skiptraceResults,
                            isProcessingBatch = uiState.isBatchSkiptracing,
                            batchProgress = uiState.batchSkiptraceProgress,
                            batchStatusText = uiState.batchSkiptraceStatusText,
                            selectedSubtab = uiState.selectedSkiptraceSubtab,
                            onSubtabSelected = portalViewModel::selectSkiptraceSubtab,
                            onRunBatchSkiptrace = portalViewModel::runBatchSkiptrace,
                            onTopUpCredits = portalViewModel::topUpCredits
                        )
                    }

                    "billing" -> {
                        BillingScreen(
                            account = account,
                            subscriptions = subscriptions,
                            invoices = invoices,
                            transactions = transactions,
                            paymentMethods = paymentMethods,
                            selectedSubtab = uiState.selectedBillingSubtab,
                            onSubtabSelected = portalViewModel::selectBillingSubtab,
                            onOpenManageSubscription = portalViewModel::openManageSubscription,
                            onTopUpCredits = portalViewModel::topUpCredits,
                            onOpenAddPaymentMethod = portalViewModel::openAddPaymentMethodModal,
                            onSetDefaultPaymentMethod = portalViewModel::setDefaultPaymentMethod,
                            onDeletePaymentMethod = portalViewModel::deletePaymentMethod
                        )
                    }
                }
            }

            // Simulated Push Notification Banner HUD
            AnimatedVisibility(
                visible = uiState.activeSimulatedAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                uiState.activeSimulatedAlert?.let { alert ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                alert.targetRoute?.let { target -> portalViewModel.selectTab(target) }
                                portalViewModel.dismissSimulatedAlert()
                            }
                            .testTag("simulated_push_notification_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Incoming Alert",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SleekPrimary
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = alert.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = portalViewModel::dismissSimulatedAlert,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (uiState.isNotificationCenterOpen) {
        NotificationCenterSheet(
            notifications = notifications,
            onNotificationClick = { notif ->
                portalViewModel.markNotificationRead(notif.id)
                notif.targetRoute?.let { portalViewModel.selectTab(it) }
                portalViewModel.toggleNotificationCenter()
            },
            onMarkAllRead = portalViewModel::markAllNotificationsRead,
            onClearAll = portalViewModel::clearNotifications,
            onDismiss = portalViewModel::toggleNotificationCenter
        )
    }

    if (uiState.isCreateCampaignModalOpen) {
        CreateCampaignSheet(
            onDismiss = portalViewModel::closeCreateCampaignModal,
            onSubmit = { _, tier, dialer, data ->
                portalViewModel.addCaller("sub_01", tier, dialer, data)
            }
        )
    }

    if (uiState.isDisputeModalOpen && uiState.targetDisputeLeadId != null) {
        DisputeModalSheet(
            leadId = uiState.targetDisputeLeadId!!,
            onDismiss = portalViewModel::closeDisputeModal,
            onSubmit = portalViewModel::submitDispute
        )
    }

    if (uiState.isFeedbackModalOpen && uiState.targetDisputeLeadId != null) {
        FeedbackModalSheet(
            leadId = uiState.targetDisputeLeadId!!,
            onDismiss = portalViewModel::closeFeedbackModal,
            onSubmit = portalViewModel::submitFeedback
        )
    }

    if (uiState.isManageSubscriptionModalOpen && uiState.manageSubscriptionId != null) {
        ManageSubscriptionSheet(
            subscriptionId = uiState.manageSubscriptionId!!,
            onDismiss = portalViewModel::closeManageSubscription,
            onAddCaller = { tier, dialer, data ->
                portalViewModel.addCaller(uiState.manageSubscriptionId!!, tier, dialer, data)
            },
            onRequestPause = { portalViewModel.requestPause(uiState.manageSubscriptionId!!) },
            onRequestCancel = { portalViewModel.requestCancel(uiState.manageSubscriptionId!!) }
        )
    }

    if (uiState.isProfileModalOpen) {
        ProfileSheet(
            account = account,
            isDarkTheme = uiState.isDarkTheme,
            quietHoursSettings = quietHoursSettings,
            onToggleTheme = portalViewModel::toggleDarkTheme,
            onUpdateProfile = portalViewModel::updateAccountProfile,
            onUpdateNotificationPreferences = portalViewModel::updateNotificationPreferences,
            onUpdateQuietHours = portalViewModel::updateQuietHours,
            onUpdateBiometric = portalViewModel::updateBiometric,
            onSignOut = authViewModel::signOut,
            onDismiss = portalViewModel::closeProfileModal
        )
    }

    if (uiState.isAddPaymentMethodModalOpen) {
        AddPaymentMethodDialog(
            onDismiss = portalViewModel::closeAddPaymentMethodModal,
            onSaveCard = { brand, last4, expMonth, expYear, isDefault ->
                portalViewModel.addPaymentMethod(brand, last4, expMonth, expYear, isDefault)
            }
        )
    }

    if (uiState.isVoiceNoteModalOpen && uiState.voiceNoteTargetLeadId != null) {
        val targetLead = leads.find { it.id == uiState.voiceNoteTargetLeadId }
        VoiceNoteModal(
            lead = targetLead,
            isRecording = uiState.isRecordingVoiceNote,
            durationSec = uiState.voiceRecordingDurationSec,
            isTranscribing = uiState.isTranscribingVoiceNote,
            transcribedText = uiState.transcribedTextPreview,
            waveform = uiState.voiceNoteWaveform,
            onStartRecording = portalViewModel::startVoiceNoteRecording,
            onStopAndTranscribe = { portalViewModel.stopAndTranscribeVoiceNote(targetLead) },
            onSaveVoiceNote = { transcript ->
                portalViewModel.saveTranscribedVoiceNoteToLead(uiState.voiceNoteTargetLeadId!!)
            },
            onDismiss = portalViewModel::closeVoiceNoteModal
        )
    }
}
