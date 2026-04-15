// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminHomeScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Admin Navigation Tab
// ─────────────────────────────────────────────────────────────────────────────

enum class AdminTab(val icon: ImageVector, val label: String) {
    DASHBOARD(Icons.Default.Dashboard,  "Dashboard"),
    USERS    (Icons.Default.People,     "Users"),
    BABIES   (Icons.Default.ChildCare,  "Babies"),
    SETTINGS (Icons.Default.Settings,   "Settings"),
}

// ─────────────────────────────────────────────────────────────────────────────
// Admin Home Screen Root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminHomeScreen(
    viewModel       : AdminViewModel,
    onNavigateToLogin: () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val snackbar     = remember { SnackbarHostState() }

    var selectedTab  by remember { mutableStateOf(AdminTab.DASHBOARD) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmDeleteUser  by remember { mutableStateOf<String?>(null) }
    var confirmDeleteBaby  by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.navigateToWelcome) {
        if (state.navigateToWelcome) onNavigateToLogin()
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title  = { Text("Log Out") },
            text   = { Text("Are you sure you want to log out of the admin panel?") },
            confirmButton = {
                Button(
                    onClick = { confirmLogout = false; viewModel.logout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } }
        )
    }

    confirmDeleteUser?.let { userId ->
        val user = state.allUsers.find { it.userId == userId }
        AlertDialog(
            onDismissRequest = { confirmDeleteUser = null },
            title  = { Text("Delete User") },
            text   = { Text("Permanently delete ${user?.fullName ?: "this user"}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUser(userId); confirmDeleteUser = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteUser = null }) { Text("Cancel") } }
        )
    }

    confirmDeleteBaby?.let { babyId ->
        val baby = state.allBabies.find { it.babyId == babyId }
        AlertDialog(
            onDismissRequest = { confirmDeleteBaby = null },
            title  = { Text("Delete Baby") },
            text   = { Text("Permanently delete ${baby?.fullName ?: "this baby"}'s profile? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteBaby(babyId); confirmDeleteBaby = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteBaby = null }) { Text("Cancel") } }
        )
    }

    // ── Main Scaffold ──────────────────────────────────────────────────────────

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AdminTopBar(
                adminName  = state.adminName,
                adminEmail = state.adminEmail,
                onLogout   = { confirmLogout = true },
                customColors = customColors,
                dimensions   = dimensions,
                onRefresh    = { viewModel.refresh() },
                isRefreshing = state.isRefreshing
            )
        },
        bottomBar = {
            AdminBottomBar(
                selectedTab  = selectedTab,
                onTabSelected = { selectedTab = it },
                customColors  = customColors
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> AdminLoadingState()
                else -> AnimatedContent(
                    targetState   = selectedTab,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label         = "admin_tab"
                ) { tab ->
                    when (tab) {
                        AdminTab.DASHBOARD -> AdminDashboardTab(
                            stats      = state.stats,
                            customColors = customColors,
                            dimensions   = dimensions
                        )
                        AdminTab.USERS -> AdminUsersTab(
                            users        = state.filteredUsers,
                            searchQuery  = state.userSearchQuery,
                            selectedTab  = state.selectedUserTab,
                            onSearch     = viewModel::setUserSearchQuery,
                            onTabChange  = viewModel::setUserTab,
                            onDelete     = { confirmDeleteUser = it },
                            customColors  = customColors,
                            dimensions    = dimensions
                        )
                        AdminTab.BABIES -> AdminBabiesTab(
                            babies       = state.filteredBabies,
                            searchQuery  = state.babySearchQuery,
                            selectedTab  = state.selectedBabyTab,
                            onSearch     = viewModel::setBabySearchQuery,
                            onTabChange  = viewModel::setBabyTab,
                            onDelete     = { confirmDeleteBaby = it },
                            customColors  = customColors,
                            dimensions    = dimensions
                        )
                        AdminTab.SETTINGS -> AdminSettingsTab(
                            adminName    = state.adminName,
                            adminEmail   = state.adminEmail,
                            onLogout     = { confirmLogout = true },
                            customColors  = customColors,
                            dimensions    = dimensions
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    adminName   : String,
    adminEmail  : String,
    onLogout    : () -> Unit,
    onRefresh   : () -> Unit,
    isRefreshing: Boolean,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(dimensions.spacingSmall))
                    Text(
                        "Admin Panel",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = customColors.accentGradientStart
                    )
                }
                Text(
                    adminName.ifBlank { adminEmail },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        },
        actions = {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(dimensions.iconMedium).padding(4.dp),
                    color       = customColors.accentGradientStart,
                    strokeWidth = dimensions.borderWidthMedium
                )
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, null, tint = customColors.accentGradientStart)
                }
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = MaterialTheme.colorScheme.error)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = customColors.accentGradientStart.copy(alpha = 0.10f)
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminBottomBar(
    selectedTab   : AdminTab,
    onTabSelected : (AdminTab) -> Unit,
    customColors  : CustomColors
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        AdminTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick  = { onTabSelected(tab) },
                icon     = {
                    Icon(tab.icon, contentDescription = tab.label)
                },
                label  = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = customColors.accentGradientStart,
                    selectedTextColor   = customColors.accentGradientStart,
                    indicatorColor      = customColors.accentGradientStart.copy(0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminDashboardTab(
    stats       : AdminDashboardStats,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        item {
            Text(
                "Overview",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                AdminStatCard(
                    emoji = "👥", label = "Total Users", value = stats.totalUsers.toString(),
                    color = customColors.accentGradientStart, modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    emoji = "👶", label = "Total Babies", value = stats.totalBabies.toString(),
                    color = customColors.info, modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                AdminStatCard(
                    emoji = "👨‍👩‍👧", label = "Parents", value = stats.totalParents.toString(),
                    color = customColors.success, modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    emoji = "✅", label = "Verified", value = stats.verifiedUsers.toString(),
                    color = customColors.accentGradientEnd, modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                AdminStatCard(
                    emoji = "🟢", label = "Active Babies", value = stats.activeBabies.toString(),
                    color = customColors.success, modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    emoji = "📦", label = "Archived", value = stats.archivedBabies.toString(),
                    color = customColors.warning, modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(dimensions.spacingMedium)) }

        item {
            Text(
                "Quick Summary",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensions.borderWidthThin)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    SummaryRow(label = "Total registered users",  value = stats.totalUsers.toString(),     icon = "👥")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    SummaryRow(label = "Parent accounts",         value = stats.totalParents.toString(),   icon = "👨‍👩‍👧")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    SummaryRow(label = "Total baby profiles",     value = stats.totalBabies.toString(),    icon = "👶")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    SummaryRow(label = "Active baby profiles",    value = stats.activeBabies.toString(),   icon = "🟢")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    SummaryRow(label = "Archived baby profiles",  value = stats.archivedBabies.toString(), icon = "📦")
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    emoji   : String,
    label   : String,
    value   : String,
    color   : Color,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = color.copy(0.10f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(dimensions.spacingXSmall))
            Text(
                value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            Text(
                label,
                style   = MaterialTheme.typography.labelSmall,
                color   = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, icon: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = dimensions.borderWidthMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Text(icon)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
        }
        Text(
            value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Users Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminUsersTab(
    users       : List<UserResponse>,
    searchQuery : String,
    selectedTab : AdminUserTab,
    onSearch    : (String) -> Unit,
    onTabChange : (AdminUserTab) -> Unit,
    onDelete    : (String) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearch,
            placeholder   = { Text("Search users…") },
            leadingIcon   = { Icon(Icons.Default.Search, null) },
            trailingIcon  = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { onSearch("") }) { Icon(Icons.Default.Clear, null) } }
            } else null,
            singleLine = true,
            modifier   = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            shape      = RoundedCornerShape(dimensions.buttonCornerRadius)
        )

        // Tab row
        ScrollableTabRow(
            selectedTabIndex = AdminUserTab.entries.indexOf(selectedTab),
            edgePadding      = dimensions.screenPadding,
            containerColor   = Color.Transparent,
            contentColor     = customColors.accentGradientStart
        ) {
            AdminUserTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick  = { onTabChange(tab) },
                    text     = { Text(tab.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        // Count
        Text(
            "${users.size} user(s)",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            modifier = Modifier.padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingXSmall)
        )

        LazyColumn(
            contentPadding      = PaddingValues(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            if (users.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(dimensions.spacingXLarge), Alignment.Center) {
                        Text("No users found", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
            } else {
                items(users, key = { it.userId }) { user ->
                    AdminUserCard(user = user, onDelete = { onDelete(user.userId) }, customColors = customColors, dimensions = dimensions)
                }
            }
            item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
        }
    }
}

@Composable
private fun AdminUserCard(
    user        : UserResponse,
    onDelete    : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val roleColor = when {
        user.role.equals("admin", ignoreCase = true) -> MaterialTheme.colorScheme.error
        else -> customColors.success
    }
    val roleEmoji = if (user.role.equals("admin", ignoreCase = true)) "🛡️" else "👤"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.borderWidthThin)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.avatarSmall + dimensions.spacingSmall)
                    .clip(CircleShape)
                    .background(roleColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(roleEmoji, style = MaterialTheme.typography.bodyMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.fullName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    user.email,
                    style   = MaterialTheme.typography.bodySmall,
                    color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    Surface(shape = RoundedCornerShape(50), color = roleColor.copy(0.12f)) {
                        Text(
                            user.role.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = roleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (user.isActive) {
                        Surface(shape = RoundedCornerShape(50), color = customColors.success.copy(0.12f)) {
                            Text(
                                "Verified",
                                modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = customColors.success
                            )
                        }
                    }
                }
            }
            // Don't allow deleting admins from here as a safety measure
            if (!user.role.equals("admin", ignoreCase = true)) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(0.7f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Babies Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminBabiesTab(
    babies      : List<BabyResponse>,
    searchQuery : String,
    selectedTab : AdminBabyTab,
    onSearch    : (String) -> Unit,
    onTabChange : (AdminBabyTab) -> Unit,
    onDelete    : (String) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearch,
            placeholder   = { Text("Search babies…") },
            leadingIcon   = { Icon(Icons.Default.Search, null) },
            trailingIcon  = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { onSearch("") }) { Icon(Icons.Default.Clear, null) } }
            } else null,
            singleLine = true,
            modifier   = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            shape      = RoundedCornerShape(dimensions.buttonCornerRadius)
        )

        ScrollableTabRow(
            selectedTabIndex = AdminBabyTab.entries.indexOf(selectedTab),
            edgePadding      = dimensions.screenPadding,
            containerColor   = Color.Transparent,
            contentColor     = customColors.accentGradientStart
        ) {
            AdminBabyTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick  = { onTabChange(tab) },
                    text     = { Text(tab.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Text(
            "${babies.size} profile(s)",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            modifier = Modifier.padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingXSmall)
        )

        LazyColumn(
            contentPadding      = PaddingValues(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            if (babies.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(dimensions.spacingXLarge), Alignment.Center) {
                        Text("No baby profiles found", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
            } else {
                items(babies, key = { it.babyId }) { baby ->
                    AdminBabyCard(baby = baby, onDelete = { onDelete(baby.babyId) }, customColors = customColors, dimensions = dimensions)
                }
            }
            item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
        }
    }
}

@Composable
private fun AdminBabyCard(
    baby        : BabyResponse,
    onDelete    : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isFemale   = baby.gender.equals("GIRL", ignoreCase = true) || baby.gender.equals("FEMALE", ignoreCase = true)
    val genderEmoji = if (isFemale) "👧" else "👦"
    val statusColor = if (baby.isActive) customColors.success else customColors.warning

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.borderWidthThin)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.avatarSmall + dimensions.spacingSmall)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(genderEmoji, style = MaterialTheme.typography.bodyMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    baby.fullName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Parent: ${baby.parentName}",
                    style   = MaterialTheme.typography.bodySmall,
                    color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    Text(
                        "${baby.ageInMonths}mo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                        Text(
                            if (baby.isActive) "Active" else "Archived",
                            modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(0.7f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminSettingsTab(
    adminName   : String,
    adminEmail  : String,
    onLogout    : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // Profile card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(dimensions.cardCornerRadius),
            colors    = CardDefaults.cardColors(containerColor = customColors.accentGradientStart.copy(0.08f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimensions.spacingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.avatarLarge)
                        .clip(CircleShape)
                        .background(customColors.accentGradientStart.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", style = MaterialTheme.typography.headlineMedium)
                }
                Column {
                    Text(
                        adminName.ifBlank { "Admin" },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        adminEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.error.copy(0.12f)) {
                        Text(
                            "Administrator",
                            modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Info card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(dimensions.cardCornerRadius),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensions.borderWidthThin)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AdminSettingsRow(icon = Icons.Default.AdminPanelSettings, label = "Role", value = "Administrator")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                AdminSettingsRow(icon = Icons.Default.Email, label = "Email", value = adminEmail.ifBlank { "—" })
            }
        }

        Spacer(Modifier.height(dimensions.spacingMedium))

        // Logout button
        Button(
            onClick  = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(dimensions.iconMedium))
            Spacer(Modifier.width(dimensions.spacingSmall))
            Text("Log Out", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminSettingsRow(icon: ImageVector, label: String, value: String) {
    val dimensions = LocalDimensions.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dimensions.iconMedium))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminLoadingState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Loading admin data…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}