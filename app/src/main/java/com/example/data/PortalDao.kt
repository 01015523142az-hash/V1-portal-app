package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM client_account WHERE id = :id LIMIT 1")
    fun getAccountFlow(id: String = "client_user_01"): Flow<ClientAccount?>

    @Query("SELECT * FROM client_account WHERE id = :id LIMIT 1")
    suspend fun getAccount(id: String = "client_user_01"): ClientAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: ClientAccount)

    @Query("UPDATE client_account SET fullName = :name, companyName = :company, phone = :phone WHERE id = :id")
    suspend fun updateAccountProfile(name: String, company: String, phone: String, id: String = "client_user_01")

    @Query("UPDATE client_account SET notifyLeads = :leads, notifyTickets = :tickets, notifyBilling = :billing, notifyOrders = :orders, notifySms = :sms WHERE id = :id")
    suspend fun updateNotificationPreferences(leads: Boolean, tickets: Boolean, billing: Boolean, orders: Boolean, sms: Boolean, id: String = "client_user_01")

    @Query("UPDATE client_account SET skiptraceCredits = skiptraceCredits + :credits, creditBalanceCents = creditBalanceCents + (:credits * 3) WHERE id = :id")
    suspend fun addCredits(credits: Int, id: String = "client_user_01")

    @Query("UPDATE client_account SET skiptraceCredits = skiptraceCredits - :credits, creditBalanceCents = creditBalanceCents - (:credits * 3) WHERE id = :id")
    suspend fun deductCredits(credits: Int, id: String = "client_user_01")

    @Query("UPDATE client_account SET biometricEnabled = :enabled WHERE id = :id")
    suspend fun updateBiometric(enabled: Boolean, id: String = "client_user_01")

    @Query("UPDATE client_account SET lastLoginAt = :time WHERE id = :id")
    suspend fun updateLastLogin(time: Long, id: String = "client_user_01")
}

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY submittedAt DESC")
    fun getAllLeadsFlow(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id LIMIT 1")
    fun getLeadByIdFlow(id: String): Flow<LeadEntity?>

    @Query("SELECT * FROM leads WHERE id = :id LIMIT 1")
    suspend fun getLeadById(id: String): LeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<LeadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity)

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Query("UPDATE leads SET status = :status WHERE id = :leadId")
    suspend fun updateLeadStatus(leadId: String, status: String)

    @Query("UPDATE leads SET disputeStatus = :status WHERE id = :leadId")
    suspend fun updateDisputeStatus(leadId: String, status: String)

    @Query("UPDATE leads SET feedbackStatus = :status WHERE id = :leadId")
    suspend fun updateFeedbackStatus(leadId: String, status: String)

    @Query("SELECT COUNT(*) FROM leads")
    suspend fun getLeadCount(): Int
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE status = 'client_review' ORDER BY submittedAt DESC")
    fun getReviewTicketsFlow(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets ORDER BY submittedAt DESC")
    fun getAllTicketsFlow(): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Query("UPDATE tickets SET status = :status WHERE id = :ticketId")
    suspend fun updateTicketStatus(ticketId: String, status: String)

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): TicketEntity?
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllSupportTicketsFlow(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    fun getSupportTicketByIdFlow(id: String): Flow<SupportTicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTickets(tickets: List<SupportTicketEntity>)

    @Query("UPDATE support_tickets SET status = :status, updatedAt = :updatedAt WHERE id = :ticketId")
    suspend fun updateStatus(ticketId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE support_tickets SET lastStaffReply = :reply, updatedAt = :updatedAt WHERE id = :ticketId")
    suspend fun updateStaffReply(ticketId: String, reply: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM support_ticket_messages WHERE ticketId = :ticketId ORDER BY createdAt ASC")
    fun getMessagesFlow(ticketId: String): Flow<List<SupportTicketMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SupportTicketMessageEntity)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM dispute_threads ORDER BY createdAt DESC")
    fun getAllThreadsFlow(): Flow<List<DisputeThreadEntity>>

    @Query("SELECT * FROM dispute_threads WHERE leadId = :leadId LIMIT 1")
    fun getThreadByLeadId(leadId: String): Flow<DisputeThreadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: DisputeThreadEntity)

    @Query("SELECT * FROM dispute_messages WHERE disputeId = :disputeId ORDER BY createdAt ASC")
    fun getMessagesFlow(disputeId: String): Flow<List<DisputeMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DisputeMessageEntity)

    @Query("UPDATE dispute_threads SET status = :status WHERE id = :disputeId")
    suspend fun updateThreadStatus(disputeId: String, status: String)
}

@Dao
interface CampaignGuideDao {
    @Query("SELECT * FROM campaign_guide_items ORDER BY campaignDisplayName ASC, id ASC")
    fun getGuideItemsFlow(): Flow<List<CampaignGuideItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuideItems(items: List<CampaignGuideItem>)

    @Update
    suspend fun updateGuideItem(item: CampaignGuideItem)

    @Query("UPDATE campaign_guide_items SET proposedValue = :proposed, isPendingApproval = 1 WHERE id = :id")
    suspend fun proposeEdit(id: Long, proposed: String)

    @Query("UPDATE campaign_guide_items SET value = proposedValue, proposedValue = null, isPendingApproval = 0 WHERE id = :id")
    suspend fun approveProposal(id: Long)
}

@Dao
interface ListOrderDao {
    @Query("SELECT * FROM list_orders ORDER BY createdAt DESC")
    fun getListOrdersFlow(): Flow<List<ListOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: ListOrderEntity)

    @Query("UPDATE list_orders SET status = :status, fileName = :fileName, fileUrl = :fileUrl WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, fileName: String?, fileUrl: String?)
}

@Dao
interface SkiptraceDao {
    @Query("SELECT * FROM skiptrace_orders ORDER BY createdAt DESC")
    fun getOrdersFlow(): Flow<List<SkiptraceOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SkiptraceOrderEntity)

    @Query("UPDATE skiptrace_orders SET status = :status, resultsCount = :count WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, count: Int)
}

@Dao
interface BillingDao {
    @Query("SELECT * FROM campaign_subscriptions ORDER BY campaignDisplayName ASC")
    fun getSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subs: List<SubscriptionEntity>)

    @Update
    suspend fun updateSubscription(sub: SubscriptionEntity)

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getInvoicesFlow(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<TransactionEntity>)

    @Query("SELECT * FROM payment_methods ORDER BY isDefault DESC")
    fun getPaymentMethodsFlow(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(pm: PaymentMethodEntity)

    @Query("UPDATE payment_methods SET isDefault = 0")
    suspend fun clearDefaultPaymentMethods()

    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultPaymentMethod(id: String)

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deletePaymentMethod(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(item: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
    fun getMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET readAt = :readAt WHERE readAt IS NULL")
    suspend fun markAllRead(readAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_messages SET body = :body WHERE id = :id")
    suspend fun updateBody(id: String, body: String)
}

@Dao
interface LeadActivityDao {
    @Query("SELECT * FROM lead_activities ORDER BY timestamp DESC")
    fun getAllActivitiesFlow(): Flow<List<LeadActivityEntity>>

    @Query("SELECT * FROM lead_activities WHERE leadId = :leadId ORDER BY timestamp DESC")
    fun getActivitiesForLeadFlow(leadId: String): Flow<List<LeadActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LeadActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<LeadActivityEntity>)
}

@Dao
interface SkiptraceResultDao {
    @Query("SELECT * FROM skiptrace_results ORDER BY createdAt DESC")
    fun getAllResultsFlow(): Flow<List<SkiptraceResultEntity>>

    @Query("SELECT * FROM skiptrace_results WHERE batchId = :batchId ORDER BY createdAt DESC")
    fun getResultsForBatchFlow(batchId: String): Flow<List<SkiptraceResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SkiptraceResultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SkiptraceResultEntity)

    @Query("DELETE FROM skiptrace_results")
    suspend fun clearAll()
}

