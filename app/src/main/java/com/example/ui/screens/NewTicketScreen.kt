package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SettingsManager
import com.example.data.local.entity.UserEntity
import com.example.ui.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTicketScreen(
    ticketViewModel: TicketViewModel,
    user: UserEntity,
    settings: SettingsManager
) {
    val customerName by ticketViewModel.customerName.collectAsStateWithLifecycle()
    val phoneNumber by ticketViewModel.phoneNumber.collectAsStateWithLifecycle()
    val deviceBrand by ticketViewModel.deviceBrand.collectAsStateWithLifecycle()
    val deviceModel by ticketViewModel.deviceModel.collectAsStateWithLifecycle()
    val imei by ticketViewModel.imei.collectAsStateWithLifecycle()
    val errorType by ticketViewModel.errorType.collectAsStateWithLifecycle()
    val estimatedCost by ticketViewModel.estimatedCost.collectAsStateWithLifecycle()
    val screenLockType by ticketViewModel.screenLockType.collectAsStateWithLifecycle()
    val includedAccessories by ticketViewModel.includedAccessories.collectAsStateWithLifecycle()
    val serviceNotes by ticketViewModel.serviceNotes.collectAsStateWithLifecycle()
    
    val formError by ticketViewModel.formError.collectAsStateWithLifecycle()
    val saveSuccess by ticketViewModel.saveSuccess.collectAsStateWithLifecycle()

    val errorsList = settings.errorTypes.toList().sorted()
    var expandedDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            // Success alert could trigger, but VM auto-cleared form
            // Reset success state after view consumes it
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft, beautiful slate white
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Top Section Card (Open New Ticket Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Open New Ticket",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Text(
                        text = "Fill in customer device details to register a repair workorder.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                // Save button styled with Floppy Disk icon as in user picture
                Button(
                    onClick = {
                        ticketViewModel.saveTicket(user.id)
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("save_ticket_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Success & Error States
            AnimatedVisibility(visible = formError != null) {
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
                        Icon(Icons.Default.Error, "Error icon", tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formError ?: "",
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = saveSuccess) {
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
                        Icon(Icons.Default.CheckCircle, "Success icon", tint = Color(0xFF22C55E))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ticket Saved Successfully!",
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Saved in offline local database. Ready to sync secure cloud.",
                                color = Color(0xFF166534),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { ticketViewModel.resetSuccess() }) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF166534))
                        }
                    }
                }
            }

            // Two-column layout representation for form content
            // Left main details, Right accessory details (highly responsive in layout)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Customer & Hardware Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Customer Name Field
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { ticketViewModel.customerName.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("customer_name_input"),
                            label = { Text("Customer Name") },
                            placeholder = { Text("Full Name") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Phone Number Field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { ticketViewModel.phoneNumber.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("phone_number_input"),
                            label = { Text("Phone Number") },
                            placeholder = { Text("09...") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        // Brand & Model Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = deviceBrand,
                                onValueChange = { ticketViewModel.deviceBrand.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("device_brand_input"),
                                label = { Text("Device Brand") },
                                placeholder = { Text("Brand (e.g. Samsung)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = deviceModel,
                                onValueChange = { ticketViewModel.deviceModel.value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("device_model_input"),
                                label = { Text("Device Model") },
                                placeholder = { Text("Model (e.g. Galaxy S24)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // IMEI (Optional)
                        OutlinedTextField(
                            value = imei,
                            onValueChange = { ticketViewModel.imei.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("imei_input"),
                            label = { Text("IMEI (Optional)") },
                            placeholder = { Text("IMEI") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Error Selection Menu
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = errorType,
                                onValueChange = { },
                                label = { Text("Error / Issue") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                errorsList.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            ticketViewModel.errorType.value = selectionOption
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Estimated Cost
                        OutlinedTextField(
                            value = estimatedCost,
                            onValueChange = { ticketViewModel.estimatedCost.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("estimated_cost_input"),
                            label = { Text("Estimated Cost ($)") },
                            placeholder = { Text("Cost") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // Accessories & Security Screen Lock Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Security & Accessories Diagnostics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Screen Lock Selection (Pill Style)
                        Text(
                            text = "SCREEN LOCK SECURITY",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val locks = listOf("None", "Pin", "Password", "Pattern")
                            locks.forEachIndexed { index, lock ->
                                val isSelected = screenLockType == lock
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { ticketViewModel.screenLockType.value = lock }
                                        .background(if (isSelected) Color(0xFF4F46E5) else Color.Transparent)
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lock,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                    )
                                }
                                if (index < locks.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(48.dp)
                                            .background(Color(0xFFCBD5E1))
                                    )
                                }
                            }
                        }

                        // Conditional Security Lock Inputs (PIN/Password / Pattern Node Clicks)
                        val currentLockVal by ticketViewModel.screenLockValue.collectAsStateWithLifecycle()

                        if (screenLockType == "Pin" || screenLockType == "Password") {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = currentLockVal,
                                onValueChange = { ticketViewModel.screenLockValue.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("screen_lock_credential_input"),
                                label = { Text(if (screenLockType == "Pin") "Screen Lock PIN Code" else "Screen Lock Password") },
                                placeholder = { Text(if (screenLockType == "Pin") "Enter customer's PIN code" else "Enter customer's alphanumeric password") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (screenLockType == "Pin") KeyboardType.Number else KeyboardType.Password
                                )
                            )
                        } else if (screenLockType == "Pattern") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "CLICK PATTERN DOTS IN SEQUENCE",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    val currentNodesList = remember(currentLockVal) {
                                        if (currentLockVal.isBlank()) emptyList()
                                        else currentLockVal.split("-").mapNotNull { it.trim().toIntOrNull() }
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        for (row in 0..2) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (col in 0..2) {
                                                    val nodeNum = row * 3 + col + 1
                                                    val isTapped = currentNodesList.contains(nodeNum)
                                                    val orderIndex = if (isTapped) currentNodesList.indexOf(nodeNum) + 1 else 0

                                                    Box(
                                                        modifier = Modifier
                                                            .size(54.dp)
                                                            .clip(CircleShape)
                                                            .border(
                                                                width = if (isTapped) 3.dp else 1.5.dp,
                                                                color = if (isTapped) Color(0xFF4F46E5) else Color(0xFF94A3B8),
                                                                shape = CircleShape
                                                            )
                                                            .background(if (isTapped) Color(0xFFEEF2FF) else Color.White)
                                                            .clickable {
                                                                if (!isTapped) {
                                                                    val newList = currentNodesList + nodeNum
                                                                    ticketViewModel.screenLockValue.value = newList.joinToString("-")
                                                                }
                                                            }
                                                            .testTag("pattern_node_$nodeNum"),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isTapped) {
                                                            Text(
                                                                text = orderIndex.toString(),
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF4F46E5),
                                                                fontSize = 16.sp
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .background(Color(0xFF94A3B8), CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentNodesList.isEmpty()) "No pattern sequence entered"
                                            else "Tapped sequence: ${currentNodesList.joinToString(" → ")}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (currentNodesList.isNotEmpty()) {
                                            TextButton(
                                                onClick = { ticketViewModel.screenLockValue.value = "" },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                                modifier = Modifier.testTag("clear_pattern_button")
                                            ) {
                                                Icon(Icons.Default.Refresh, "Clear pattern", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Included Accessories
                        Text(
                            text = "INCLUDED ACCESSORIES",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val accs = listOf("Charger", "Battery", "Memory Card", "Sim Card")
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            accs.chunked(2).forEach { rowAccs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowAccs.forEach { acc ->
                                        val checked = includedAccessories.contains(acc)
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { ticketViewModel.toggleAccessory(acc) }
                                                .background(if (checked) Color(0xFFEEF2FF) else Color.White)
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { ticketViewModel.toggleAccessory(acc) },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F46E5))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = acc,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E293B)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Service Notes / Fault details
                        OutlinedTextField(
                            value = serviceNotes,
                            onValueChange = { ticketViewModel.serviceNotes.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("Service Notes / Fault Details") },
                            placeholder = { Text("Service Note / Fault Details...") },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Prominent, beautiful bottom Save button
            Button(
                onClick = {
                    ticketViewModel.saveTicket(user.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_ticket_button_bottom"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Save Workorder Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
