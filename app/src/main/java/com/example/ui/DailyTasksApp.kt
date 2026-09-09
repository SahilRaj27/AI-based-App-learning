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
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SkySecondary
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

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Tasks",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Plan, track & sync across devices",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(IndigoPrimary, SkySecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
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

                    // Cloud Sync status badge pill
                    Surface(
                        onClick = { showCloudSyncSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        color = when (cloudSyncStatus.state) {
                            SyncState.SYNCING -> CloudSyncingBlue.copy(alpha = 0.15f)
                            SyncState.SUCCESS -> CloudSyncedGreen.copy(alpha = 0.15f)
                            SyncState.ERROR -> CloudErrorRed.copy(alpha = 0.15f)
                            SyncState.IDLE -> if (unsyncedCount > 0) CloudPendingAmber.copy(alpha = 0.15f) else CloudSyncedGreen.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.testTag("cloud_sync_badge_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (cloudSyncStatus.state == SyncState.SYNCING) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing",
                                    tint = CloudSyncingBlue,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .rotate(syncRotation)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Syncing",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CloudSyncingBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            } else {
                                val icon = if (unsyncedCount > 0) Icons.Default.CloudUpload else Icons.Default.CloudDone
                                val tint = if (unsyncedCount > 0) CloudPendingAmber else CloudSyncedGreen
                                val text = if (unsyncedCount > 0) "$unsyncedCount" else "Synced"

                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = tint,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (user.displayName?.take(1) ?: user.email?.take(1) ?: "U").uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary AI Planner FAB
                FloatingActionButton(
                    onClick = { showGeminiChatSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("gemini_fab_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask Gemini AI",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Primary Add Task FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        taskToEdit = null
                        showAddTaskDialog = true
                    },
                    containerColor = IndigoPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                    text = { Text("Add Task", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("add_task_fab")
                )
            }
        },
        modifier = modifier.fillMaxSize()
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

@Composable
private fun EmptyStateView(
    searchActive: Boolean,
    onAddTask: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (searchActive) Icons.Default.Search else Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchActive) "No matching tasks" else "All clear for this day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchActive) "Try searching with a different keyword or clear the search filter."
                else "No scheduled tasks found. Tap below to create your daily priorities.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )

            if (!searchActive) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onAddTask) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add your first task", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
