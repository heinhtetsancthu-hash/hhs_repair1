package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.BackupViewModel
import com.example.ui.viewmodel.TicketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local database and preferences service locator
        ServiceLocator.initialize(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Instantiate ViewModels
                val authViewModel: AuthViewModel = viewModel()
                val ticketViewModel: TicketViewModel = viewModel()
                val backupViewModel: BackupViewModel = viewModel()
                
                val loggedInUser by authViewModel.loggedInUser.collectAsStateWithLifecycle()
                val settings = ServiceLocator.getSettingsManager(applicationContext)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        // Smoothly transition between Auth screens and the business portal
                        Crossfade(targetState = loggedInUser) { user ->
                            if (user == null) {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onAuthSuccess = { _, _ ->
                                        // login successful, state matches reactively
                                    }
                                )
                            } else {
                                MainDashboardScreen(
                                    user = user,
                                    settings = settings,
                                    ticketViewModel = ticketViewModel,
                                    backupViewModel = backupViewModel,
                                    onLogout = {
                                        authViewModel.logout(onComplete = {
                                            ticketViewModel.clearForm()
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
