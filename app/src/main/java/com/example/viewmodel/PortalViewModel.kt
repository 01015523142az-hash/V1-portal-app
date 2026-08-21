package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PortalRepository
import com.example.model.*
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CallUiState(
    val isCallActive: Boolean = false,
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isVideoMinimized: Boolean = false,
    val staffName: String = "Sarah Mitchell (Account Lead)",
    val durationSeconds: Int = 0
)

data class SimulatedAlert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "leads", // "leads", "tickets", "billing", "orders"
    val timestamp: Long = System.currentTimeMillis(),
    val targetRoute: String? = null
)

data class PortalUiState(
    val selectedTab: String = "dashboard",
    val selectedLeadsSubtab: String = "my_leads",
    val selectedBuylistSubtab: String = "request",
    val selectedSkiptraceSubtab: String = "batch_trace",
    val selectedBillingSubtab: String = "subscriptions",
    
    // Simulated Push Notification Banner State
    val activeSimulatedAlert: SimulatedAlert? = null,
    val isBatchSkiptracing: Boolean = false,
    val batchSkiptraceProgress: Float = 0f,
    val batchSkiptraceStatusText: String = "",
    
    // Leads search, sort & filters
    val leadSearchQuery: String = "",
    val selectedCampaignFilter: String = "",
    val selectedStatusFilter: String = "all", // "all", "new", "qualified", "accepted"
    val leadSortOption: String = "date_added", // "date_added", "last_interaction", "alphabetical"
    val leadSortAscending: Boolean = false,
    val dateFromFilter: String = "",
    val dateToFilter: String = "",
    
    // Status update animation & visual feedback
    val recentlyQualifiedLeadId: String? = null,
    val leadStatusFeedbackMessage: String? = null,
    
    // Voice Note Recording & AI Transcription
    val isVoiceNoteModalOpen: Boolean = false,
    val voiceNoteTargetLeadId: String? = null,
    val isRecordingVoiceNote: Boolean = false,
    val voiceRecordingDurationSec: Int = 0,
    val isTranscribingVoiceNote: Boolean = false,
    val transcribedTextPreview: String = "",
    val voiceNoteWaveform: List<Float> = emptyList(),
    
    // Detail view
    val selectedLeadDetailId: String? = null,
    
    // Audio Player
    val activePlayingLeadId: String? = null,
    val isAudioPlaying: Boolean = false,
    val audioProgress: Float = 0f,
    val audioDurationSec: Int = 180,
    val isTranscriptVisible: Boolean = false,
    
    // Dialogs & Modals
    val isDisputeModalOpen: Boolean = false,
    val isFeedbackModalOpen: Boolean = false,
    val targetDisputeLeadId: String? = null,
    val isCreateCampaignModalOpen: Boolean = false,
    val isManageSubscriptionModalOpen: Boolean = false,
    val manageSubscriptionId: String? = null,
    val isAddPaymentMethodModalOpen: Boolean = false,
    val isProfileModalOpen: Boolean = false,
    val isNotificationCenterOpen: Boolean = false,
    val isChatPanelOpen: Boolean = false,
    
    // Voice Call Bar
    val callState: CallUiState = CallUiState(),
    
    // Chat
    val chatInputText: String = "",
    val isPeerTyping: Boolean = false,
    val activeCallThreadStaff: String = "Sarah Mitchell",
    
    // App Theme / Tweaks
    val isDarkTheme: Boolean = true,
    val isLeadsMapView: Boolean = false
)

class PortalViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PortalUiState())
    val uiState: StateFlow<PortalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.isDarkThemeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        viewModelScope.launch {
            repository.isLeadsMapViewFlow.collect { isMap ->
                _uiState.update { it.copy(isLeadsMapView = isMap) }
            }
        }
    }

    // Reactive database streams
    val account: StateFlow<ClientAccount?> = repository.accountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leads: StateFlow<List<LeadEntity>> = repository.leadsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tickets: StateFlow<List<TicketEntity>> = repository.ticketsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.supportTicketsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disputes: StateFlow<List<DisputeThreadEntity>> = repository.disputesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaignGuides: StateFlow<List<CampaignGuideItem>> = repository.campaignGuideFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listOrders: StateFlow<List<ListOrderEntity>> = repository.listOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skiptraceOrders: StateFlow<List<SkiptraceOrderEntity>> = repository.skiptraceOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skiptraceResults: StateFlow<List<SkiptraceResultEntity>> = repository.skiptraceResultsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leadActivities: StateFlow<List<LeadActivityEntity>> = repository.leadActivitiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.subscriptionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.invoicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.paymentMethodsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val quietHoursSettings: StateFlow<com.example.data.QuietHoursSettings> = repository.quietHoursSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.QuietHoursSettings())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var audioPlaybackJob: Job? = null
    private var callDurationJob: Job? = null

    // --- Navigation & Subtab selection ---
    fun selectTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab, selectedLeadDetailId = null) }
    }

    fun selectLeadsSubtab(subtab: String) = _uiState.update { it.copy(selectedLeadsSubtab = subtab) }
    fun selectBuylistSubtab(subtab: String) = _uiState.update { it.copy(selectedBuylistSubtab = subtab) }
    fun selectSkiptraceSubtab(subtab: String) = _uiState.update { it.copy(selectedSkiptraceSubtab = subtab) }
    fun selectBillingSubtab(subtab: String) = _uiState.update { it.copy(selectedBillingSubtab = subtab) }

    fun openLeadDetail(leadId: String) = _uiState.update { it.copy(selectedLeadDetailId = leadId) }
    fun closeLeadDetail() = _uiState.update { it.copy(selectedLeadDetailId = null) }

    // --- Search, Sort & Filters ---
    fun onLeadSearchQueryChanged(q: String) = _uiState.update { it.copy(leadSearchQuery = q) }
    fun onCampaignFilterChanged(camp: String) = _uiState.update { it.copy(selectedCampaignFilter = camp) }
    fun onStatusFilterChanged(status: String) = _uiState.update { it.copy(selectedStatusFilter = status) }
    fun setLeadSortOption(option: String) = _uiState.update { it.copy(leadSortOption = option) }
    fun toggleLeadSortDirection() = _uiState.update { it.copy(leadSortAscending = !it.leadSortAscending) }
    fun onDateFromFilterChanged(d: String) = _uiState.update { it.copy(dateFromFilter = d) }
    fun onDateToFilterChanged(d: String) = _uiState.update { it.copy(dateToFilter = d) }

    // --- Lead Status Updates with Animation & Feedback ---
    fun updateLeadStatus(leadId: String, newStatus: String) {
        viewModelScope.launch {
            val isPromoted = newStatus.lowercase() == "qualified" || newStatus.lowercase() == "accepted"
            if (isPromoted) {
                _uiState.update {
                    it.copy(
                        recentlyQualifiedLeadId = leadId,
                        leadStatusFeedbackMessage = "🎉 Lead moved to ${newStatus.replaceFirstChar { c -> c.uppercase() }}!"
                    )
                }
            }
            repository.updateLeadStatus(leadId, newStatus)
            
            if (isPromoted) {
                delay(4000)
                _uiState.update {
                    if (it.recentlyQualifiedLeadId == leadId) {
                        it.copy(recentlyQualifiedLeadId = null, leadStatusFeedbackMessage = null)
                    } else it
                }
            }
        }
    }

    // --- Voice Note Recording & AI Transcription ---
    private var voiceRecordingJob: Job? = null

    fun openVoiceNoteModal(leadId: String) {
        _uiState.update {
            it.copy(
                isVoiceNoteModalOpen = true,
                voiceNoteTargetLeadId = leadId,
                isRecordingVoiceNote = false,
                voiceRecordingDurationSec = 0,
                isTranscribingVoiceNote = false,
                transcribedTextPreview = "",
                voiceNoteWaveform = emptyList()
            )
        }
    }

    fun closeVoiceNoteModal() {
        voiceRecordingJob?.cancel()
        _uiState.update {
            it.copy(
                isVoiceNoteModalOpen = false,
                voiceNoteTargetLeadId = null,
                isRecordingVoiceNote = false,
                voiceRecordingDurationSec = 0,
                isTranscribingVoiceNote = false,
                transcribedTextPreview = "",
                voiceNoteWaveform = emptyList()
            )
        }
    }

    fun startVoiceNoteRecording() {
        voiceRecordingJob?.cancel()
        _uiState.update {
            it.copy(
                isRecordingVoiceNote = true,
                voiceRecordingDurationSec = 0,
                transcribedTextPreview = "",
                voiceNoteWaveform = listOf(0.3f, 0.5f, 0.8f, 0.4f, 0.6f)
            )
        }
        voiceRecordingJob = viewModelScope.launch {
            while (_uiState.value.isRecordingVoiceNote) {
                delay(1000)
                val newSec = _uiState.value.voiceRecordingDurationSec + 1
                val randomWave = (1..12).map { (20..95).random() / 100f }
                _uiState.update {
                    it.copy(
                        voiceRecordingDurationSec = newSec,
                        voiceNoteWaveform = randomWave
                    )
                }
            }
        }
    }

    fun stopAndTranscribeVoiceNote(targetLead: LeadEntity?) {
        val duration = _uiState.value.voiceRecordingDurationSec.coerceAtLeast(4)
        voiceRecordingJob?.cancel()
        _uiState.update {
            it.copy(
                isRecordingVoiceNote = false,
                isTranscribingVoiceNote = true
            )
        }
        viewModelScope.launch {
            delay(1800) // Simulated Gemini neural speech-to-text processing
            val sellerName = targetLead?.sellerName ?: "Seller"
            val address = targetLead?.propertyAddress ?: "Property"
            val price = targetLead?.askingPrice ?: "$250,000"

            val transcriptionsPool = listOf(
                "Spoke with $sellerName regarding $address. Seller indicated strong motivation to close within 30 days due to relocation. Confirmed asking price of $price with room for a 5% cash discount if inspection is waived. Title is clear with no secondary liens.",
                "Followed up on $address with $sellerName. Property roof was replaced in 2021, HVAC serviced last fall. Seller is very receptive to an all-cash as-is offer and requested a formal letter of intent by end of week.",
                "Voice memo for $address: Key decision maker is $sellerName. They want a flexible 60-day leaseback post-closing to pack up belongings. Agreed to provide seller financing on 20% of the equity at 4.5% interest.",
                "Quick update on $sellerName ($address): Property has cosmetic deferred maintenance (flooring and interior paint). Willing to accept $price if closing costs are covered by buyer."
            )
            val generatedTranscription = transcriptionsPool.random()
            
            _uiState.update {
                it.copy(
                    isTranscribingVoiceNote = false,
                    transcribedTextPreview = generatedTranscription
                )
            }
        }
    }

    fun saveTranscribedVoiceNoteToLead(leadId: String, title: String = "Client Voice Memo") {
        val text = _uiState.value.transcribedTextPreview
        val duration = _uiState.value.voiceRecordingDurationSec.coerceAtLeast(5)
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addVoiceNoteLeadActivity(
                leadId = leadId,
                durationSec = duration,
                transcriptionText = text,
                memoTitle = title
            )
            closeVoiceNoteModal()
        }
    }

    // --- Audio Player ---
    fun toggleAudioPlayback(leadId: String, durationSec: Int = 180) {
        if (_uiState.value.activePlayingLeadId == leadId && _uiState.value.isAudioPlaying) {
            // Pause
            audioPlaybackJob?.cancel()
            _uiState.update { it.copy(isAudioPlaying = false) }
        } else {
            // Play
            audioPlaybackJob?.cancel()
            _uiState.update {
                it.copy(
                    activePlayingLeadId = leadId,
                    isAudioPlaying = true,
                    audioDurationSec = durationSec
                )
            }
            audioPlaybackJob = viewModelScope.launch {
                val totalSteps = 100
                val delayTime = (durationSec * 1000L) / totalSteps
                while (_uiState.value.audioProgress < 1f && _uiState.value.isAudioPlaying) {
                    delay(delayTime.coerceAtLeast(100L))
                    _uiState.update {
                        val next = it.audioProgress + (1f / totalSteps)
                        if (next >= 1f) {
                            it.copy(audioProgress = 0f, isAudioPlaying = false)
                        } else {
                            it.copy(audioProgress = next)
                        }
                    }
                }
            }
        }
    }

    fun seekAudio(progress: Float) {
        _uiState.update { it.copy(audioProgress = progress.coerceIn(0f, 1f)) }
    }

    fun toggleTranscript() {
        _uiState.update { it.copy(isTranscriptVisible = !it.isTranscriptVisible) }
    }

    // --- Modals & Dialogs ---
    fun openDisputeModal(leadId: String) = _uiState.update { it.copy(isDisputeModalOpen = true, targetDisputeLeadId = leadId) }
    fun closeDisputeModal() = _uiState.update { it.copy(isDisputeModalOpen = false, targetDisputeLeadId = null) }

    fun openFeedbackModal(leadId: String) = _uiState.update { it.copy(isFeedbackModalOpen = true, targetDisputeLeadId = leadId) }
    fun closeFeedbackModal() = _uiState.update { it.copy(isFeedbackModalOpen = false, targetDisputeLeadId = null) }

    fun openCreateCampaignModal() = _uiState.update { it.copy(isCreateCampaignModalOpen = true) }
    fun closeCreateCampaignModal() = _uiState.update { it.copy(isCreateCampaignModalOpen = false) }

    fun openManageSubscription(subId: String) = _uiState.update { it.copy(isManageSubscriptionModalOpen = true, manageSubscriptionId = subId) }
    fun closeManageSubscription() = _uiState.update { it.copy(isManageSubscriptionModalOpen = false, manageSubscriptionId = null) }

    fun openAddPaymentMethodModal() = _uiState.update { it.copy(isAddPaymentMethodModalOpen = true) }
    fun closeAddPaymentMethodModal() = _uiState.update { it.copy(isAddPaymentMethodModalOpen = false) }

    fun openProfileModal() = _uiState.update { it.copy(isProfileModalOpen = true) }
    fun closeProfileModal() = _uiState.update { it.copy(isProfileModalOpen = false) }

    fun toggleNotificationCenter() = _uiState.update { it.copy(isNotificationCenterOpen = !it.isNotificationCenterOpen) }
    fun toggleChatPanel() = _uiState.update { it.copy(isChatPanelOpen = !it.isChatPanelOpen) }

    fun toggleTheme() = _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }

    // --- Actions with Repository ---
    fun submitDispute(reason: String, explanation: String) {
        val leadId = _uiState.value.targetDisputeLeadId ?: return
        viewModelScope.launch {
            repository.fileDispute(leadId, reason, explanation)
            closeDisputeModal()
        }
    }

    fun submitFeedback(comment: String) {
        val leadId = _uiState.value.targetDisputeLeadId ?: return
        viewModelScope.launch {
            repository.fileFeedback(leadId, comment)
            closeFeedbackModal()
        }
    }

    fun answerTicket(ticketId: String, accept: Boolean) {
        viewModelScope.launch {
            repository.answerTicket(ticketId, accept)
        }
    }

    fun submitSupportTicket(
        subject: String,
        category: String,
        priority: String,
        description: String,
        relatedLeadId: String? = null
    ) {
        viewModelScope.launch {
            repository.submitSupportTicket(subject, category, priority, description, relatedLeadId)
        }
    }

    fun addSupportTicketReply(ticketId: String, reply: String) {
        viewModelScope.launch {
            repository.addSupportTicketReply(ticketId, reply)
        }
    }

    fun updateSupportTicketStatus(ticketId: String, status: String) {
        viewModelScope.launch {
            repository.updateSupportTicketStatus(ticketId, status)
        }
    }

    fun getSupportTicketMessages(ticketId: String) = repository.getSupportTicketMessagesFlow(ticketId)

    fun proposeGuideEdit(id: Long, proposedValue: String) {
        viewModelScope.launch {
            repository.proposeGuideEdit(id, proposedValue)
        }
    }

    fun createListOrder(typeOfLeads: String, propertyType: String, targetMarket: String, otherDetails: String, count: Int) {
        viewModelScope.launch {
            repository.createListOrder(typeOfLeads, propertyType, targetMarket, otherDetails, count)
        }
    }

    fun placeSkiptraceBatch(recordCount: Int) {
        viewModelScope.launch {
            repository.placeSkiptraceOrder(recordCount)
        }
    }

    fun runBatchSkiptrace(
        rawInputs: List<String>,
        onSuccess: () -> Unit = {}
    ) {
        if (rawInputs.isEmpty()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBatchSkiptracing = true,
                    batchSkiptraceProgress = 0.1f,
                    batchSkiptraceStatusText = "Connecting to LexisNexis / TLOxp carrier registries..."
                )
            }
            delay(800)
            _uiState.update {
                it.copy(
                    batchSkiptraceProgress = 0.45f,
                    batchSkiptraceStatusText = "Scrubbing National DNC registry & validating phone carrier lines..."
                )
            }
            delay(900)
            _uiState.update {
                it.copy(
                    batchSkiptraceProgress = 0.8f,
                    batchSkiptraceStatusText = "Retrieving verified emails, relative contacts & deed records..."
                )
            }
            delay(700)
            repository.runBatchSkiptrace(rawInputs)
            _uiState.update {
                it.copy(
                    isBatchSkiptracing = false,
                    batchSkiptraceProgress = 1.0f,
                    batchSkiptraceStatusText = "Complete! ${rawInputs.size} records enriched.",
                    selectedSkiptraceSubtab = "results"
                )
            }
            onSuccess()
        }
    }

    // --- Mock Push Notification Handler Simulation ---
    fun simulatePushAlert(
        title: String,
        body: String,
        category: String = "leads",
        leadAddress: String? = null,
        sellerName: String? = null
    ) {
        viewModelScope.launch {
            val alert = SimulatedAlert(
                id = UUID.randomUUID().toString(),
                title = title,
                message = body,
                category = category,
                timestamp = System.currentTimeMillis(),
                targetRoute = if (category == "tickets") "tickets" else if (category == "orders") "skiptrace" else "leads"
            )
            _uiState.update { it.copy(activeSimulatedAlert = alert) }
            repository.simulateIncomingLeadAlert(title, body, category, leadAddress, sellerName)
            
            // Auto dismiss notification banner after 6 seconds
            delay(6000)
            if (_uiState.value.activeSimulatedAlert?.id == alert.id) {
                _uiState.update { it.copy(activeSimulatedAlert = null) }
            }
        }
    }

    fun dismissSimulatedAlert() {
        _uiState.update { it.copy(activeSimulatedAlert = null) }
    }

    fun triggerQuickAlertSimulation(presetIndex: Int = 0) {
        when (presetIndex) {
            0 -> simulatePushAlert(
                title = "🎯 High Motivation Lead Delivered",
                body = "Sarah Santos submitted 7412 Lakewood Blvd, Dallas, TX (Asking $180k, Zillow ARV $265k). Ready for underwriting.",
                category = "leads",
                leadAddress = "7412 Lakewood Blvd, Dallas, TX",
                sellerName = "Robert & Linda Morales"
            )
            1 -> simulatePushAlert(
                title = "🎟️ New Deal Review Ticket",
                body = "Cold caller flagged 5120 Northside Pkwy, Atlanta, GA: Inherited estate wanting 14-day cash close.",
                category = "tickets",
                leadAddress = "5120 Northside Pkwy, Atlanta, GA",
                sellerName = "William Sterling"
            )
            2 -> simulatePushAlert(
                title = "🔍 Skip Trace Batch Completed",
                body = "250 property records processed. 97.4% match rate with wireless carrier validation.",
                category = "orders"
            )
            else -> simulatePushAlert(
                title = "⚡ Live Caller Activity Update",
                body = "Caller Maria S. just completed a 4-minute qualification call with owner on Meadowbrook Dr.",
                category = "leads",
                leadAddress = "4812 Meadowbrook Dr, Dallas, TX",
                sellerName = "David Henderson"
            )
        }
    }

    fun addPaymentMethod(brand: String, last4: String, expMonth: Int, expYear: Int, isDefault: Boolean = true) {
        viewModelScope.launch {
            repository.addPaymentMethod(brand, last4, expMonth, expYear, isDefault)
            closeAddPaymentMethodModal()
        }
    }

    fun setDefaultPaymentMethod(id: String) {
        viewModelScope.launch {
            repository.setDefaultPaymentMethod(id)
        }
    }

    fun deletePaymentMethod(id: String) {
        viewModelScope.launch {
            repository.deletePaymentMethod(id)
        }
    }

    fun updateAccountProfile(name: String, company: String, phone: String) {
        viewModelScope.launch {
            repository.updateAccountProfile(name, company, phone)
        }
    }

    fun updateNotificationPreferences(
        leads: Boolean,
        tickets: Boolean,
        billing: Boolean,
        orders: Boolean,
        sms: Boolean
    ) {
        viewModelScope.launch {
            repository.updateNotificationPreferences(leads, tickets, billing, orders, sms)
        }
    }

    fun updateQuietHours(settings: com.example.data.QuietHoursSettings) {
        viewModelScope.launch {
            repository.updateQuietHoursSettings(settings)
        }
    }

    fun toggleQuietHours(enabled: Boolean) {
        val current = quietHoursSettings.value
        updateQuietHours(current.copy(isEnabled = enabled))
    }

    fun setQuietHoursTimeRange(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val current = quietHoursSettings.value
        updateQuietHours(
            current.copy(
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute
            )
        )
    }

    fun logLeadCallMade(leadId: String) {
        viewModelScope.launch {
            repository.logLeadCallMade(leadId)
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBiometric(enabled)
        }
    }

    fun topUpCredits(credits: Int) {
        viewModelScope.launch {
            repository.topUpSkiptraceCredits(credits)
        }
    }

    fun requestPause(subId: String) = viewModelScope.launch { repository.requestPauseSubscription(subId) }
    fun requestCancel(subId: String) = viewModelScope.launch { repository.requestCancelSubscription(subId) }

    fun addCaller(subId: String, tier: String, dialer: String, data: String) {
        viewModelScope.launch {
            repository.addCaller(subId, tier, dialer, data)
            closeManageSubscription()
        }
    }

    // --- Voice Calling Simulation ---
    fun startVoiceCall() {
        _uiState.update {
            it.copy(
                callState = CallUiState(
                    isCallActive = true,
                    isConnected = false,
                    isMuted = false,
                    isScreenSharing = false,
                    durationSeconds = 0
                )
            )
        }
        viewModelScope.launch {
            delay(2000) // Simulated connection
            _uiState.update { it.copy(callState = it.callState.copy(isConnected = true)) }
            callDurationJob?.cancel()
            callDurationJob = launch {
                while (_uiState.value.callState.isCallActive) {
                    delay(1000)
                    _uiState.update {
                        it.copy(callState = it.callState.copy(durationSeconds = it.callState.durationSeconds + 1))
                    }
                }
            }
        }
    }

    fun toggleCallMute() {
        _uiState.update { it.copy(callState = it.callState.copy(isMuted = !it.callState.isMuted)) }
    }

    fun toggleScreenSharing() {
        _uiState.update { it.copy(callState = it.callState.copy(isScreenSharing = !it.callState.isScreenSharing)) }
    }

    fun toggleCallVideoMinimized() {
        _uiState.update { it.copy(callState = it.callState.copy(isVideoMinimized = !it.callState.isVideoMinimized)) }
    }

    fun endVoiceCall() {
        callDurationJob?.cancel()
        _uiState.update { it.copy(callState = CallUiState(isCallActive = false)) }
    }

    // --- Chat Actions ---
    fun onChatInputChanged(text: String) = _uiState.update { it.copy(chatInputText = text) }

    fun sendChatMessage() {
        val text = _uiState.value.chatInputText.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(chatInputText = "", isPeerTyping = true) }
        viewModelScope.launch {
            repository.sendChatMessage(text)
            delay(1500)
            _uiState.update { it.copy(isPeerTyping = false) }
        }
    }

    fun sendVoiceMessage() {
        viewModelScope.launch {
            repository.sendChatMessage("🎙️ Voice message (0:14)", isAudio = true, audioDuration = 14)
        }
    }

    fun markNotificationRead(id: String) = viewModelScope.launch { repository.markNotificationRead(id) }
    fun markAllNotificationsRead() = viewModelScope.launch { repository.markAllNotificationsRead() }
    fun clearNotifications() = viewModelScope.launch { repository.clearNotifications() }

    // --- Global Theme & Preferences ---
    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(enabled)
        }
    }

    fun toggleDarkTheme() {
        val next = !_uiState.value.isDarkTheme
        setDarkTheme(next)
    }

    fun setLeadsMapView(isMap: Boolean) {
        viewModelScope.launch {
            repository.setLeadsMapView(isMap)
        }
    }

    fun toggleLeadsMapView() {
        val next = !_uiState.value.isLeadsMapView
        setLeadsMapView(next)
    }
}

class PortalViewModelFactory(private val repository: PortalRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PortalViewModel(repository) as T
    }
}
