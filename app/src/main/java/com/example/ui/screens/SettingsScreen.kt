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
                        text = "Pick which error options appear in the New Ticket selection dropdown:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val standardFaults = listOf(
                        "Screen Damage", "Battery Issue", "Charging Port",
                        "Software Bug", "Water Damage", "Button Fault",
                        "Camera Repair", "Speaker & Audio", "Network / SIM", "Other"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        standardFaults.chunked(2).forEach { rowFaults ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowFaults.forEach { fault ->
                                    val checked = selectedErrorTypes.value.contains(fault)
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val editSet = selectedErrorTypes.value.toMutableSet()
                                                if (editSet.contains(fault)) {
                                                    if (editSet.size > 1) { // keep at least 1
                                                        editSet.remove(fault)
                                                    }
                                                } else {
                                                    editSet.add(fault)
                                                }
                                                selectedErrorTypes.value = editSet
                                                settings.errorTypes = editSet
                                            }
                                            .background(if (checked) Color(0xFFEEF2FF) else Color.White)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = null, // Custom Row Click Handles This
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F46E5)),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                        Text(
                                            text = fault,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E293B)
                                            )
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
