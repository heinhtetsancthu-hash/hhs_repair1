package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val ticketRepository = ServiceLocator.getTicketRepository(application)
    private val settings = ServiceLocator.getSettingsManager(application)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _syncSuccess = MutableStateFlow<String?>(null)
    val syncSuccess: StateFlow<String?> = _syncSuccess.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    fun addLog(message: String) {
        val current = _syncLogs.value.toMutableList()
        current.add(0, "[${System.currentTimeMillis() % 1000000 / 1000}s] $message")
        _syncLogs.value = current
    }

    fun syncData(userId: String, token: String) {
        if (userId.isEmpty()) {
            _syncError.value = "User session expired. Please sign in again."
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            _syncSuccess.value = null
            addLog("Initializing secure cloud database synchronizer...")
            addLog("Connecting safely to ${if (settings.useMockCloud) "Secure Encrypted Cloud Simulator" else settings.apiUrl}...")

            try {
                val result = ticketRepository.syncTickets(userId, token)
                addLog("Secure channel established. Processing payloads...")
                
                if (result.uploadedCount > 0) {
                    addLog("Successfully uploaded ${result.uploadedCount} repair logs created/edited offline.")
                } else {
                    addLog("No local offline mutations detected.")
                }

                if (result.downloadedCount > 0) {
                    addLog("Synchronized and downloaded ${result.downloadedCount} updated records from cloud.")
                } else {
                    addLog("Local database is fully up-to-date with cloud database.")
                }

                addLog("Synchronization transaction completed securely.")
                _syncSuccess.value = "Synced successfully! Uploaded ${result.uploadedCount}, Downloaded ${result.downloadedCount} logs."
            } catch (e: Exception) {
                addLog("Transaction Interrupted: ${e.message}")
                _syncError.value = e.message ?: "Failed to connect to cloud database."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearLogs() {
        _syncLogs.value = emptyList()
    }

    fun simulateNewDevice(onComplete: () -> Unit) {
        viewModelScope.launch {
            addLog("SIMULATION: Simulating moving to a new device.")
            addLog("Wiping local Room database cache...")
            ticketRepository.clearAllLocalCachedTickets()
            settings.clearSyncTime()
            addLog("Local database cache cleared successfully in offline sandbox!")
            addLog("Please perform synchronization again to download data from the secure cloud.")
            onComplete()
        }
    }
}
