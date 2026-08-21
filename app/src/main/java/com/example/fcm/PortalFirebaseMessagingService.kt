package com.example.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PortalFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        // Token can be synced to user profile/server when online
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val title: String = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Client Portal Update"

        val body: String = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You have a new update in your account."

        val category: String = remoteMessage.data["category"]
            ?: remoteMessage.data["type"]
            ?: "skiptrace"

        val customId: String? = remoteMessage.data["id"]

        Log.d(TAG, "Incoming Push - Title: $title, Body: $body, Category: $category")

        FcmNotificationManager.postSystemNotification(
            context = applicationContext,
            title = title,
            body = body,
            category = category,
            customId = customId,
            extraData = remoteMessage.data
        )
    }

    companion object {
        private const val TAG = "PortalFCMService"
    }
}
