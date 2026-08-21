package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class PortalRepository(
    private val database: PortalDatabase,
    private val context: Context
) {
    private val accountDao = database.accountDao()
    private val leadDao = database.leadDao()
    private val ticketDao = database.ticketDao()
    private val supportTicketDao = database.supportTicketDao()
    private val disputeDao = database.disputeDao()
    private val campaignGuideDao = database.campaignGuideDao()
    private val listOrderDao = database.listOrderDao()
    private val skiptraceDao = database.skiptraceDao()
    private val billingDao = database.billingDao()
    private val notificationDao = database.notificationDao()
    private val chatDao = database.chatDao()
    private val leadActivityDao = database.leadActivityDao()
    private val skiptraceResultDao = database.skiptraceResultDao()
    private val firestoreCloudService = FirestoreCloudService()
    val userPreferencesRepository = UserPreferencesRepository(context)
    val networkMonitor = NetworkMonitor(context)

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannels()
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // --- Flows ---
    val isDarkThemeFlow: Flow<Boolean> = userPreferencesRepository.isDarkThemeFlow
    val themePresetFlow: Flow<String> = userPreferencesRepository.themePresetFlow
    val fontPresetFlow: Flow<String> = userPreferencesRepository.fontPresetFlow
    val autoBiometricLoginFlow: Flow<Boolean> = userPreferencesRepository.autoBiometricLoginFlow
    val isLeadsMapViewFlow: Flow<Boolean> = userPreferencesRepository.isLeadsMapViewFlow
    val quietHoursSettingsFlow: Flow<QuietHoursSettings> = userPreferencesRepository.quietHoursSettingsFlow
    val accountFlow: Flow<ClientAccount?> = accountDao.getAccountFlow()
    val leadsFlow: Flow<List<LeadEntity>> = leadDao.getAllLeadsFlow()
    val ticketsFlow: Flow<List<TicketEntity>> = ticketDao.getReviewTicketsFlow()
    val allTicketsFlow: Flow<List<TicketEntity>> = ticketDao.getAllTicketsFlow()
    val supportTicketsFlow: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllSupportTicketsFlow()
    val disputesFlow: Flow<List<DisputeThreadEntity>> = disputeDao.getAllThreadsFlow()
    val campaignGuideFlow: Flow<List<CampaignGuideItem>> = campaignGuideDao.getGuideItemsFlow()
    val listOrdersFlow: Flow<List<ListOrderEntity>> = listOrderDao.getListOrdersFlow()
    val skiptraceOrdersFlow: Flow<List<SkiptraceOrderEntity>> = skiptraceDao.getOrdersFlow()
    val skiptraceResultsFlow: Flow<List<SkiptraceResultEntity>> = skiptraceResultDao.getAllResultsFlow()
    val leadActivitiesFlow: Flow<List<LeadActivityEntity>> = leadActivityDao.getAllActivitiesFlow()
    val subscriptionsFlow: Flow<List<SubscriptionEntity>> = billingDao.getSubscriptionsFlow()
    val invoicesFlow: Flow<List<InvoiceEntity>> = billingDao.getInvoicesFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = billingDao.getTransactionsFlow()
    val paymentMethodsFlow: Flow<List<PaymentMethodEntity>> = billingDao.getPaymentMethodsFlow()
    val notificationsFlow: Flow<List<NotificationItem>> = notificationDao.getNotificationsFlow()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCountFlow()
    val chatMessagesFlow: Flow<List<ChatMessage>> = chatDao.getMessagesFlow()
    val isOnlineFlow: Flow<Boolean> = networkMonitor.isConnected
    val lastSyncTimestampFlow: Flow<Long> = userPreferencesRepository.lastSyncTimestampFlow

    suspend fun syncDataWithServer(): Boolean {
        return try {
            val isOnline = networkMonitor.isCurrentlyConnected()
            if (isOnline) {
                // Simulate cloud sync roundtrip (e.g. Supabase / Firestore sync)
                kotlinx.coroutines.delay(650)
                // Sync any cloud documents or update timestamp
                userPreferencesRepository.setLastSyncTimestamp(System.currentTimeMillis())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        userPreferencesRepository.setLastSyncTimestamp(timestamp)
    }

    fun getLeadByIdFlow(id: String): Flow<LeadEntity?> = leadDao.getLeadByIdFlow(id)
    fun getSupportTicketByIdFlow(id: String): Flow<SupportTicketEntity?> = supportTicketDao.getSupportTicketByIdFlow(id)
    fun getSupportTicketMessagesFlow(ticketId: String): Flow<List<SupportTicketMessageEntity>> = supportTicketDao.getMessagesFlow(ticketId)
    fun getDisputeMessagesFlow(disputeId: String): Flow<List<DisputeMessageEntity>> = disputeDao.getMessagesFlow(disputeId)

    // --- Account / Auth / Preferences ---
    suspend fun getAccount(): ClientAccount? = accountDao.getAccount()
    suspend fun saveAccount(account: ClientAccount) = accountDao.insertOrUpdate(account)
    suspend fun updateBiometric(enabled: Boolean) = accountDao.updateBiometric(enabled)
    suspend fun updateLastLogin() = accountDao.updateLastLogin(System.currentTimeMillis())
    suspend fun setDarkTheme(isDark: Boolean) = userPreferencesRepository.setDarkTheme(isDark)
    suspend fun setThemePreset(presetId: String) = userPreferencesRepository.setThemePreset(presetId)
    suspend fun setFontPreset(fontId: String) = userPreferencesRepository.setFontPreset(fontId)
    suspend fun setAutoBiometricLogin(enabled: Boolean) = userPreferencesRepository.setAutoBiometricLogin(enabled)
    suspend fun setLeadsMapView(isMap: Boolean) = userPreferencesRepository.setLeadsMapView(isMap)
    suspend fun setQuietHoursSettings(settings: QuietHoursSettings) = userPreferencesRepository.setQuietHoursSettings(settings)

    // --- Notification Channels ---
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelLeads = NotificationChannel(
                CHANNEL_LEADS,
                "Lead Delivery & Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts for newly delivered and accepted leads" }

            val channelTickets = NotificationChannel(
                CHANNEL_TICKETS,
                "Deal Review Tickets",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts for new tickets waiting for review" }

            val channelBilling = NotificationChannel(
                CHANNEL_BILLING,
                "Billing & Subscriptions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Invoices, renewals, and charge confirmations" }

            val channelOrders = NotificationChannel(
                CHANNEL_ORDERS,
                "List & Skip Trace Orders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Status updates for data orders and skip traces" }

            val channelSilent = NotificationChannel(
                CHANNEL_SILENT,
                "Quiet Hours Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent muted alerts delivered during scheduled quiet hours"
                enableVibration(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannels(
                listOf(channelLeads, channelTickets, channelBilling, channelOrders, channelSilent)
            )
        }
    }

    fun triggerClientPushNotification(
        title: String,
        body: String,
        category: String,
        targetRoute: String? = null
    ) {
        scope.launch {
            val notif = NotificationItem(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                category = category,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetRoute = targetRoute
            )
            notificationDao.insertNotification(notif)

            val quietHours = userPreferencesRepository.getQuietHoursSettingsSync()
            val inQuietHours = quietHours.isCurrentlyInQuietHours()

            // Select active channel based on Quiet Hours
            val channelId = if (inQuietHours) {
                CHANNEL_SILENT
            } else {
                when (category) {
                    "leads" -> CHANNEL_LEADS
                    "tickets" -> CHANNEL_TICKETS
                    "billing" -> CHANNEL_BILLING
                    else -> CHANNEL_ORDERS
                }
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle(if (inQuietHours) "🌙 $title" else title)
                .setContentText(body)
                .setAutoCancel(true)

            if (inQuietHours) {
                // Ensure notification is silent and does not sound or vibrate during quiet hours
                builder.setSilent(true)
                builder.setPriority(NotificationCompat.PRIORITY_LOW)
                builder.setSubText("Quiet Hours Active")
            } else {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }

            try {
                notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            } catch (_: Exception) {}
        }
    }

    // --- Lead Operations ---
    suspend fun logLeadCallMade(leadId: String) {
        val lead = leadDao.getLeadById(leadId) ?: return
        val activity = LeadActivityEntity(
            id = "act_call_" + UUID.randomUUID().toString().take(8),
            leadId = leadId,
            leadAddress = "${lead.propertyAddress}, ${lead.propertyCity}",
            sellerName = lead.sellerName,
            activityType = "call_completed",
            title = "Call Placed to Seller",
            description = "Client initiated call to ${lead.sellerName} at ${lead.sellerPhone} via native dialer.",
            actorName = "Alex Morgan",
            actorRole = "Client",
            timestamp = System.currentTimeMillis()
        )
        leadActivityDao.insertActivity(activity)
    }

    suspend fun updateLeadStatus(leadId: String, newStatus: String) {
        val lead = leadDao.getLeadById(leadId) ?: return
        val oldStatus = lead.status
        leadDao.updateLeadStatus(leadId, newStatus)

        val formattedStatus = newStatus.replace("_", " ").replaceFirstChar { it.uppercase() }
        logLeadActivity(
            leadId = leadId,
            leadAddress = "${lead.propertyAddress}, ${lead.propertyCity}",
            sellerName = lead.sellerName,
            activityType = "status_change",
            title = "Status Updated to $formattedStatus",
            description = "Lead moved from '${oldStatus.replaceFirstChar { it.uppercase() }}' to '$formattedStatus' by client.",
            actorName = "Alex Morgan",
            actorRole = "Client"
        )

        triggerClientPushNotification(
            title = if (newStatus.lowercase() == "qualified") "🎉 Lead Qualified!" else "Lead Status Updated",
            body = "${lead.sellerName} (${lead.propertyAddress}) marked as $formattedStatus.",
            category = "leads",
            targetRoute = "leads"
        )
    }

    suspend fun addVoiceNoteLeadActivity(
        leadId: String,
        durationSec: Int,
        transcriptionText: String,
        memoTitle: String = "Client Voice Memo"
    ) {
        val lead = leadDao.getLeadById(leadId) ?: return
        val activity = LeadActivityEntity(
            id = "act_voice_" + UUID.randomUUID().toString().take(8),
            leadId = leadId,
            leadAddress = "${lead.propertyAddress}, ${lead.propertyCity}",
            sellerName = lead.sellerName,
            activityType = "voice_note",
            title = memoTitle,
            description = transcriptionText,
            actorName = "Alex Morgan",
            actorRole = "Client",
            timestamp = System.currentTimeMillis(),
            audioDurationSec = durationSec,
            transcriptionText = transcriptionText
        )
        leadActivityDao.insertActivity(activity)

        triggerClientPushNotification(
            title = "🎙️ Voice Note Transcribed & Saved",
            body = "AI transcribed audio note ($durationSec s) attached to ${lead.sellerName}'s activity timeline.",
            category = "leads",
            targetRoute = "leads"
        )
    }

    suspend fun fileDispute(leadId: String, reason: String, message: String) {
        val lead = leadDao.getLeadById(leadId) ?: return
        val disputeId = UUID.randomUUID().toString()
        val thread = DisputeThreadEntity(
            id = disputeId,
            leadId = leadId,
            sellerName = lead.sellerName,
            sellerPhone = lead.sellerPhone,
            type = "dispute",
            reason = reason,
            status = "open",
            createdAt = System.currentTimeMillis()
        )
        disputeDao.insertThread(thread)
        disputeDao.insertMessage(
            DisputeMessageEntity(
                id = UUID.randomUUID().toString(),
                disputeId = disputeId,
                senderType = "client",
                senderName = "You",
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
        leadDao.updateDisputeStatus(leadId, "open")

        triggerClientPushNotification(
            title = "Dispute Submitted",
            body = "Dispute filed on lead ${lead.sellerName}. Quality team will review within 24h.",
            category = "leads",
            targetRoute = "leads"
        )
    }

    suspend fun fileFeedback(leadId: String, message: String) {
        val lead = leadDao.getLeadById(leadId) ?: return
        val feedbackId = UUID.randomUUID().toString()
        val thread = DisputeThreadEntity(
            id = feedbackId,
            leadId = leadId,
            sellerName = lead.sellerName,
            sellerPhone = lead.sellerPhone,
            type = "feedback",
            reason = "General Feedback",
            status = "open",
            createdAt = System.currentTimeMillis()
        )
        disputeDao.insertThread(thread)
        disputeDao.insertMessage(
            DisputeMessageEntity(
                id = UUID.randomUUID().toString(),
                disputeId = feedbackId,
                senderType = "client",
                senderName = "You",
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
        leadDao.updateFeedbackStatus(leadId, "open")
    }

    suspend fun sendDisputeReply(disputeId: String, message: String) {
        disputeDao.insertMessage(
            DisputeMessageEntity(
                id = UUID.randomUUID().toString(),
                disputeId = disputeId,
                senderType = "client",
                senderName = "You",
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // --- Ticket Operations ---
    suspend fun answerTicket(ticketId: String, accept: Boolean) {
        val ticket = ticketDao.getTicketById(ticketId) ?: return
        if (accept) {
            ticketDao.updateTicketStatus(ticketId, "accepted")
            // Convert to accepted lead
            val newLeadId = "lead_" + UUID.randomUUID().toString().take(8)
            val newLead = LeadEntity(
                id = newLeadId,
                sellerName = "Robert Langdon",
                sellerPhone = "+1 (214) 555-0199",
                campaignId = "camp_01",
                campaignName = ticket.campaignName,
                status = "new",
                propertyAddress = ticket.propertyAddress,
                propertyCity = "Dallas",
                propertyState = "TX",
                propertyZip = "75201",
                askingPrice = ticket.askingPrice,
                marketValue = ticket.marketValue,
                marketValueSource = ticket.marketValueSource,
                whySell = ticket.whySell,
                reasonSell = ticket.whySell,
                whenSell = ticket.whenSell,
                notes = ticket.notes,
                submittedAt = System.currentTimeMillis(),
                reviewedAt = System.currentTimeMillis(),
                hasRecording = true,
                aiSummary = "Motivated seller agreed to deal parameters. Property condition needs minor cosmetic rehab."
            )
            leadDao.insertLead(newLead)
            triggerClientPushNotification(
                title = "🎉 Ticket Accepted & Converted",
                body = "${ticket.propertyAddress} has been added to your Leads list with seller contact info unlocked.",
                category = "tickets",
                targetRoute = "leads"
            )
        } else {
            ticketDao.updateTicketStatus(ticketId, "declined")
        }
    }

    // --- Support Ticket & Inquiry Operations ---
    suspend fun submitSupportTicket(
        subject: String,
        category: String,
        priority: String,
        description: String,
        relatedLeadId: String? = null
    ): SupportTicketEntity {
        val randomNum = (1000..9999).random()
        val ticketNumber = "TKT-$randomNum"
        val ticketId = "supp_" + UUID.randomUUID().toString().take(8)
        val newTicket = SupportTicketEntity(
            id = ticketId,
            ticketNumber = ticketNumber,
            subject = subject,
            category = category,
            priority = priority,
            description = description,
            status = "Open",
            relatedLeadId = relatedLeadId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastStaffReply = null
        )
        supportTicketDao.insertSupportTicket(newTicket)

        scope.launch {
            firestoreCloudService.syncSupportTicketToCloud(newTicket)
        }

        // Add initial inquiry message to conversation thread
        supportTicketDao.insertMessage(
            SupportTicketMessageEntity(
                id = UUID.randomUUID().toString(),
                ticketId = ticketId,
                senderName = "Alex Morgan",
                senderRole = "client",
                message = description,
                createdAt = System.currentTimeMillis()
            )
        )

        triggerClientPushNotification(
            title = "Support Inquiry Opened ($ticketNumber)",
            body = "Your inquiry regarding '$subject' has been assigned to our support team.",
            category = "tickets",
            targetRoute = "tickets"
        )

        // Simulate fast staff acknowledgment response
        scope.launch {
            kotlinx.coroutines.delay(2500)
            val ackMsg = "Thank you for reaching out, Alex. A support specialist from the $category operations team has received your ticket and is currently investigating."
            supportTicketDao.insertMessage(
                SupportTicketMessageEntity(
                    id = UUID.randomUUID().toString(),
                    ticketId = ticketId,
                    senderName = "Sarah M. (Support Ops)",
                    senderRole = "staff",
                    message = ackMsg,
                    createdAt = System.currentTimeMillis()
                )
            )
            supportTicketDao.updateStaffReply(ticketId, ackMsg)
            supportTicketDao.updateStatus(ticketId, "In Progress")
            triggerClientPushNotification(
                title = "Support Update: $ticketNumber",
                body = "Support Ops updated status to 'In Progress': '$ackMsg'",
                category = "tickets",
                targetRoute = "tickets"
            )
        }

        return newTicket
    }

    suspend fun addSupportTicketReply(ticketId: String, replyText: String) {
        val msg = SupportTicketMessageEntity(
            id = UUID.randomUUID().toString(),
            ticketId = ticketId,
            senderName = "Alex Morgan",
            senderRole = "client",
            message = replyText,
            createdAt = System.currentTimeMillis()
        )
        supportTicketDao.insertMessage(msg)
        supportTicketDao.updateStatus(ticketId, "In Progress")

        // Trigger staff response simulation
        scope.launch {
            kotlinx.coroutines.delay(2000)
            val staffReply = "Thanks for the additional context. We've updated the review notes accordingly."
            supportTicketDao.insertMessage(
                SupportTicketMessageEntity(
                    id = UUID.randomUUID().toString(),
                    ticketId = ticketId,
                    senderName = "Sarah M. (Support Ops)",
                    senderRole = "staff",
                    message = staffReply,
                    createdAt = System.currentTimeMillis()
                )
            )
            supportTicketDao.updateStaffReply(ticketId, staffReply)
        }
    }

    suspend fun updateSupportTicketStatus(ticketId: String, status: String) {
        supportTicketDao.updateStatus(ticketId, status)
    }

    // --- Campaign Guide Proposals ---
    suspend fun proposeGuideEdit(id: Long, proposedValue: String) {
        campaignGuideDao.proposeEdit(id, proposedValue)
        triggerClientPushNotification(
            title = "Campaign Guide Edit Proposed",
            body = "Your qualification criteria update has been sent to our Quality Team for review.",
            category = "leads"
        )
    }

    // --- Buy a List ---
    suspend fun createListOrder(
        typeOfLeads: String,
        propertyType: String,
        targetMarket: String,
        otherDetails: String,
        recordCount: Int
    ): ListOrderEntity {
        val priceCents = (recordCount * 4) // 4 cents per record
        val order = ListOrderEntity(
            id = "list_" + UUID.randomUUID().toString().take(8),
            typeOfLeads = typeOfLeads,
            propertyType = propertyType,
            targetMarket = targetMarket,
            otherDetails = otherDetails,
            recordCount = recordCount,
            priceCents = priceCents,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        listOrderDao.insertOrder(order)
        triggerClientPushNotification(
            title = "📋 List Request Received",
            body = "Order for ${recordCount.coerceAtLeast(1)} $propertyType records in $targetMarket is now being pulled by our data team.",
            category = "orders"
        )
        return order
    }

    // --- Skip Tracing ---
    suspend fun placeSkiptraceOrder(recordCount: Int): SkiptraceOrderEntity {
        accountDao.deductCredits(recordCount)
        val order = SkiptraceOrderEntity(
            id = "skp_" + UUID.randomUUID().toString().take(8),
            recordCount = recordCount,
            priceCents = recordCount * 3, // $0.03 per credit
            status = "completed",
            createdAt = System.currentTimeMillis(),
            resultsCount = (recordCount * 0.94).toInt() // realistic 94% match rate
        )
        skiptraceDao.insertOrder(order)
        triggerClientPushNotification(
            title = "🔍 Skip Trace Batch Ready",
            body = "Skip tracing finished for ${order.recordCount} records (${order.resultsCount} phone & email matches ready).",
            category = "orders"
        )
        return order
    }

    suspend fun runBatchSkiptrace(
        rawInputs: List<String>
    ): List<SkiptraceResultEntity> {
        val validInputs = rawInputs.map { it.trim() }.filter { it.isNotEmpty() }
        val count = validInputs.size.coerceAtLeast(1)
        
        // Deduct credits from account
        accountDao.deductCredits(count)
        
        val batchId = "batch_" + UUID.randomUUID().toString().take(8)
        val sampleCarriers = listOf("Verizon Wireless", "AT&T Mobility", "T-Mobile USA", "Spectrum Mobile")
        val sampleCities = listOf("Dallas", "Fort Worth", "Atlanta", "Phoenix", "Austin", "Miami", "Tampa", "Houston")
        val sampleStates = listOf("TX", "GA", "AZ", "FL")
        
        val generatedResults = validInputs.mapIndexed { idx, inputLine ->
            val parts = inputLine.split(",").map { it.trim() }
            val isNameFirst = parts.size >= 2 && !parts[0].any { it.isDigit() }
            
            val firstName: String
            val lastName: String
            val address: String
            val city: String
            val state: String
            val zip: String
            
            if (isNameFirst) {
                val nameParts = parts[0].split(" ")
                firstName = nameParts.getOrNull(0) ?: "Property"
                lastName = nameParts.getOrNull(1) ?: "Owner"
                address = parts.getOrNull(1) ?: "${1000 + (idx * 342)} Main St"
                city = parts.getOrNull(2) ?: sampleCities[idx % sampleCities.size]
                state = parts.getOrNull(3) ?: sampleStates[idx % sampleStates.size]
                zip = parts.getOrNull(4) ?: "750${10 + idx}"
            } else {
                val sampleNames = listOf(
                    Pair("Arthur", "Pendelton"),
                    Pair("Brenda", "Vaughn"),
                    Pair("Charles", "Montgomery"),
                    Pair("Diana", "Kovacs"),
                    Pair("Edward", "Ramsay"),
                    Pair("Fiona", "Gallagher"),
                    Pair("Gerald", "Huxley")
                )
                val chosenName = sampleNames[idx % sampleNames.size]
                firstName = chosenName.first
                lastName = chosenName.second
                address = parts.getOrNull(0) ?: "${2400 + (idx * 115)} Oakridge Way"
                city = parts.getOrNull(1) ?: sampleCities[idx % sampleCities.size]
                state = parts.getOrNull(2) ?: sampleStates[idx % sampleStates.size]
                zip = parts.getOrNull(3) ?: "752${20 + idx}"
            }
            
            val areaCode = when (state) {
                "TX" -> if (idx % 2 == 0) "214" else "469"
                "GA" -> "404"
                "FL" -> "305"
                "AZ" -> "602"
                else -> "555"
            }
            
            val phone1 = "+1 ($areaCode) ${550 + idx}-${1000 + (idx * 43)}"
            val phone2 = if (idx % 3 != 0) "+1 ($areaCode) ${560 + idx}-${2000 + (idx * 67)}" else null
            val phone3 = if (idx % 2 == 0) "+1 ($areaCode) ${570 + idx}-${3000 + (idx * 89)}" else null
            
            val email1 = "${firstName.lowercase()}.${lastName.lowercase()}@gmail.com"
            val email2 = if (idx % 2 == 0) "${firstName.lowercase().first()}${lastName.lowercase()}@outlook.com" else null
            
            SkiptraceResultEntity(
                id = "res_${UUID.randomUUID().toString().take(8)}",
                batchId = batchId,
                inputAddressOrName = inputLine,
                ownerFirstName = firstName,
                ownerLastName = lastName,
                age = 45 + (idx * 3) % 35,
                isDeceased = idx % 9 == 0,
                propertyAddress = address,
                propertyCity = city,
                propertyState = state,
                propertyZip = zip,
                mailingAddress = if (idx % 2 == 0) "$address, $city, $state $zip" else "${500 + idx} Corporate Blvd, Suite 200, $city, $state $zip",
                phone1 = phone1,
                phone1Type = "Mobile",
                phone1Carrier = sampleCarriers[idx % sampleCarriers.size],
                phone1Dnc = idx % 4 == 0,
                phone1Confidence = 95 - (idx % 10),
                phone2 = phone2,
                phone2Type = if (phone2 != null) "Mobile" else null,
                phone2Carrier = if (phone2 != null) sampleCarriers[(idx + 1) % sampleCarriers.size] else null,
                phone2Dnc = false,
                phone3 = phone3,
                phone3Type = if (phone3 != null) "Landline" else null,
                phone3Carrier = if (phone3 != null) "Charter / Spectrum" else null,
                phone3Dnc = true,
                email1 = email1,
                email1Deliverable = true,
                email2 = email2,
                relativeName = "${firstName} Relative / Co-Owner",
                relativePhone = "+1 ($areaCode) ${580 + idx}-${4000 + (idx * 23)}"
            )
        }
        
        skiptraceResultDao.insertResults(generatedResults)
        
        val order = SkiptraceOrderEntity(
            id = batchId,
            recordCount = count,
            priceCents = count * 3,
            status = "completed",
            createdAt = System.currentTimeMillis(),
            resultsCount = generatedResults.size
        )
        skiptraceDao.insertOrder(order)
        
        triggerClientPushNotification(
            title = "🔍 Batch Skip Trace Complete",
            body = "Matched ${generatedResults.size} records with carrier & DNC verified phone numbers.",
            category = "orders",
            targetRoute = "skiptrace"
        )
        
        return generatedResults
    }

    suspend fun logLeadActivity(
        leadId: String,
        leadAddress: String,
        sellerName: String,
        activityType: String,
        title: String,
        description: String,
        actorName: String = "AI Quality Bot",
        actorRole: String = "AI Engine"
    ) {
        val activity = LeadActivityEntity(
            id = "act_" + UUID.randomUUID().toString().take(8),
            leadId = leadId,
            leadAddress = leadAddress,
            sellerName = sellerName,
            activityType = activityType,
            title = title,
            description = description,
            actorName = actorName,
            actorRole = actorRole,
            timestamp = System.currentTimeMillis()
        )
        leadActivityDao.insertActivity(activity)
    }

    suspend fun simulateIncomingLeadAlert(
        title: String,
        body: String,
        category: String = "leads",
        leadAddress: String? = null,
        sellerName: String? = null
    ) {
        val notif = NotificationItem(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = title,
            body = body,
            category = category,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            targetRoute = if (category == "tickets") "tickets" else "leads"
        )
        notificationDao.insertNotification(notif)
        
        if (leadAddress != null && sellerName != null) {
            logLeadActivity(
                leadId = "lead_" + UUID.randomUUID().toString().take(6),
                leadAddress = leadAddress,
                sellerName = sellerName,
                activityType = "lead_intake",
                title = title,
                description = body,
                actorName = "Sarah Santos",
                actorRole = "Cold Caller"
            )
        }
        
        triggerClientPushNotification(
            title = title,
            body = body,
            category = category,
            targetRoute = notif.targetRoute
        )
    }

    suspend fun topUpSkiptraceCredits(credits: Int) {
        accountDao.addCredits(credits)
        val tx = TransactionEntity(
            id = "ch_" + UUID.randomUUID().toString().take(12),
            description = "Skip Tracing Credits Top-Up ($credits credits)",
            amountCents = credits * 3,
            createdAt = System.currentTimeMillis()
        )
        billingDao.insertTransactions(listOf(tx))
        triggerClientPushNotification(
            title = "💳 Credits Added",
            body = "Successfully added $credits Skip Tracing credits to your balance.",
            category = "billing"
        )
    }

    // --- Payment Methods ---
    suspend fun addPaymentMethod(
        brand: String,
        last4: String,
        expMonth: Int,
        expYear: Int,
        isDefault: Boolean = true
    ): PaymentMethodEntity {
        if (isDefault) {
            billingDao.clearDefaultPaymentMethods()
        }
        val pm = PaymentMethodEntity(
            id = "pm_" + UUID.randomUUID().toString().take(8),
            brand = brand.lowercase(),
            last4 = last4,
            expMonth = expMonth,
            expYear = expYear,
            isDefault = isDefault
        )
        billingDao.insertPaymentMethod(pm)
        triggerClientPushNotification(
            title = "💳 Payment Method Added",
            body = "${brand.replaceFirstChar { it.uppercase() }} ending in $last4 has been added to your account.",
            category = "billing",
            targetRoute = "billing"
        )
        return pm
    }

    suspend fun setDefaultPaymentMethod(id: String) {
        billingDao.clearDefaultPaymentMethods()
        billingDao.setDefaultPaymentMethod(id)
        triggerClientPushNotification(
            title = "Default Payment Method Updated",
            body = "Your default payment method for campaign subscriptions has been updated.",
            category = "billing",
            targetRoute = "billing"
        )
    }

    suspend fun deletePaymentMethod(id: String) {
        billingDao.deletePaymentMethod(id)
    }

    // --- Account & Profile Management ---
    suspend fun updateAccountProfile(name: String, company: String, phone: String) {
        accountDao.updateAccountProfile(name, company, phone)
        triggerClientPushNotification(
            title = "Profile Updated",
            body = "Your account details and company information have been saved.",
            category = "leads"
        )
    }

    suspend fun updateNotificationPreferences(
        leads: Boolean,
        tickets: Boolean,
        billing: Boolean,
        orders: Boolean,
        sms: Boolean
    ) {
        accountDao.updateNotificationPreferences(leads, tickets, billing, orders, sms)
    }

    suspend fun updateQuietHoursSettings(settings: QuietHoursSettings) {
        userPreferencesRepository.setQuietHoursSettings(settings)
    }

    // --- Subscription & Caller Management ---
    suspend fun addCaller(subscriptionId: String, tier: String, dialerChoice: String, dataChoice: String) {
        val subs = billingDao.getSubscriptionsFlow()
        // Simple update
        triggerClientPushNotification(
            title = "Caller Added to Campaign",
            body = "New $tier caller with $dialerChoice dialer added. Prorated billing updated.",
            category = "billing"
        )
    }

    suspend fun requestPauseSubscription(subscriptionId: String) {
        val now = System.currentTimeMillis()
        val effective = now + (14L * 24 * 60 * 60 * 1000)
        triggerClientPushNotification(
            title = "Pause Request Scheduled",
            body = "Campaign pause request recorded. Effective in 14 days per policy.",
            category = "billing"
        )
    }

    suspend fun requestCancelSubscription(subscriptionId: String) {
        val now = System.currentTimeMillis()
        val effective = now + (14L * 24 * 60 * 60 * 1000)
        triggerClientPushNotification(
            title = "Cancellation Request Scheduled",
            body = "Campaign cancellation request recorded. Effective in 14 days.",
            category = "billing"
        )
    }

    // --- Notifications & Chat ---
    suspend fun markNotificationRead(id: String) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()
    suspend fun clearNotifications() = notificationDao.clearAll()

    suspend fun sendChatMessage(body: String, isAudio: Boolean = false, audioDuration: Int = 0) {
        val msgId = "msg_" + UUID.randomUUID().toString().take(8)
        val clientMsg = ChatMessage(
            id = msgId,
            sender = "client",
            senderName = "You",
            body = body,
            isAudio = isAudio,
            audioDurationSec = audioDuration,
            createdAt = System.currentTimeMillis()
        )
        chatDao.insertMessage(clientMsg)

        // Generate instant smart Remi AI assistant reply
        scope.launch {
            kotlinx.coroutines.delay(1200)
            val replyText = generateRemiResponse(body)
            val aiMsg = ChatMessage(
                id = "ai_" + UUID.randomUUID().toString().take(8),
                sender = "ai",
                senderName = "Remi",
                body = replyText,
                createdAt = System.currentTimeMillis()
            )
            chatDao.insertMessage(aiMsg)
        }
    }

    private fun generateRemiResponse(userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("underwriting") || lower.contains("arv") || lower.contains("mao") || lower.contains("calculate") || lower.contains("deal") ->
                "📊 **PropTech AI Deal Underwriting Analysis**\n\n" +
                "• **Estimated ARV:** $330,000\n" +
                "• **Estimated Rehab:** $35,000 (Roof, Kitchen, LVP)\n" +
                "• **Wholesale Assignment Fee:** $15,000\n" +
                "• **Calculated MAO (70% Rule):** **$181,000**\n" +
                "• **Seller Asking:** $210,000 (Spread: -$29,000)\n\n" +
                "💡 **Recommendation:** Counter at **$185,000 cash with 10-day close** and inspection contingency. Projected end-buyer ROI is 16.8% with $48,000 net profit margin."

            lower.contains("cold call") || lower.contains("script") || lower.contains("pitch") || lower.contains("hook") ->
                "📞 **High-Converting Cold Call Script (Distressed / Absentee Owner)**\n\n" +
                "**Opening Hook:**\n\"Hi [Seller Name], my name is Alex with Apex Partners. I was looking at properties in [City/Neighborhood] and noticed your house on [Address]. I wanted to see if you'd consider a fair all-cash offer if the price and timeline made sense for you?\"\n\n" +
                "**If they ask 'What's your offer?':**\n\"We buy completely as-is and cover 100% of closing costs with zero agent commissions. Based on recent neighborhood sales, we typically range between $[Low] and $[High] depending on current condition. If we could close within 14 days, what number would you need to walk away with at closing?\"\n\n" +
                "**Next Step:** Lock in a 10-minute walkthrough inspection."

            lower.contains("wholesale") || lower.contains("contract") || lower.contains("clause") || lower.contains("assignment") ->
                "📝 **Standard Wholesale Assignment & Contingency Clauses**\n\n" +
                "1. **Assignment Clause:**\n\"Buyer has the unqualified right to assign this Contract and all rights hereunder to any third-party individual or entity without requiring Seller's prior consent, provided that Assignee assumes all Buyer obligations.\"\n\n" +
                "2. **Due Diligence Contingency:**\n\"Buyer's obligation is contingent upon a satisfactory 14-day property inspection and title verification. In the event of disapproval, Buyer may cancel with 100% return of Earnest Money ($1,000.00).\"\n\n" +
                "3. **Closing Terms:** As-is condition, Seller conveys clear marketable general warranty deed."

            lower.contains("sms") || lower.contains("drip") || lower.contains("nurture") || lower.contains("follow-up") ->
                "💬 **4-Step SMS Revival Drip Sequence**\n\n" +
                "• **Day 1:** \"Hi [Name], Alex here. Just following up on [Address]—are you still open to a cash offer this month?\"\n" +
                "• **Day 3:** \"Hey [Name], 2 cash buyers just closed nearby on [Street Name]. We have active capital ready to deploy for [Address]. Let me know if 5 mins works to connect!\"\n" +
                "• **Day 7:** \"Hi [Name], if price was the hurdle, we can offer flexible terms or leaseback options. Open to a quick brainstorm?\"\n" +
                "• **Day 14 (Revival):** \"Final check-in regarding [Address] before we reallocate our acquisition funds for the quarter. Should I close your file?\""

            lower.contains("cma") || lower.contains("comp") || lower.contains("market analysis") || lower.contains("appraisal") ->
                "📈 **Comparative Market Analysis (CMA) Summary**\n\n" +
                "• **Subject:** 3 Bed / 2 Bath · 1,750 sqft\n" +
                "• **Comp #1 (0.2 mi):** 1,800 sqft, Renovated -> Sold **$342,000** ($190/sqft)\n" +
                "• **Comp #2 (0.4 mi):** 1,700 sqft, Original -> Sold **$280,000** ($164/sqft)\n" +
                "• **Comp #3 (0.3 mi):** 1,775 sqft, Partial Rehab -> Sold **$315,000** ($177/sqft)\n\n" +
                "📊 **Adjusted Subject ARV:** **$325,000 - $335,000**\nMedian DOM: 18 days. Inventory trend is tight (1.8 months supply)."

            lower.contains("rehab") || lower.contains("repair") || lower.contains("estimate") || lower.contains("cost") ->
                "🔨 **Itemized Rehab & Repair Cost Estimate (2,100 sqft)**\n\n" +
                "• **Roof (Architectural Shingles - 25 sq):** $8,500\n" +
                "• **HVAC (4-Ton 16 SEER Complete System):** $7,200\n" +
                "• **Kitchen (Shaker Cabinets, Quartz, Stainless Appliances):** $9,500\n" +
                "• **Bathrooms (2x Modern Vanities, Tile, Fixtures):** $6,000\n" +
                "• **Flooring (LVP throughout & Baseboards):** $5,800\n" +
                "• **Interior/Exterior Paint:** $4,500\n" +
                "• **Contingency & Permits (10%):** $4,150\n\n" +
                "💰 **Total Estimated Rehab:** **$45,650**"

            lower.contains("dispute") || lower.contains("wrong") ->
                "You can dispute any lead within 2 US business days directly from the lead detail card! Our quality control team reviews every dispute thoroughly."
            lower.contains("skip trace") || lower.contains("credit") ->
                "Skip tracing costs 1 credit ($0.03) per record. You currently have 2,450 credits in your wallet balance."
            lower.contains("list") || lower.contains("niche") ->
                "You can order custom niche lists (Absentee Owners, High Equity, Pre-foreclosures) under the 'Buy a list' tab for $0.04 per record."
            lower.contains("human") || lower.contains("agent") || lower.contains("speak") || lower.contains("caller") ->
                "I've alerted your dedicated Account Manager. You can also tap the phone icon at the top to start an instant voice call or screen share!"
            lower.contains("billing") || lower.contains("invoice") || lower.contains("receipt") ->
                "All your invoices, caller staffing groups, and Stripe receipts are accessible in the Billing tab."
            else ->
                "Hello! I'm Remi, your PropTech AI assistant. Choose any template above (Deal Underwriting, Cold Call Scripts, Wholesale Contracts, SMS Drips, CMA Comps) or ask me any real estate question!"
        }
    }

    // --- Seed Initial Realistic Data ---
    private suspend fun seedInitialDataIfNeeded() {
        if (leadDao.getLeadCount() > 0) return

        // 1. Account
        accountDao.insertOrUpdate(
            ClientAccount(
                id = "client_user_01",
                email = "01015523142az@gmail.com",
                fullName = "Alex Morgan",
                companyName = "Apex Real Estate Partners",
                phone = "+1 (555) 234-8921",
                accountType = "full_service",
                biometricEnabled = true,
                skiptraceCredits = 2450,
                creditBalanceCents = 7350,
                lastLoginAt = System.currentTimeMillis() - 7200000
            )
        )

        val now = System.currentTimeMillis()
        val hour = 3600000L
        val day = 86400000L

        // 2. Leads
        val sampleLeads = listOf(
            LeadEntity(
                id = "lead_01",
                sellerName = "David Henderson",
                sellerPhone = "+1 (469) 555-8312",
                campaignId = "camp_01",
                campaignName = "Dallas Distressed Sellers",
                status = "new",
                propertyAddress = "4812 Meadowbrook Dr",
                propertyCity = "Dallas",
                propertyState = "TX",
                propertyZip = "75227",
                askingPrice = "$165,000",
                marketValue = "$230,000",
                marketValueSource = "Zillow & Redfin ($235k / $228k)",
                whySell = "Relocating out of state for work; wants quick cash closing",
                reasonSell = "Job transfer to Denver by end of month. House has older roof and needs new HVAC.",
                whenSell = "As soon as possible (under 30 days)",
                notes = "Owner confirmed clear title. Flexible on closing date if earnest money deposit is placed.",
                submittedAt = now - 2 * hour,
                reviewedAt = now - 1 * hour,
                hasRecording = true,
                audioDurationSec = 195,
                aiSummary = "High motivation. Asking price is 28% below Zillow ARV. Clear title, vacant within 3 weeks."
            ),
            LeadEntity(
                id = "lead_02",
                sellerName = "Eleanor Vance",
                sellerPhone = "+1 (404) 555-4920",
                campaignId = "camp_02",
                campaignName = "Atlanta Off-Market Inbound",
                status = "new",
                propertyAddress = "1138 Cascade Rd SW",
                propertyCity = "Atlanta",
                propertyState = "GA",
                propertyZip = "30311",
                askingPrice = "$140,000",
                marketValue = "$195,000",
                marketValueSource = "BatchLeads & Redfin",
                whySell = "Tired landlord dealing with non-paying tenant",
                reasonSell = "Tenant moving out next week; tired of property management headaches.",
                whenSell = "Within 45 days",
                notes = "Brick ranch 3/2 with detached garage. Needs cosmetic interior paint and flooring.",
                submittedAt = now - 14 * hour,
                reviewedAt = now - 12 * hour,
                hasRecording = true,
                audioDurationSec = 142,
                aiSummary = "Motivated tired landlord. Asking price 28% below market. Good rental yield profile."
            ),
            LeadEntity(
                id = "lead_03",
                sellerName = "Marcus Sterling",
                sellerPhone = "+1 (602) 555-7119",
                campaignId = "camp_03",
                campaignName = "Phoenix High-Equity Acquisitions",
                status = "accepted",
                propertyAddress = "3820 E Thomas Rd",
                propertyCity = "Phoenix",
                propertyState = "AZ",
                propertyZip = "85018",
                askingPrice = "$285,000",
                marketValue = "$375,000",
                marketValueSource = "Zillow Zestimate",
                whySell = "Inherited estate property, dividing proceeds between siblings",
                reasonSell = "Probate cleared. Property is vacant and ready for immediate walk-through.",
                whenSell = "14 - 30 days",
                notes = "All heirs agreed on price. Roof replaced in 2021. Excellent flip opportunity.",
                submittedAt = now - 3 * day,
                reviewedAt = now - 3 * day + hour,
                hasRecording = true,
                audioDurationSec = 230,
                aiSummary = "Probate property with probate concluded. All 3 heirs on board for cash liquidation."
            ),
            LeadEntity(
                id = "lead_04",
                sellerName = "Carmen Ortiz",
                sellerPhone = "+1 (305) 555-9034",
                campaignId = "camp_04",
                campaignName = "Miami Cash Buyers Direct",
                status = "accepted",
                propertyAddress = "720 NW 45th St",
                propertyCity = "Miami",
                propertyState = "FL",
                propertyZip = "33127",
                askingPrice = "$310,000",
                marketValue = "$420,000",
                marketValueSource = "MLS Comps / Redfin",
                whySell = "Downsizing to smaller condo",
                reasonSell = "Older couple seeking hassle-free sale without listing on MLS.",
                whenSell = "Flexible 30-60 days",
                notes = "Prime Wynwood corridor proximity. Highly sought-after lot size.",
                submittedAt = now - 6 * day,
                reviewedAt = now - 6 * day + 2 * hour,
                hasRecording = true,
                audioDurationSec = 175,
                aiSummary = "Solid margin potential. Off-market direct to seller with no realtor fees required."
            ),
            LeadEntity(
                id = "lead_05",
                sellerName = "Gary Patterson",
                sellerPhone = "+1 (214) 555-6671",
                campaignId = "camp_01",
                campaignName = "Dallas Distressed Sellers",
                status = "accepted",
                propertyAddress = "914 S Beacon St",
                propertyCity = "Dallas",
                propertyState = "TX",
                propertyZip = "75223",
                askingPrice = "$195,000",
                marketValue = "$260,000",
                marketValueSource = "Zillow & Realtor.com",
                whySell = "Behind on property taxes",
                reasonSell = "Wants to pay off tax lien before auction date in 60 days.",
                whenSell = "30 days",
                notes = "Tax lien payoff amount is $8,400. Seller willing to credit at closing.",
                submittedAt = now - 9 * day,
                reviewedAt = now - 9 * day + hour,
                hasRecording = true,
                audioDurationSec = 160,
                aiSummary = "Urgent tax delinquent motivation. Fast contract turnaround recommended."
            )
        )
        leadDao.insertLeads(sampleLeads)

        // 3. Tickets
        val sampleTickets = listOf(
            TicketEntity(
                id = "tkt_01",
                campaignName = "Dallas Distressed Sellers",
                propertyAddress = "2918 Glenfield Ave, Dallas, TX 75233",
                askingPrice = "$175,000",
                marketValue = "$240,000",
                whySell = "Divorce settlement requirement",
                whenSell = "Within 45 days",
                notes = "Both parties agree to sell off-market to avoid open houses.",
                submittedAt = now - 3 * hour
            ),
            TicketEntity(
                id = "tkt_02",
                campaignName = "Atlanta Off-Market Inbound",
                propertyAddress = "2408 Donald Lee Hollowell Pkwy, Atlanta, GA 30318",
                askingPrice = "$135,000",
                marketValue = "$190,000",
                whySell = "Pre-foreclosure notice received",
                whenSell = "Under 3 weeks",
                notes = "Auction date is in 4 weeks. Seller owes $112k on mortgage.",
                submittedAt = now - 8 * hour
            )
        )
        ticketDao.insertTickets(sampleTickets)

        // 3b. Support Inquiries & Tickets
        val sampleSupportTickets = listOf(
            SupportTicketEntity(
                id = "supp_01",
                ticketNumber = "TKT-8412",
                subject = "Requesting Lead Delivery Throttle Adjustment",
                category = "Campaign Settings",
                priority = "High",
                description = "Our underwriting acquisitions team is receiving 4+ leads per day. Can we cap daily deliveries to 2 high-motivation leads per day for Dallas County?",
                status = "In Progress",
                relatedLeadId = null,
                createdAt = now - 1 * day - 4 * hour,
                updatedAt = now - 2 * hour,
                lastStaffReply = "Sarah M: We've adjusted your dialer routing cap to 2 verified contracts/day. Changes will take effect at 8:00 AM CST tomorrow."
            ),
            SupportTicketEntity(
                id = "supp_02",
                ticketNumber = "TKT-8429",
                subject = "Skip Trace Credits Replenishment Confirmation",
                category = "Billing & Invoices",
                priority = "Normal",
                description = "We purchased 5,000 skip trace credits yesterday. Wanted to confirm if the bulk tier bonus credits were credited to our account ledger.",
                status = "Resolved",
                relatedLeadId = null,
                createdAt = now - 3 * day,
                updatedAt = now - 1 * day,
                lastStaffReply = "Billing Support: Confirmed! 5,000 credits plus 500 bonus promotional credits have been added to your credit balance."
            ),
            SupportTicketEntity(
                id = "supp_03",
                ticketNumber = "TKT-8450",
                subject = "Audio Transcript Clarification - 4812 Meadowbrook",
                category = "Lead Quality",
                priority = "Urgent",
                description = "On lead #lead_01 (Henderson), seller mentions a second mortgage payoff. Could the quality manager check if cold caller verified clear title?",
                status = "Open",
                relatedLeadId = "lead_01",
                createdAt = now - 45 * 60 * 1000L,
                updatedAt = now - 45 * 60 * 1000L,
                lastStaffReply = null
            )
        )
        supportTicketDao.insertSupportTickets(sampleSupportTickets)

        // Initial messages for support tickets
        supportTicketDao.insertMessage(
            SupportTicketMessageEntity(
                id = "supp_msg_01",
                ticketId = "supp_01",
                senderName = "Alex Morgan",
                senderRole = "client",
                message = "Our underwriting acquisitions team is receiving 4+ leads per day. Can we cap daily deliveries to 2 high-motivation leads per day for Dallas County?",
                createdAt = now - 1 * day - 4 * hour
            )
        )
        supportTicketDao.insertMessage(
            SupportTicketMessageEntity(
                id = "supp_msg_02",
                ticketId = "supp_01",
                senderName = "Sarah M. (Campaign Ops)",
                senderRole = "staff",
                message = "We've adjusted your dialer routing cap to 2 verified contracts/day. Changes will take effect at 8:00 AM CST tomorrow.",
                createdAt = now - 2 * hour
            )
        )

        // 4. Campaign Guides
        val guides = listOf(
            CampaignGuideItem(campaignNormName = "dallas_distressed", campaignDisplayName = "Dallas Distressed Sellers", label = "Target Price Discount", value = "20% - 35% below Zillow Market Value"),
            CampaignGuideItem(campaignNormName = "dallas_distressed", campaignDisplayName = "Dallas Distressed Sellers", label = "Property Types", value = "Single Family (1-4 units), Townhomes"),
            CampaignGuideItem(campaignNormName = "dallas_distressed", campaignDisplayName = "Dallas Distressed Sellers", label = "Owner Occupied or Vacant", value = "Accept both; must have clear title authority"),
            CampaignGuideItem(campaignNormName = "dallas_distressed", campaignDisplayName = "Dallas Distressed Sellers", label = "Closing Timeline", value = "Maximum 60 days"),
            CampaignGuideItem(campaignNormName = "atlanta_inbound", campaignDisplayName = "Atlanta Off-Market Inbound", label = "Target Price Discount", value = "25% minimum discount on estimated ARV"),
            CampaignGuideItem(campaignNormName = "atlanta_inbound", campaignDisplayName = "Atlanta Off-Market Inbound", label = "Listed on MLS", value = "Strictly off-market only (no active MLS listings)")
        )
        campaignGuideDao.insertGuideItems(guides)

        // 5. List Orders
        val sampleListOrders = listOf(
            ListOrderEntity(
                id = "list_01",
                typeOfLeads = "Absentee Owner, High Equity",
                propertyType = "Single Family Home",
                targetMarket = "Dallas County, TX",
                otherDetails = "Exclude properties owned less than 3 years.",
                recordCount = 5000,
                priceCents = 20000,
                status = "fulfilled",
                fileName = "Dallas_Absentee_5k_Cleaned.csv",
                fileUrl = "https://example.com/dallas_5k.csv",
                createdAt = now - 4 * day
            ),
            ListOrderEntity(
                id = "list_02",
                typeOfLeads = "Tired Landlord, Pre-foreclosure",
                propertyType = "Multi- Family Home 2-4",
                targetMarket = "Fulton County, GA",
                otherDetails = "Target zip codes 30311, 30318, 30310",
                recordCount = 2500,
                priceCents = 10000,
                status = "pending",
                createdAt = now - 1 * day
            )
        )
        for (order in sampleListOrders) {
            listOrderDao.insertOrder(order)
        }

        // 6. Skip Trace Orders
        val sampleSkiptrace = listOf(
            SkiptraceOrderEntity(
                id = "skp_101",
                recordCount = 500,
                priceCents = 1500,
                status = "completed",
                createdAt = now - 2 * day,
                resultsCount = 472
            ),
            SkiptraceOrderEntity(
                id = "skp_102",
                recordCount = 1200,
                priceCents = 3600,
                status = "completed",
                createdAt = now - 5 * day,
                resultsCount = 1140
            )
        )
        for (order in sampleSkiptrace) {
            skiptraceDao.insertOrder(order)
        }

        // 7. Subscriptions & Billing
        val sampleSubs = listOf(
            SubscriptionEntity(
                id = "sub_01",
                campaignDisplayName = "Dallas Distressed Sellers",
                callerTier = "full_time",
                partTimeCallers = 0,
                fullTimeCallers = 1,
                dialerChoice = "ours",
                dataChoice = "ours",
                status = "active",
                currentPeriodEnd = now + 18 * day,
                monthlyAmountCents = 150000 // $900 caller + $200 dialer + $400 data
            ),
            SubscriptionEntity(
                id = "sub_02",
                campaignDisplayName = "Atlanta Off-Market Inbound",
                callerTier = "part_time",
                partTimeCallers = 1,
                fullTimeCallers = 0,
                dialerChoice = "ours",
                dataChoice = "own",
                status = "active",
                currentPeriodEnd = now + 24 * day,
                monthlyAmountCents = 70000 // $500 caller + $200 dialer
            )
        )
        billingDao.insertSubscriptions(sampleSubs)

        // Invoices
        val sampleInvoices = listOf(
            InvoiceEntity(
                id = "inv_01",
                invoiceNumber = "INV-2026-0842",
                description = "Monthly Campaign Subscription - Dallas Distressed (1 Full-Time Caller + Dialer + Data)",
                amountCents = 150000,
                status = "paid",
                createdAt = now - 12 * day
            ),
            InvoiceEntity(
                id = "inv_02",
                invoiceNumber = "INV-2026-0791",
                description = "Monthly Campaign Subscription - Atlanta Inbound (1 Part-Time Caller + Dialer)",
                amountCents = 70000,
                status = "paid",
                createdAt = now - 6 * day
            )
        )
        billingDao.insertInvoices(sampleInvoices)

        // Transactions
        val sampleTxs = listOf(
            TransactionEntity(
                id = "tx_01",
                description = "Dallas Distressed Monthly Subscription",
                amountCents = 150000,
                status = "succeeded",
                createdAt = now - 12 * day
            ),
            TransactionEntity(
                id = "tx_02",
                description = "Skip Trace Credits Top-Up (2,000 credits)",
                amountCents = 6000,
                status = "succeeded",
                createdAt = now - 8 * day
            )
        )
        billingDao.insertTransactions(sampleTxs)

        // Payment Method
        billingDao.insertPaymentMethod(
            PaymentMethodEntity(
                id = "pm_01",
                brand = "visa",
                last4 = "4242",
                expMonth = 12,
                expYear = 2028,
                isDefault = true
            )
        )

        // 8. Notifications
        val sampleNotifs = listOf(
            NotificationItem(
                id = "notif_01",
                title = "🎯 New Lead Accepted",
                body = "David Henderson at 4812 Meadowbrook Dr, Dallas, TX was qualified and delivered.",
                category = "leads",
                timestamp = now - 2 * hour,
                isRead = false,
                targetRoute = "leads"
            ),
            NotificationItem(
                id = "notif_02",
                title = "🎟️ Deal Review Ticket Waiting",
                body = "New ticket on Glenfield Ave is waiting for your review. Accept to unlock contact details.",
                category = "tickets",
                timestamp = now - 3 * hour,
                isRead = false,
                targetRoute = "tickets"
            ),
            NotificationItem(
                id = "notif_03",
                title = "🚀 Data List Ready",
                body = "Your 5,000 record Absentee Owner list for Dallas County is ready for download.",
                category = "orders",
                timestamp = now - 1 * day,
                isRead = true,
                targetRoute = "buylist"
            )
        )
        for (notif in sampleNotifs) {
            notificationDao.insertNotification(notif)
        }

        // 9. Chat initial welcome
        val welcomeMsg = ChatMessage(
            id = "msg_init",
            sender = "ai",
            senderName = "Remi",
            body = "Hi Alex! 👋 I'm Remi, your Proptech AI assistant. I'm here 24/7 to help you track your campaigns, manage leads, order niche data lists, or connect directly with our quality team. How can I help today?",
            createdAt = now - 1 * hour
        )
        chatDao.insertMessage(welcomeMsg)

        // 10. Real-time Lead Activities
        val sampleActivities = listOf(
            LeadActivityEntity(
                id = "act_01",
                leadId = "lead_01",
                leadAddress = "4812 Meadowbrook Dr, Dallas, TX",
                sellerName = "David Henderson",
                activityType = "lead_intake",
                title = "New Motivated Seller Delivered",
                description = "Cold caller Maria Santos qualified seller. Asking $165,000 (28% below Zillow ARV).",
                actorName = "Maria Santos",
                actorRole = "Cold Caller",
                timestamp = now - 2 * hour
            ),
            LeadActivityEntity(
                id = "act_02",
                leadId = "lead_01",
                leadAddress = "4812 Meadowbrook Dr, Dallas, TX",
                sellerName = "David Henderson",
                activityType = "recording_ready",
                title = "Call Audio & Transcript Analyzed",
                description = "AI Quality Auditor generated 3-minute transcript and verified motivation rating (Score: 9.4/10).",
                actorName = "AI Quality Engine",
                actorRole = "AI Engine",
                timestamp = now - 1 * hour - 45 * 60 * 1000L
            ),
            LeadActivityEntity(
                id = "act_03",
                leadId = "lead_02",
                leadAddress = "1138 Cascade Rd SW, Atlanta, GA",
                sellerName = "Eleanor Vance",
                activityType = "status_change",
                title = "Lead Status: Under Client Review",
                description = "Tired landlord lead routed to client review queue. Asking price $140,000.",
                actorName = "Sarah Mitchell",
                actorRole = "Quality Lead",
                timestamp = now - 14 * hour
            ),
            LeadActivityEntity(
                id = "act_04",
                leadId = "lead_03",
                leadAddress = "3820 E Thomas Rd, Phoenix, AZ",
                sellerName = "Marcus Sterling",
                activityType = "status_change",
                title = "Lead Accepted by Client",
                description = "Client accepted probate estate lead. Contract underwriting initiated.",
                actorName = "Alex Morgan",
                actorRole = "Client",
                timestamp = now - 3 * day
            ),
            LeadActivityEntity(
                id = "act_05",
                leadId = "lead_04",
                leadAddress = "720 NW 45th St, Miami, FL",
                sellerName = "Carmen Ortiz",
                activityType = "skip_traced",
                title = "Skip Trace Data Verified",
                description = "3 wireless phone numbers and verified deed ownership confirmed via LexisNexis.",
                actorName = "Data Operations",
                actorRole = "System",
                timestamp = now - 6 * day
            )
        )
        leadActivityDao.insertActivities(sampleActivities)

        // 11. Initial Skip Trace Results
        val initialSkiptraceResults = listOf(
            SkiptraceResultEntity(
                id = "res_init_01",
                batchId = "skp_101",
                inputAddressOrName = "4812 Meadowbrook Dr, Dallas, TX 75227",
                ownerFirstName = "David",
                ownerLastName = "Henderson",
                age = 52,
                isDeceased = false,
                propertyAddress = "4812 Meadowbrook Dr",
                propertyCity = "Dallas",
                propertyState = "TX",
                propertyZip = "75227",
                mailingAddress = "4812 Meadowbrook Dr, Dallas, TX 75227",
                phone1 = "+1 (469) 555-8312",
                phone1Type = "Mobile",
                phone1Carrier = "Verizon Wireless",
                phone1Dnc = false,
                phone1Confidence = 98,
                phone2 = "+1 (214) 555-4921",
                phone2Type = "Mobile",
                phone2Carrier = "AT&T Mobility",
                phone2Dnc = false,
                phone3 = "+1 (214) 555-1088",
                phone3Type = "Landline",
                phone3Carrier = "Spectrum",
                phone3Dnc = true,
                email1 = "d.henderson52@gmail.com",
                email1Deliverable = true,
                email2 = "dhenderson@txrealty.net",
                relativeName = "Sarah Henderson (Spouse)",
                relativePhone = "+1 (469) 555-9014"
            ),
            SkiptraceResultEntity(
                id = "res_init_02",
                batchId = "skp_101",
                inputAddressOrName = "1138 Cascade Rd SW, Atlanta, GA 30311",
                ownerFirstName = "Eleanor",
                ownerLastName = "Vance",
                age = 64,
                isDeceased = false,
                propertyAddress = "1138 Cascade Rd SW",
                propertyCity = "Atlanta",
                propertyState = "GA",
                propertyZip = "30311",
                mailingAddress = "405 Peachtree St NE, Suite 400, Atlanta, GA 30308",
                phone1 = "+1 (404) 555-4920",
                phone1Type = "Mobile",
                phone1Carrier = "T-Mobile USA",
                phone1Dnc = false,
                phone1Confidence = 96,
                phone2 = "+1 (404) 555-8120",
                phone2Type = "Landline",
                phone2Carrier = "AT&T",
                phone2Dnc = false,
                phone3 = null,
                email1 = "eleanor.vance@bellsouth.net",
                email1Deliverable = true,
                email2 = null,
                relativeName = "Thomas Vance (Son)",
                relativePhone = "+1 (404) 555-3319"
            ),
            SkiptraceResultEntity(
                id = "res_init_03",
                batchId = "skp_101",
                inputAddressOrName = "3820 E Thomas Rd, Phoenix, AZ 85018",
                ownerFirstName = "Marcus",
                ownerLastName = "Sterling",
                age = 47,
                isDeceased = false,
                propertyAddress = "3820 E Thomas Rd",
                propertyCity = "Phoenix",
                propertyState = "AZ",
                propertyZip = "85018",
                mailingAddress = "1202 N Central Ave, Phoenix, AZ 85004",
                phone1 = "+1 (602) 555-7119",
                phone1Type = "Mobile",
                phone1Carrier = "Verizon Wireless",
                phone1Dnc = false,
                phone1Confidence = 99,
                phone2 = "+1 (602) 555-9430",
                phone2Type = "Mobile",
                phone2Carrier = "AT&T Mobility",
                phone2Dnc = false,
                phone3 = "+1 (480) 555-2201",
                phone3Type = "VoIP",
                phone3Carrier = "Vonage",
                phone3Dnc = false,
                email1 = "marcus.sterling@sterlingholdings.com",
                email1Deliverable = true,
                email2 = "msterling@yahoo.com",
                relativeName = "Rachel Sterling (Co-Heir)",
                relativePhone = "+1 (602) 555-8840"
            )
        )
        skiptraceResultDao.insertResults(initialSkiptraceResults)
    }

    companion object {
        const val CHANNEL_LEADS = "channel_leads"
        const val CHANNEL_TICKETS = "channel_tickets"
        const val CHANNEL_BILLING = "channel_billing"
        const val CHANNEL_ORDERS = "channel_orders"
        const val CHANNEL_SILENT = "channel_silent"
    }
}
