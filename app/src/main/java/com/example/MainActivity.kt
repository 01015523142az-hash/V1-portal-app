package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PortalDatabase
import com.example.data.PortalRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.AuthViewModelFactory
import com.example.viewmodel.PortalViewModel
import com.example.viewmodel.PortalViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
