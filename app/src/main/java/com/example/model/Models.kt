package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- Client Account & Session ---
@Entity(tableName = "client_account")
data class ClientAccount(
    @PrimaryKey val id: String = "client_user_01",
    val email: String = "01015523142az@gmail.com",
    val fullName: String = "Alex Morgan",
    val companyName: String = "Apex Real Estate Partners",
    val phone: String = "+1 555-234-8921",
    val accountType: String = "full_service", // "full_service" or "skiptrace"
    val mustChangePassword: Boolean = false,
    val biometricEnabled: Boolean = true,
    val notifyLeads: Boolean = true,
    val notifyTickets: Boolean = true,
    val notifyBilling: Boolean = true,
    val notifyOrders: Boolean = true,
    val notifySms: Boolean = false,
    val skiptraceCredits: Int = 2450,
    val creditBalanceCents: Int = 7350,
    val lastLoginAt: Long = System.currentTimeMillis()
)

// --- Lead Entity ---
@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val id: String,
    val sellerName: String,
    val sellerPhone: String,
    val campaignId: String,
    val campaignName: String,
    val status: String, // "new" or "accepted"
    val propertyAddress: String,
    val propertyCity: String,
    val propertyState: String,
    val propertyZip: String,
    val askingPrice: String,
    val marketValue: String,
    val marketValueSource: String = "Zillow & Redfin",
    val whySell: String,
    val reasonSell: String,
    val whenSell: String = "Within 30-60 days",
    val notes: String = "",
    val submittedAt: Long,
    val reviewedAt: Long? = null,
    val recordingUrl: String? = null,
    val hasRecording: Boolean = true,
    val audioDurationSec: Int = 145,
    val aiSummary: String? = null,
    val disputeStatus: String? = null, // null, "open", "accepted", "rejected"
    val feedbackStatus: String? = null, // null, "open", "closed"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun getResolvedCoordinates(): Pair<Double, Double> {
        if (latitude != 0.0 && longitude != 0.0) {
            return Pair(latitude, longitude)
        }
        val baseCoords = when (propertyCity.lowercase().trim()) {
            "dallas" -> Pair(32.7767, -96.7970)
            "atlanta" -> Pair(33.7490, -84.3880)
            "phoenix" -> Pair(33.4484, -112.0740)
            "tampa" -> Pair(27.9506, -82.4572)
            "houston" -> Pair(29.7604, -95.3698)
            "charlotte" -> Pair(35.2271, -80.8431)
            "chicago" -> Pair(41.8781, -87.6298)
            "memphis" -> Pair(35.1495, -90.0490)
            "cleveland" -> Pair(41.4993, -81.6944)
            "indianapolis" -> Pair(39.7684, -86.1581)
            "kansas city" -> Pair(39.0997, -94.5786)
            "philadelphia" -> Pair(39.9526, -75.1652)
            "jacksonville" -> Pair(30.3322, -81.6557)
            "nashville" -> Pair(36.1627, -86.7816)
            "san antonio" -> Pair(29.4241, -98.4936)
            "las vegas" -> Pair(36.1699, -115.1398)
            "orlando" -> Pair(28.5383, -81.3792)
            "st. louis", "st louis" -> Pair(38.6270, -90.1994)
            "detroit" -> Pair(42.3314, -83.0458)
            "columbus" -> Pair(39.9612, -82.9988)
            "austin" -> Pair(30.2672, -97.7431)
            "baltimore" -> Pair(39.2904, -76.6122)
            "miami" -> Pair(25.7617, -80.1918)
            "denver" -> Pair(39.7392, -104.9903)
            else -> when (propertyState.uppercase().trim()) {
                "TX" -> Pair(31.9686, -99.9018)
                "GA" -> Pair(32.1656, -82.9001)
                "AZ" -> Pair(34.0489, -111.0937)
                "FL" -> Pair(27.6648, -81.5158)
                "NC" -> Pair(35.7596, -79.0193)
                "IL" -> Pair(40.6331, -89.3985)
                "TN" -> Pair(35.5175, -86.5804)
                "OH" -> Pair(40.4173, -82.9071)
                "IN" -> Pair(40.2672, -86.1349)
                "MO" -> Pair(37.9643, -91.8318)
                "PA" -> Pair(41.2033, -77.1945)
                else -> Pair(39.8283, -98.5795) // Center of US
            }
        }
        // Add subtle deterministic jitter based on lead id hash so markers don't overlap completely
        val hash = (id.hashCode().and(0x7FFFFFFF) % 100) - 50
        val jitterLat = hash * 0.00045
        val jitterLng = ((id.reversed().hashCode().and(0x7FFFFFFF) % 100) - 50) * 0.00045
        return Pair(baseCoords.first + jitterLat, baseCoords.second + jitterLng)
    }
}

// --- Deal Review Ticket ---
@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val campaignName: String,
    val propertyAddress: String,
    val askingPrice: String,
    val marketValue: String,
    val marketValueSource: String = "Zillow / Redfin Average",
    val whySell: String,
    val whenSell: String,
    val notes: String,
    val submittedAt: Long,
    val status: String = "client_review" // "client_review", "accepted", "declined"
)

// --- Support Inquiry & Ticket Entity ---
@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val ticketNumber: String, // e.g. "TKT-8412"
    val subject: String,
    val category: String, // "Lead Quality", "Billing & Invoices", "Skip Tracing", "Campaign Settings", "Technical Issue", "General Support"
    val priority: String = "Normal", // "Low", "Normal", "High", "Urgent"
    val description: String,
    val status: String = "Open", // "Open", "In Progress", "Resolved", "Closed"
    val relatedLeadId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastStaffReply: String? = null
)

@Entity(tableName = "support_ticket_messages")
data class SupportTicketMessageEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val senderName: String,
    val senderRole: String, // "client" or "staff"
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)

// --- Dispute & Feedback Threads ---
@Entity(tableName = "dispute_threads")
data class DisputeThreadEntity(
    @PrimaryKey val id: String,
    val leadId: String,
    val sellerName: String,
    val sellerPhone: String,
    val type: String, // "dispute" or "feedback"
    val reason: String,
    val status: String, // "open", "accepted", "rejected", "closed"
    val createdAt: Long
)

@Entity(tableName = "dispute_messages")
data class DisputeMessageEntity(
    @PrimaryKey val id: String,
    val disputeId: String,
    val senderType: String, // "client" or "staff"
    val senderName: String,
    val message: String,
    val createdAt: Long
)

// --- Campaign Qualification Guide ---
@Entity(tableName = "campaign_guide_items")
data class CampaignGuideItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignNormName: String,
    val campaignDisplayName: String,
    val label: String,
    val value: String,
    val isPendingApproval: Boolean = false,
    val proposedValue: String? = null
)

// --- Buy a List Order ---
@Entity(tableName = "list_orders")
data class ListOrderEntity(
    @PrimaryKey val id: String,
    val typeOfLeads: String,
    val propertyType: String,
    val targetMarket: String,
    val otherDetails: String,
    val recordCount: Int,
    val priceCents: Int,
    val status: String, // "pending_payment", "pending", "fulfilled"
    val fileName: String? = null,
    val fileUrl: String? = null,
    val createdAt: Long
)

// --- Skip Trace Orders & History ---
@Entity(tableName = "skiptrace_orders")
data class SkiptraceOrderEntity(
    @PrimaryKey val id: String,
    val recordCount: Int,
    val priceCents: Int,
    val status: String, // "processing", "completed", "failed"
    val createdAt: Long,
    val resultsCount: Int = 0,
    val error: String? = null
)

data class SkiptraceMatchRecord(
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val phone1: String,
    val phone1Type: String = "Mobile",
    val phone2: String = "",
    val email1: String = "",
    val deceased: Boolean = false
)

// --- Subscriptions & Billing ---
@Entity(tableName = "campaign_subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val campaignDisplayName: String,
    val callerTier: String, // "part_time" or "full_time"
    val partTimeCallers: Int = 0,
    val fullTimeCallers: Int = 1,
    val dialerChoice: String = "ours", // "ours" or "own"
    val dataChoice: String = "ours", // "ours" or "own"
    val status: String = "active", // "active", "past_due", "pause_scheduled", "paused", "cancel_scheduled", "canceled"
    val currentPeriodEnd: Long,
    val monthlyAmountCents: Int,
    val unpaidAmountCents: Int = 0,
    val pendingActionType: String? = null, // "pause", "cancel"
    val pendingEffectiveAt: Long? = null
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val description: String,
    val amountCents: Int,
    val status: String, // "paid", "open", "void"
    val createdAt: Long,
    val pdfUrl: String? = null
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amountCents: Int,
    val status: String = "succeeded",
    val refunded: Boolean = false,
    val receiptUrl: String? = null,
    val createdAt: Long
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val brand: String, // "visa", "mastercard", "amex"
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean = true
)

// --- Push Client Notifications ---
@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String, // "leads", "tickets", "billing", "orders", "chat"
    val iconType: String = "lead",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetRoute: String? = null
)

// --- Lead Real-Time Activity Feed ---
@Entity(tableName = "lead_activities")
data class LeadActivityEntity(
    @PrimaryKey val id: String,
    val leadId: String,
    val leadAddress: String,
    val sellerName: String,
    val activityType: String, // "lead_intake", "status_change", "call_completed", "recording_ready", "dispute_update", "skip_traced", "notes_added", "voice_note"
    val title: String,
    val description: String,
    val actorName: String,
    val actorRole: String, // "Cold Caller", "Quality Lead", "AI Engine", "Client", "System"
    val timestamp: Long = System.currentTimeMillis(),
    val audioDurationSec: Int? = null,
    val transcriptionText: String? = null
)

// --- Skip Trace Result Entity ---
@Entity(tableName = "skiptrace_results")
data class SkiptraceResultEntity(
    @PrimaryKey val id: String,
    val batchId: String,
    val inputAddressOrName: String,
    val ownerFirstName: String,
    val ownerLastName: String,
    val age: Int = 54,
    val isDeceased: Boolean = false,
    val propertyAddress: String,
    val propertyCity: String,
    val propertyState: String,
    val propertyZip: String,
    val mailingAddress: String,
    val phone1: String,
    val phone1Type: String = "Mobile", // "Mobile", "Landline", "VoIP"
    val phone1Carrier: String = "Verizon Wireless",
    val phone1Dnc: Boolean = false,
    val phone1Confidence: Int = 98,
    val phone2: String? = null,
    val phone2Type: String? = "Mobile",
    val phone2Carrier: String? = "AT&T",
    val phone2Dnc: Boolean = false,
    val phone3: String? = null,
    val phone3Type: String? = "Landline",
    val phone3Carrier: String? = "Spectrum",
    val phone3Dnc: Boolean = true,
    val email1: String? = null,
    val email1Deliverable: Boolean = true,
    val email2: String? = null,
    val relativeName: String? = null,
    val relativePhone: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// --- Remi AI & Staff Chat Messages ---
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val sender: String, // "client", "ai", "staff"
    val senderName: String,
    val body: String,
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val attachmentMime: String? = null,
    val isAudio: Boolean = false,
    val audioDurationSec: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val readAt: Long? = null
)
