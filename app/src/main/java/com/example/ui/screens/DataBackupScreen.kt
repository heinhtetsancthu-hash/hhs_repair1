package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SettingsManager
import com.example.data.local.entity.UserEntity
import com.example.ui.viewmodel.BackupViewModel
import com.example.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DataBackupScreen(
    backupViewModel: BackupViewModel,
    ticketViewModel: TicketViewModel,
    user: UserEntity,
    settings: SettingsManager
) {
    val isSyncing by backupViewModel.isSyncing.collectAsStateWithLifecycle()
    val syncError by backupViewModel.syncError.collectAsStateWithLifecycle()
    val syncSuccess by backupViewModel.syncSuccess.collectAsStateWithLifecycle()
    val syncLogs by backupViewModel.syncLogs.collectAsStateWithLifecycle()

    val tickets by ticketViewModel.tickets.collectAsStateWithLifecycle()
    val unsyncedCount = remember(tickets) { tickets.count { !it.isSynced } }
    val totalCount = remember(tickets) { tickets.size }

    val lastSyncFormatted = remember(settings.lastSyncTime) {
        if (settings.lastSyncTime == 0L) "Never"
        else {
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            sdf.format(Date(settings.lastSyncTime))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Cloud Synchronization Engine",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            )
            Text(
                text = "Synchronize local repair logs with your secure business database securely.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Alert Messages
            AnimatedVisibility(visible = syncError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, "Error Icon", tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = syncError ?: "",
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = syncSuccess != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, "Success Icon", tint = Color(0xFF22C55E))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = syncSuccess ?: "",
                            color = Color(0xFF166534),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sync Status Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Current Database Sync Status",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Metric 1: Pending Sync
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Local Changes",
                                tint = if (unsyncedCount > 0) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Local Offline Changes",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$unsyncedCount tickets",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (unsyncedCount > 0) Color(0xFFD97706) else Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Metric 2: Total Cache
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dataset,
                                contentDescription = "Local Cache",
                                tint = Color(0xFF4F46E5)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Total Local Log Cache",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalCount tickets",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            DetailTextLabel("LAST SENSITIVE CLOUD SYNCHRONIZATION TRANSACTION")
                            Text(
                                text = lastSyncFormatted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }

                        if (settings.isForceOffline) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFEDD5))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.WifiOff,
                                        "Forced Offline",
                                        tint = Color(0xFFC2410C),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OFFLINE OVERRIDE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sync Buttons Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Perform sync
                Button(
                    onClick = {
                        backupViewModel.syncData(user.id, user.sessionToken ?: "jwt_placeholder")
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("sync_data_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.SyncAlt, "Sync Now")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sync Remote Database",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Simulate device change
                Button(
                    onClick = {
                        backupViewModel.simulateNewDevice(onComplete = {
                            // Local ticket cache wiped, we can force list view updates
                        })
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("simulate_device_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF475569)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSyncing
                ) {
                    Icon(Icons.Default.PhonelinkSetup, "Device Change")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device Transition",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Interactive Backup Logs Screen console terminal
            Text(
                text = "TRANSACTION LOG ENGINE OUTPUT",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A)) // Terminal style dark slate
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (syncLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sync terminal idle. Awaiting operations...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "portal_sync_engine@localhost:~",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                "Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF43F5E),
                                modifier = Modifier
                                    .clickable { backupViewModel.clearLogs() }
                                    .padding(4.dp)
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(syncLogs) { log ->
                                Text(
                                    text = log,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (log.contains("Transaction Interrupted")) Color(0xFFFB7185)
                                    else if (log.contains("successfully") || log.contains("completed")) Color(0xFF34D399)
                                    else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
