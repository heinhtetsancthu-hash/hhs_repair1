package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TicketEntity
import com.example.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    ticketViewModel: TicketViewModel
) {
    val tickets by ticketViewModel.tickets.collectAsStateWithLifecycle()
    val searchQuery by ticketViewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by ticketViewModel.filterStatus.collectAsStateWithLifecycle()

    // Filter tickets based on search query & status tabs
    val filteredTickets = remember(tickets, searchQuery, filterStatus) {
        tickets.filter { t ->
            val matchQuery = t.customerName.contains(searchQuery, ignoreCase = true) ||
                    t.phoneNumber.contains(searchQuery) ||
                    t.deviceBrand.contains(searchQuery, ignoreCase = true) ||
                    t.deviceModel.contains(searchQuery, ignoreCase = true) ||
                    t.id.contains(searchQuery, ignoreCase = true)

            val matchStatus = if (filterStatus == "All") true else t.status.equals(filterStatus, ignoreCase = true)

            matchQuery && matchStatus
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
                text = "Repair Log Sheets",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            )
            Text(
                text = "Manage customer repair tickets, update job status, and inspect sync flows.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Search and Status Filters Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { ticketViewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("ticket_search_input"),
                        placeholder = { Text("Search by ticket, customer name, phone or model...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search icon", tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { ticketViewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Status Tabs Segment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf("All", "Pending", "In Progress", "Completed", "Delivered")
                        tabs.forEach { tab ->
                            val isSelected = filterStatus == tab
                            val count = remember(tickets, tab) {
                                if (tab == "All") tickets.size
                                else tickets.count { it.status.equals(tab, ignoreCase = true) }
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { ticketViewModel.filterStatus.value = tab },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tab)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFFE2E8F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF475569)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4F46E5),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Ticket List Stream
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty state",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No repair tickets found",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Draft and save tickets under the 'New Ticket' tab.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        TicketCardItem(
                            ticket = ticket,
                            onStatusChange = { newStatus ->
                                ticketViewModel.updateTicketStatus(ticket, newStatus)
                            },
                            onDelete = {
                                ticketViewModel.deleteTicket(ticket.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TicketCardItem(
    ticket: TicketEntity,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val formattedDate = remember(ticket.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(ticket.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ticket ID and date
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ticket.id,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        // Sync Indicator Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (ticket.isSynced) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ticket.isSynced) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                    contentDescription = "Sync state",
                                    tint = if (ticket.isSynced) Color(0xFF166534) else Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (ticket.isSynced) "Synced" else "Local",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ticket.isSynced) Color(0xFF166534) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Action Menu (Change Status / Delete)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Cost Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$${String.format("%.2f", ticket.estimatedCost)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFF1F5F9))

            // Body Detail grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Details - Profile information
                Column(modifier = Modifier.weight(1.1f)) {
                    DetailTextLabel("CUSTOMER FULL NAME")
                    Text(
                        text = ticket.customerName,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailTextLabel("PHONE COMPACT")
                    Text(
                        text = ticket.phoneNumber,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        fontSize = 13.sp
                    )
                }

                // Center Details - Brand Model imei
                Column(modifier = Modifier.weight(1.2f)) {
                    DetailTextLabel("DEVICE CLASSIFICATION")
                    Text(
                        text = "${ticket.deviceBrand} ${ticket.deviceModel}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailTextLabel("IMEI SERIAL")
                    Text(
                        text = ticket.imei ?: "Not provided",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }

                // Right Details - Lock Security and Error classification
                Column(modifier = Modifier.weight(1.1f)) {
                    DetailTextLabel("REPORTED ISSUE")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFF7ED))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.errorType,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailTextLabel("SCREEN SECURITY")
                    Text(
                        text = "Pattern/Lock: ${ticket.screenLockType}",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            // Accessories section
            if (ticket.includedAccessories.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DetailTextLabel("INCLUDED DEVICES & PARTS: ")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.includedAccessories.replace(",", ", "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        ),
                        fontSize = 11.sp
                    )
                }
            }

            // Problem Details (Notes)
            if (ticket.serviceNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp)
                ) {
                    Column {
                        DetailTextLabel("SERVICE NOTES / REMARK")
                        Text(
                            text = ticket.serviceNotes,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFF1F5F9))

            // Footer Row: Status selection pill and Status Indicator Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Code Status Pill
                val statusConfig = when (ticket.status) {
                    "Pending" -> Pair(Color(0xFFF1F5F9), Color(0xFF475569)) // Gray
                    "In Progress" -> Pair(Color(0xFFEFF6FF), Color(0xFF1D4ED8)) // Blue
                    "Completed" -> Pair(Color(0xFFECFDF5), Color(0xFF047857)) // Green
                    "Delivered" -> Pair(Color(0xFFF5F3FF), Color(0xFF6D28D9)) // Purple
                    else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusConfig.second)
                            .size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ticket.status.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = statusConfig.second
                    )
                }

                // Inline workflow selection dropdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Update state: ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        Button(
                            onClick = { expandedMenu = true },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("update_status_${ticket.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Action",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "dropdown",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            val workflowStates = listOf("Pending", "In Progress", "Completed", "Delivered")
                            workflowStates.forEach { wState ->
                                DropdownMenuItem(
                                    text = { Text(wState) },
                                    onClick = {
                                        onStatusChange(wState)
                                        expandedMenu = false
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

@Composable
fun DetailTextLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}
