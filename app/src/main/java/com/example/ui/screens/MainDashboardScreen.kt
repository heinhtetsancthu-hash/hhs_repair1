package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.data.local.entity.UserEntity
import com.example.ui.viewmodel.BackupViewModel
import com.example.ui.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    user: UserEntity,
    settings: SettingsManager,
    ticketViewModel: TicketViewModel,
    backupViewModel: BackupViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf(DashboardTab.NEW_TICKET) }

    // Read screen width configuration dynamically
    val configuration = LocalConfiguration.current
    val isTabletOrDesktop = configuration.screenWidthDp > 720

    // Sync VM UserId once on load or change
    LaunchedEffect(user.id) {
        ticketViewModel.setUserId(user.id)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useSidebar = isTabletOrDesktop || maxWidth > 720.dp

        if (useSidebar) {
            // Elegant Desktop / Tablet Sidebar Drawer Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Sidebar Drawer
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(0.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title / Profile header as in picture
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handyman,
                                    contentDescription = "Handy logo",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "REPAIR CENTER",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4F46E5),
                                        letterSpacing = 1.sp
                                    ),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Navigation Items
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SidebarNavItem(
                                label = "New Ticket",
                                icon = Icons.Default.AddCircleOutline,
                                activeIcon = Icons.Default.AddCircle,
                                isSelected = activeTab == DashboardTab.NEW_TICKET,
                                onClick = { activeTab = DashboardTab.NEW_TICKET },
                                testTag = "nav_new_ticket"
                            )
                            SidebarNavItem(
                                label = "Ticket List",
                                icon = Icons.Default.FormatListBulleted,
                                activeIcon = Icons.Default.ListAlt,
                                isSelected = activeTab == DashboardTab.TICKET_LIST,
                                onClick = { activeTab = DashboardTab.TICKET_LIST },
                                testTag = "nav_ticket_list"
                            )
                            SidebarNavItem(
                                label = "Data Sync",
                                icon = Icons.Default.CloudQueue,
                                activeIcon = Icons.Default.CloudDone,
                                isSelected = activeTab == DashboardTab.BACKUP,
                                onClick = { activeTab = DashboardTab.BACKUP },
                                testTag = "nav_backup"
                            )
                            SidebarNavItem(
                                label = "Settings",
                                icon = Icons.Default.Settings,
                                activeIcon = Icons.Default.SettingsApplications,
                                isSelected = activeTab == DashboardTab.SETTINGS,
                                onClick = { activeTab = DashboardTab.SETTINGS },
                                testTag = "nav_settings"
                            )
                        }
                    }

                    // Bottom: Sign Out button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLogout() }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign Out",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // Right Panel: Active Screen Layout
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Desktop Top Area Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF1F5F9))
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeTab.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E293B)
                            )
                        )

                        // Initials profile badge "AD" as in top right of screenshot
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = remember(user.fullName) {
                                user.fullName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .take(2)
                                    .joinToString("")
                            }
                            Text(
                                text = initials.ifEmpty { "AD" },
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ActiveContentTab(activeTab, ticketViewModel, backupViewModel, user, settings, onLogout)
                    }
                }
            }
        } else {
            // Elegant Mobile Layout using bottom navigation bar
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = activeTab.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = remember(user.fullName) {
                                    user.fullName.split(" ")
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .take(2)
                                        .joinToString("")
                                }
                                Text(
                                    text = initials.ifEmpty { "AD" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5),
                                    fontSize = 11.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.NEW_TICKET,
                            onClick = { activeTab = DashboardTab.NEW_TICKET },
                            icon = { Icon(Icons.Default.AddCircle, "New Ticket") },
                            label = { Text("New Ticket") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                indicatorColor = Color(0xFFEEF2FF)
                            )
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.TICKET_LIST,
                            onClick = { activeTab = DashboardTab.TICKET_LIST },
                            icon = { Icon(Icons.Default.ListAlt, "Tickets") },
                            label = { Text("List") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                indicatorColor = Color(0xFFEEF2FF)
                            )
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.BACKUP,
                            onClick = { activeTab = DashboardTab.BACKUP },
                            icon = { Icon(Icons.Default.SyncAlt, "Sync") },
                            label = { Text("Sync") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                indicatorColor = Color(0xFFEEF2FF)
                            )
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.SETTINGS,
                            onClick = { activeTab = DashboardTab.SETTINGS },
                            icon = { Icon(Icons.Default.Settings, "Settings") },
                            label = { Text("Settings") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4F46E5),
                                selectedTextColor = Color(0xFF4F46E5),
                                indicatorColor = Color(0xFFEEF2FF)
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ActiveContentTab(activeTab, ticketViewModel, backupViewModel, user, settings, onLogout)
                }
            }
        }
    }
}

enum class DashboardTab(val title: String) {
    NEW_TICKET("New Ticket"),
    TICKET_LIST("Ticket List"),
    BACKUP("Data Backup"),
    SETTINGS("Settings")
}

@Composable
fun ActiveContentTab(
    tab: DashboardTab,
    ticketViewModel: TicketViewModel,
    backupViewModel: BackupViewModel,
    user: UserEntity,
    settings: SettingsManager,
    onLogout: () -> Unit
) {
    // Elegant fade transition animation on screen change
    Crossfade(
        targetState = tab,
        animationSpec = tween(150)
    ) { targetTab ->
        when (targetTab) {
            DashboardTab.NEW_TICKET -> NewTicketScreen(ticketViewModel, user, settings)
            DashboardTab.TICKET_LIST -> TicketListScreen(ticketViewModel)
            DashboardTab.BACKUP -> DataBackupScreen(backupViewModel, ticketViewModel, user, settings)
            DashboardTab.SETTINGS -> SettingsScreen(user, settings, onLogout)
        }
    }
}

@Composable
fun SidebarNavItem(
    label: String,
    icon: ImageVector,
    activeIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) activeIcon else icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4F46E5) else Color(0xFF64748B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF475569)
        )
    }
}
