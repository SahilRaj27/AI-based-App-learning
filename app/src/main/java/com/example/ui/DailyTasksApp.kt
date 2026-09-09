package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TaskEntity
import com.example.data.model.SyncState
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AuthAccountSheet
import com.example.ui.components.CloudSyncSheet
import com.example.ui.components.DateSelector
import com.example.ui.components.GeminiChatSheet
import com.example.ui.components.StatsCard
import com.example.ui.components.TaskItem
import com.example.ui.theme.CloudErrorRed
import com.example.ui.theme.CloudPendingAmber
import com.example.ui.theme.CloudSyncedGreen
import com.example.ui.theme.CloudSyncingBlue
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.FuturisticMeshBackground
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.iosGlassmorphic
import com.example.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTasksApp(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    // State collections
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val visibleTasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()
    val unsyncedCount by viewModel.unsyncedCount.collectAsStateWithLifecycle()

    // Auth & Gemini State collections
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val selectedGeminiModel by viewModel.selectedGeminiModel.collectAsStateWithLifecycle()

    // Dialog & Sheet States
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showCloudSyncSheet by remember { mutableStateOf(false) }
    var showGeminiChatSheet by remember { mutableStateOf(false) }
    var showAuthAccountSheet by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Notification Permission Check (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Sync rotation animation for TopAppBar sync badge
    val infiniteTransition = rememberInfiniteTransition(label = "top_sync_spin")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    FuturisticMeshBackground(
        modifier = modifier.fillMaxSize(),
        isDark = isDark
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0x800F1426) else Color(0x99FFFFFF)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color(0x20FFFFFF) else Color(0x30CBD5E1),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                ) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Daily Tasks",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.5).sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "Plan, track & sync across devices",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        },
                        actions = {
                            // Gemini AI Assistant button
                            IconButton(
                                onClick = { showGeminiChatSheet = true },
                                modifier = Modifier.testTag("gemini_chat_top_btn")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini AI Assistant",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Search toggle button
                            IconButton(
                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) viewModel.setSearchQuery("")
                                },
                                modifier = Modifier.testTag("search_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Cloud Sync status badge pill with iOS Gloss
                            val badgeInfo = when (cloudSyncStatus.state) {
                                SyncState.SYNCING -> SyncBadgeInfo(
                                    bg = CloudSyncingBlue.copy(alpha = 0.2f),
                                    border = CloudSyncingBlue.copy(alpha = 0.6f),
                                    text = "Syncing",
                                    icon = Icons.Default.Sync,
                                    tint = CloudSyncingBlue
                                )
                                SyncState.SUCCESS -> SyncBadgeInfo(
                                    bg = CloudSyncedGreen.copy(alpha = 0.18f),
                                    border = CloudSyncedGreen.copy(alpha = 0.5f),
                                    text = "Synced",
                                    icon = Icons.Default.CloudDone,
                                    tint = CloudSyncedGreen
                                )
                                SyncState.ERROR -> SyncBadgeInfo(
                                    bg = CloudErrorRed.copy(alpha = 0.2f),
                                    border = CloudErrorRed.copy(alpha = 0.6f),
                                    text = "Error",
                                    icon = Icons.Default.CloudSync,
                                    tint = CloudErrorRed
                                )
                                SyncState.IDLE -> {
                                    if (unsyncedCount > 0) {
                                        SyncBadgeInfo(
                                            bg = CloudPendingAmber.copy(alpha = 0.2f),
                                            border = CloudPendingAmber.copy(alpha = 0.6f),
                                            text = "$unsyncedCount",
                                            icon = Icons.Default.CloudUpload,
                                            tint = CloudPendingAmber
                                        )
                                    } else {
                                        SyncBadgeInfo(
                                            bg = CloudSyncedGreen.copy(alpha = 0.18f),
                                            border = CloudSyncedGreen.copy(alpha = 0.5f),
                                            text = "Synced",
                                            icon = Icons.Default.CloudDone,
                                            tint = CloudSyncedGreen
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(badgeInfo.bg)
                                    .border(1.dp, badgeInfo.border, RoundedCornerShape(16.dp))
                                    .clickable { showCloudSyncSheet = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                                    .testTag("cloud_sync_badge_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = badgeInfo.icon,
                                        contentDescription = null,
                                        tint = badgeInfo.tint,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .then(
                                                if (cloudSyncStatus.state == SyncState.SYNCING)
                                                    Modifier.rotate(syncRotation)
                                                else Modifier
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = badgeInfo.text,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = badgeInfo.tint,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Account & Firebase Auth button
                            IconButton(
                                onClick = { showAuthAccountSheet = true },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("auth_account_top_btn")
                            ) {
                                val user = authUiState.currentUser
                                if (user != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (user.displayName?.take(1) ?: user.email?.take(1) ?: "U").uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Account & Cloud Sync",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Secondary AI Planner FAB with Glossy Glass
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isDark) Brush.linearGradient(listOf(Color(0x501E243A), Color(0x301E243A)))
                                else Brush.linearGradient(listOf(Color(0xD0FFFFFF), Color(0xA0F1F5F9)))
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(NeonViolet.copy(alpha = 0.8f), NeonCyan.copy(alpha = 0.5f))
                                ),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { showGeminiChatSheet = true }
                            .testTag("gemini_fab_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Gemini AI",
                            tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Primary Add Task FAB with Liquid Neon iOS Sheen
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF06B6D4))
                                )
                            )
                            .border(
                                1.2.dp,
                                Brush.linearGradient(
                                    listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.1f))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .drawWithContent {
                                drawContent()
                                // Specular top highlight
                                val sheenHeight = size.height * 0.45f
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                        startY = 0f,
                                        endY = sheenHeight
                                    ),
                                    size = Size(size.width, sheenHeight)
                                )
                            }
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                taskToEdit = null
                                showAddTaskDialog = true
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .testTag("add_task_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Task",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.2.sp
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Collapsible Search Bar
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_text_field")
                )
            }

            // Notification Permission Banner (if Android 13+ and not granted)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable notifications for task reminders",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        TextButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                        ) {
                            Text("Enable", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Date Selector Bar (Today, Tomorrow, Past/Future days)
            DateSelector(
                selectedDateEpochDay = selectedDate,
                onSelectDate = { viewModel.selectDate(it) }
            )

            // Daily Progress and Filter Card
            StatsCard(
                stats = dailyStats,
                selectedFilter = selectedFilter,
                onSelectFilter = { viewModel.selectFilter(it) },
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) }
            )

            // Tasks List
            if (visibleTasks.isEmpty()) {
                EmptyStateView(
                    searchActive = searchQuery.isNotBlank(),
                    onAddTask = {
                        taskToEdit = null
                        showAddTaskDialog = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tasks_lazy_column")
                ) {
                    items(
                        items = visibleTasks,
                        key = { it.id }
                    ) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = {
                                taskToEdit = it
                                showAddTaskDialog = true
                            },
                            onDeleteTask = { id, title ->
                                viewModel.deleteTask(id, title)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            initialDateEpochDay = selectedDate,
            editingTask = taskToEdit,
            onDismiss = {
                showAddTaskDialog = false
                taskToEdit = null
            },
            onSaveTask = { title, desc, dateEpoch, priority, cat, hasReminder, reminderTime ->
                if (taskToEdit == null) {
                    viewModel.addTask(
                        title = title,
                        description = desc,
                        dateEpochDay = dateEpoch,
                        priority = priority,
                        category = cat,
                        hasReminder = hasReminder,
                        reminderTimeMillis = reminderTime
                    )
                } else {
                    val updated = taskToEdit!!.copy(
                        title = title,
                        description = desc,
                        dateEpochDay = dateEpoch,
                        priority = priority.name,
                        category = cat,
                        hasReminder = hasReminder,
                        reminderTimeMillis = if (hasReminder) reminderTime else null
                    )
                    viewModel.updateTask(updated)
                }
            }
        )
    }

    // Cloud Synchronization Management Sheet
    if (showCloudSyncSheet) {
        CloudSyncSheet(
            syncStatus = cloudSyncStatus,
            unsyncedCount = unsyncedCount,
            syncLogs = syncLogs,
            onSyncNow = { viewModel.syncNow() },
            onToggleAutoSync = { viewModel.toggleAutoSync(it) },
            onSimulateRemoteDevice = { viewModel.simulateRemoteDeviceSync() },
            onClearCloudData = { viewModel.clearCloudBackup() },
            onDismiss = { showCloudSyncSheet = false }
        )
    }

    // Gemini AI Planner Chat Sheet
    if (showGeminiChatSheet) {
        GeminiChatSheet(
            messages = chatMessages,
            isLoading = isChatLoading,
            selectedModel = selectedGeminiModel,
            onSelectModel = { viewModel.selectGeminiModel(it) },
            onSendMessage = { viewModel.sendChatMessage(it) },
            onClearChat = { viewModel.clearChatHistory() },
            onAddSuggestedTask = { suggestedTitle ->
                viewModel.addSuggestedTaskToDay(suggestedTitle, selectedDate)
            },
            onDismiss = { showGeminiChatSheet = false }
        )
    }

    // Firebase Authentication & Account Sheet
    if (showAuthAccountSheet) {
        AuthAccountSheet(
            authUiState = authUiState,
            onSignInGoogle = { viewModel.signInWithGoogle(context) },
            onSignInGuest = { viewModel.signInAnonymously() },
            onSignOut = { viewModel.signOut() },
            onDismiss = { showAuthAccountSheet = false }
        )
    }
    }
}

@Composable
private fun EmptyStateView(
    searchActive: Boolean,
    onAddTask: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .iosGlassmorphic(
                    cornerRadius = 24.dp,
                    isDark = isDark,
                    hasSpecularSheen = true,
                    borderAlpha = 0.8f
                )
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (isDark) NeonCyan.copy(alpha = 0.35f) else Color(0x356366F1),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    if (isDark) NeonCyan.copy(alpha = 0.7f) else Color(0x606366F1),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (searchActive) Icons.Default.Search else Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (searchActive) "No Matching Tasks" else "All Clear for Today",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (searchActive) "Try searching with a different keyword or reset active filters."
                    else "No tasks scheduled for this day yet. Stay ahead by planning your priority goals.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                if (!searchActive) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(onClick = onAddTask)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create First Task",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SyncBadgeInfo(
    val bg: Color,
    val border: Color,
    val text: String,
    val icon: ImageVector,
    val tint: Color
)


