package com.example.data

import android.util.Log
import com.example.model.LeadEntity
import com.example.model.SupportTicketEntity
import com.example.model.TicketEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Cloud Storage synchronization layer for Leads, Deal Tickets, and Support Inquiries using Google Cloud Firestore.
 * Provides real-time and background cloud replication for client accounts.
 */
class FirestoreCloudService {
    private val tag = "FirestoreCloudService"
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore not initialized or google-services.json not configured: ${e.message}")
            null
        }
    }

    /**
     * Sync Lead data to cloud Firestore collection "leads"
     */
    suspend fun syncLeadToCloud(lead: LeadEntity, clientId: String = "client_demo_01") {
        val db = firestore ?: return
        try {
            val leadMap = hashMapOf(
                "id" to lead.id,
                "clientId" to clientId,
                "campaignId" to lead.campaignId,
                "campaignName" to lead.campaignName,
                "sellerName" to lead.sellerName,
                "sellerPhone" to lead.sellerPhone,
                "propertyAddress" to lead.propertyAddress,
                "propertyCity" to lead.propertyCity,
                "propertyState" to lead.propertyState,
                "propertyZip" to lead.propertyZip,
                "askingPrice" to lead.askingPrice,
                "marketValue" to lead.marketValue,
                "marketValueSource" to lead.marketValueSource,
                "whySell" to lead.whySell,
                "reasonSell" to lead.reasonSell,
                "whenSell" to lead.whenSell,
                "status" to lead.status,
                "notes" to lead.notes,
                "submittedAt" to lead.submittedAt,
                "aiSummary" to (lead.aiSummary ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("clients")
                .document(clientId)
                .collection("leads")
                .document(lead.id)
                .set(leadMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced lead ${lead.id} to Firestore")
        } catch (e: Exception) {
            Log.e(tag, "Error syncing lead to Firestore: ${e.message}")
        }
    }

    /**
     * Sync Deal Review Ticket data to cloud Firestore collection "deal_tickets"
     */
    suspend fun syncDealTicketToCloud(ticket: TicketEntity, clientId: String = "client_demo_01") {
        val db = firestore ?: return
        try {
            val ticketMap = hashMapOf(
                "id" to ticket.id,
                "clientId" to clientId,
                "campaignName" to ticket.campaignName,
                "propertyAddress" to ticket.propertyAddress,
                "askingPrice" to ticket.askingPrice,
                "marketValue" to ticket.marketValue,
                "marketValueSource" to ticket.marketValueSource,
                "whySell" to ticket.whySell,
                "whenSell" to ticket.whenSell,
                "notes" to ticket.notes,
                "status" to ticket.status,
                "submittedAt" to ticket.submittedAt,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("clients")
                .document(clientId)
                .collection("deal_tickets")
                .document(ticket.id)
                .set(ticketMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced deal ticket ${ticket.id} to Firestore")
        } catch (e: Exception) {
            Log.e(tag, "Error syncing deal ticket to Firestore: ${e.message}")
        }
    }

    /**
     * Sync Client Support Ticket & Inquiry to cloud Firestore collection "support_tickets"
     */
    suspend fun syncSupportTicketToCloud(ticket: SupportTicketEntity, clientId: String = "client_demo_01") {
        val db = firestore ?: return
        try {
            val supportMap = hashMapOf(
                "id" to ticket.id,
                "clientId" to clientId,
                "ticketNumber" to ticket.ticketNumber,
                "subject" to ticket.subject,
                "category" to ticket.category,
                "priority" to ticket.priority,
                "description" to ticket.description,
                "status" to ticket.status,
                "relatedLeadId" to (ticket.relatedLeadId ?: ""),
                "lastStaffReply" to (ticket.lastStaffReply ?: ""),
                "createdAt" to ticket.createdAt,
                "updatedAt" to ticket.updatedAt
            )

            db.collection("clients")
                .document(clientId)
                .collection("support_tickets")
                .document(ticket.id)
                .set(supportMap, SetOptions.merge())
                .await()
            Log.d(tag, "Successfully synced support ticket ${ticket.id} to Firestore")
        } catch (e: Exception) {
            Log.e(tag, "Error syncing support ticket to Firestore: ${e.message}")
        }
    }
}
