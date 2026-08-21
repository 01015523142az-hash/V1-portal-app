package com.example.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PortalDatabase
import com.example.model.NotificationItem
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmNotificationManager {

    const val CHANNEL_SKIPTRACE_ID = "channel_skiptrace_updates"
    const val CHANNEL_SKIPTRACE_NAME = "Skip Tracing Status Updates"
    const val CHANNEL_SKIPTRACE_DESC = "Notifications for completed and processing skip trace batch/single orders"

    const val CHANNEL_LEADS_ID = "channel_lead_assignments"
    const val CHANNEL_LEADS_NAME = "New Lead Assignments"
    const val CHANNEL_LEADS_DESC = "Real-time alerts when fresh seller leads or campaign acquisitions are assigned"

    const val CHANNEL_GENERAL_ID = "channel_general_alerts"
    const val CHANNEL_GENERAL_NAME = "General Portal Alerts"
    const val CHANNEL_GENERAL_DESC = "Billing updates, support ticket replies, and account notices"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val skiptraceChannel = NotificationChannel(
                CHANNEL_SKIPTRACE_ID,
                CHANNEL_SKIPTRACE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_SKIPTRACE_DESC
                enableVibration(true)
                setShowBadge(true)
            }

            val leadsChannel = NotificationChannel(
                CHANNEL_LEADS_ID,
                CHANNEL_LEADS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_LEADS_DESC
                enableVibration(true)
                setShowBadge(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                CHANNEL_GENERAL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_GENERAL_DESC
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(skiptraceChannel, leadsChannel, generalChannel)
            )
        }
    }

    fun retrieveFcmToken(context: Context? = null, onTokenReceived: (String) -> Unit = {}) {
        try {
            if (context != null && com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context)
            }
            if (com.google.firebase.FirebaseApp.getApps(context ?: return).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        android.util.Log.d("FCM", "Current FCM Registration Token: $token")
                        onTokenReceived(token ?: "")
                    } else {
                        android.util.Log.w("FCM", "Fetching FCM registration token failed: ${task.exception?.message}")
                    }
                }
            } else {
                android.util.Log.d("FCM", "FirebaseApp not configured yet, skipping remote token fetch.")
            }
        } catch (e: Exception) {
            android.util.Log.w("FCM", "FirebaseMessaging initialization notice: ${e.message}")
        }
    }

    fun postSystemNotification(
        context: Context,
        title: String,
        body: String,
        category: String, // "skiptrace", "leads", "tickets", "billing"
        customId: String? = null,
        extraData: Map<String, String> = emptyMap()
    ) {
        initNotificationChannels(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.w("FCM", "POST_NOTIFICATIONS permission not granted. Saving to in-app notification center only.")
                saveInAppNotification(context, title, body, category)
                return
            }
        }

        val channelId = when (category.lowercase()) {
            "skiptrace", "skiptrace_update", "skiptrace_completed" -> CHANNEL_SKIPTRACE_ID
            "leads", "lead_assigned", "new_lead" -> CHANNEL_LEADS_ID
            else -> CHANNEL_GENERAL_ID
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_category", category)
            putExtra("notification_id", customId)
            extraData.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val requestCode = (System.currentTimeMillis() % 100000).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            pendingIntentFlags
        )

        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (channelId == CHANNEL_GENERAL_ID) NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.e("FCM", "SecurityException when posting notification", e)
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Failed to post notification", e)
        }

        // Save to internal database for in-app bell notification sheet
        saveInAppNotification(context, title, body, category)
    }

    private fun saveInAppNotification(
        context: Context,
        title: String,
        body: String,
        category: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = PortalDatabase.getInstance(context)
                val item = NotificationItem(
                    id = "fcm_notif_${System.currentTimeMillis()}_${(100..999).random()}",
                    title = title,
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    category = when (category.lowercase()) {
                        "skiptrace", "skiptrace_update", "skiptrace_completed" -> "tickets"
                        "leads", "lead_assigned", "new_lead" -> "leads"
                        "billing" -> "billing"
                        else -> "tickets"
                    }
                )
                db.notificationDao().insertNotification(item)
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Error saving in-app notification to DB", e)
            }
        }
    }
}
