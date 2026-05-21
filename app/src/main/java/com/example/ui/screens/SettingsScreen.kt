package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.data.local.entity.UserEntity

@Composable
fun SettingsScreen(
    user: UserEntity,
    settings: SettingsManager,
    onLogout: () -> Unit
) {
    var apiUrl by remember { mutableStateOf(settings.apiUrl) }
    var useMockCloud by remember { mutableStateOf(settings.useMockCloud) }
    var isForceOffline by remember { mutableStateOf(settings.isForceOffline) }
    
    val selectedErrorTypes = remember { mutableStateOf(settings.errorTypes.toMutableSet()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "System Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            )
            Text(
                text = "Configure system cloud behaviors, toggle networking, and modify diagnostic properties.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Section 1: Cloud Sync Configuration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Cloud Database Topology",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Target API Url Input
                    OutlinedTextField(
                        value = apiUrl,
                        onValueChange = {
                            apiUrl = it
                            settings.apiUrl = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("settings_api_input"),
                        label = { Text("Remote REST API Base URL") },
                        placeholder = { Text("https://your-api.com/v1") },
                        leadingIcon = { Icon(Icons.Default.Dns, "Api Target", tint = Color(0xFF64748B)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !useMockCloud
                    )

                    // Mock Cloud DB Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(4f)) {
                            Text(
                                text = "Use Encrypted Cloud Simulator",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Bypass external REST service to demonstrate secure syncing using Sandbox Cloud SharedPreferences.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = useMockCloud,
                            onCheckedChange = {
                                useMockCloud = it
                                settings.useMockCloud = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4F46E5), checkedTrackColor = Color(0xFFEEF2FF))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Forced Offline Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(4f)) {
                            Text(
                                text = "Forced Offline Override Mode",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Disable all active networking loops to test offline ticket creation, log-writes, and subsequent deferred sync.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isForceOffline,
                            onCheckedChange = {
                                isForceOffline = it
                                settings.isForceOffline = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF59E0B), checkedTrackColor = Color(0xFFFFEDD5))
                        )
                    }
                }
            }

            // Section 2: Fault types customizer
            var newFaultName by remember { mutableStateOf("") }
            var faultToEdit by remember { mutableStateOf<String?>(null) }
            var editFaultValue by remember { mutableStateOf("") }

            if (faultToEdit != null) {
                AlertDialog(
                    onDismissRequest = { faultToEdit = null },
                    title = { Text("Rename Issue Classification", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Modify the description below for this fault archetype:", fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp))
                            OutlinedTextField(
                                value = editFaultValue,
                                onValueChange = { editFaultValue = it },
                                label = { Text("Fault Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("edit_fault_name_field")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val oldVal = faultToEdit
                                val newVal = editFaultValue.trim()
                                if (oldVal != null && newVal.isNotEmpty()) {
                                    val currentSet = selectedErrorTypes.value.toMutableSet()
                                    currentSet.remove(oldVal)
                                    currentSet.add(newVal)
                                    selectedErrorTypes.value = currentSet
                                    settings.errorTypes = currentSet
                                }
                                faultToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { faultToEdit = null }) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Customize Repair Fault Classifications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Add, edit, or delete the classification choices that populate the New Ticket selection form:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Add Custom Classifications Form Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFaultName,
                            onValueChange = { newFaultName = it },
                            placeholder = { Text("e.g. Broken Glass Back") },
                            label = { Text("New Issue/Error Type") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("new_fault_input_field"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val newVal = newFaultName.trim()
                                if (newVal.isNotEmpty()) {
                                    val currentSet = selectedErrorTypes.value.toMutableSet()
                                    currentSet.add(newVal)
                                    selectedErrorTypes.value = currentSet
                                    settings.errorTypes = currentSet
                                    newFaultName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("add_fault_button")
                        ) {
                            Icon(Icons.Default.Add, "Add fault")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display active error types list with Edit / Delete actions
                    val activeErrors = selectedErrorTypes.value.toList().sorted()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeErrors.forEach { fault ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Fault Icon",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = fault,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Edit (Pencil) Button
                                    IconButton(
                                        onClick = {
                                            faultToEdit = fault
                                            editFaultValue = fault
                                        },
                                        modifier = Modifier.size(32.dp).testTag("edit_fault_$fault")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit issue",
                                            tint = Color(0xFF4338CA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete (Trash) Button
                                    IconButton(
                                        onClick = {
                                            val currentSet = selectedErrorTypes.value.toMutableSet()
                                            if (currentSet.size > 1) { // Guard to keep at least 1
                                                currentSet.remove(fault)
                                                selectedErrorTypes.value = currentSet
                                                settings.errorTypes = currentSet
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).testTag("delete_fault_$fault")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete issue",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Profile and session metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Security Audit Profile Metadata",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DetailPair("LOGGED IN REPRESENTATIVE", user.fullName)
                    DetailPair("SECURE AUTHENTICATED EMAIL", user.email)
                    DetailPair("CLIENT HARDWARE ID", user.id)
                    DetailPair("SECURE JWT SESSION TOKEN", user.sessionToken ?: "No session token found")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_logout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF2F2),
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, "Log out")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terminate Secure Portal Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun DetailPair(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        DetailTextLabel(label)
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
