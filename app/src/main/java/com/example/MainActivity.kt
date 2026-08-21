package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PortalDatabase
import com.example.data.PortalRepository
import com.example.fcm.FcmNotificationManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.AuthViewModelFactory
import com.example.viewmodel.PortalViewModel
import com.example.viewmodel.PortalViewModelFactory
import com.google.firebase.FirebaseApp

class MainActivity : FragmentActivity() {
    companion object {
        private const val PERMISSION_REQUEST_POST_NOTIFICATIONS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely initialize FirebaseApp if not already initialized
        try {
            if (FirebaseApp.getApps(applicationContext).isEmpty()) {
                FirebaseApp.initializeApp(applicationContext)
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "FirebaseApp init fallback: ${e.message}")
        }

        // Initialize FCM Push Notification Channels & retrieve FCM registration token
        FcmNotificationManager.initNotificationChannels(applicationContext)
        FcmNotificationManager.retrieveFcmToken(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+ with valid 16-bit requestCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_POST_NOTIFICATIONS
                )
            }
        }

        val database = PortalDatabase.getInstance(applicationContext)
        val repository = PortalRepository(database, applicationContext)

        setContent {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(repository)
            )
            val portalViewModel: PortalViewModel = viewModel(
                factory = PortalViewModelFactory(repository)
            )

            val authState by authViewModel.uiState.collectAsState()
            val portalState by portalViewModel.uiState.collectAsState()

            // Handle incoming notification intent routing
            LaunchedEffect(intent) {
                val notifCategory = intent.getStringExtra("notification_category")
                if (!notifCategory.isNullOrBlank()) {
                    when (notifCategory.lowercase()) {
                        "skiptrace", "skiptrace_update", "skiptrace_completed" -> {
                            portalViewModel.selectTab("skiptrace")
                        }
                        "leads", "lead_assigned", "new_lead" -> {
                            portalViewModel.selectTab("leads")
                        }
                        "billing" -> {
                            portalViewModel.selectTab("billing")
                        }
                    }
                }
            }

            MyApplicationTheme(
                darkTheme = portalState.isDarkTheme,
                themePreset = portalState.themePreset,
                fontPreset = portalState.fontPreset
            ) {
                if (authState.isAuthenticated) {
                    MainAppScreen(
                        portalViewModel = portalViewModel,
                        authViewModel = authViewModel
                    )
                } else {
                    AuthScreen(
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
