package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.*

@Database(
    entities = [
        ClientAccount::class,
        LeadEntity::class,
        TicketEntity::class,
        SupportTicketEntity::class,
        SupportTicketMessageEntity::class,
        DisputeThreadEntity::class,
        DisputeMessageEntity::class,
        CampaignGuideItem::class,
        ListOrderEntity::class,
        SkiptraceOrderEntity::class,
        SubscriptionEntity::class,
        InvoiceEntity::class,
        TransactionEntity::class,
        PaymentMethodEntity::class,
        NotificationItem::class,
        ChatMessage::class,
        LeadActivityEntity::class,
        SkiptraceResultEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PortalDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun leadDao(): LeadDao
    abstract fun ticketDao(): TicketDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun disputeDao(): DisputeDao
    abstract fun campaignGuideDao(): CampaignGuideDao
    abstract fun listOrderDao(): ListOrderDao
    abstract fun skiptraceDao(): SkiptraceDao
    abstract fun billingDao(): BillingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun chatDao(): ChatDao
    abstract fun leadActivityDao(): LeadActivityDao
    abstract fun skiptraceResultDao(): SkiptraceResultDao

    companion object {
        @Volatile
        private var INSTANCE: PortalDatabase? = null

        fun getInstance(context: Context): PortalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortalDatabase::class.java,
                    "proptech_client_portal.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
