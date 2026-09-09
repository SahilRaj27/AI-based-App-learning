package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthUiState
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreManager
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiChatService
import com.example.data.gemini.GeminiModel
import com.example.data.gemini.MessageRole
import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity
import com.example.data.model.CloudSyncStatus
import com.example.data.model.Priority
import com.example.data.model.SyncLogEntry
import com.example.data.repository.TaskRepository
import com.example.receiver.NotificationHelper
import com.example.sync.CloudSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class DailyTaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val completionPercentage: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    application: Application,
    private val repository: TaskRepository,
    private val authManager: FirebaseAuthManager,
    private val geminiService: GeminiChatService
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow(LocalDate.now().toEpochDay())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    // Auth state
    val authUiState: StateFlow<AuthUiState> = authManager.uiState

    // Gemini Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.MODEL,
                content = "Hi there! I'm your Gemini productivity assistant. I can help organize your day, break big goals into smaller steps, or suggest realistic routines. How can I assist you today?",
                suggestedTasks = listOf(
                    "Plan my top 3 priorities for today",
                    "Break down a project into steps",
                    "Suggest a healthy morning routine"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _selectedGeminiModel = MutableStateFlow(GeminiModel.FLASH)
    val selectedGeminiModel: StateFlow<GeminiModel> = _selectedGeminiModel.asStateFlow()

    val cloudSyncStatus: StateFlow<CloudSyncStatus> = repository.cloudSyncStatus
    val syncLogs: StateFlow<List<SyncLogEntry>> = repository.syncLogs

    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Raw tasks for the selected date
    private val rawTasksForSelectedDate = _selectedDate.flatMapLatest { epochDay ->
        repository.getTasksForDay(epochDay)
    }

    // Daily statistics for the currently selected date
    val dailyStats: StateFlow<DailyTaskStats> = rawTasksForSelectedDate.combine(_selectedDate) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed
        val percentage = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        DailyTaskStats(
            total = total,
            completed = completed,
            pending = pending,
            completionPercentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyTaskStats())

    // Filtered tasks display list
    val visibleTasks: StateFlow<List<TaskEntity>> = combine(
        rawTasksForSelectedDate,
        _searchQuery,
        _selectedCategory,
        _selectedFilter
    ) { tasks, query, category, filter ->
        tasks.filter { task ->
            val matchesQuery = if (query.isBlank()) true else {
                task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)
            }
            val matchesCategory = category == null || task.category == category
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }
            matchesQuery && matchesCategory && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Notification channel setup
        NotificationHelper.createNotificationChannel(application)

        // Bind user provider to repository for Firestore persistence
        repository.setCurrentUserProvider {
            authManager.uiState.value.currentUser?.uid
        }

        // Seed initial sample tasks if empty
        viewModelScope.launch {
            repository.seedInitialSampleTasksIfEmpty(LocalDate.now().toEpochDay())
        }
    }

    fun selectDate(epochDay: Long) {
        _selectedDate.value = epochDay
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun addTask(
        title: String,
        description: String,
        dateEpochDay: Long,
        priority: Priority,
        category: String,
        hasReminder: Boolean,
        reminderTimeMillis: Long?
    ) {
        if (title.isBlank()) return

        val newTask = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            dateEpochDay = dateEpochDay,
            isCompleted = false,
            priority = priority.name,
            category = category,
            hasReminder = hasReminder,
            reminderTimeMillis = if (hasReminder) reminderTimeMillis else null,
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.addTask(newTask)
            _snackbarMessages.emit("Task created: ${newTask.title}")
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            _snackbarMessages.emit("Task updated")
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            val msg = if (!task.isCompleted) "Completed: ${task.title}" else "Marked pending: ${task.title}"
            _snackbarMessages.emit(msg)
        }
    }

    fun deleteTask(id: String, taskTitle: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            _snackbarMessages.emit("Deleted '$taskTitle'")
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = repository.performCloudSync(isManual = true)
            result.onSuccess { affected ->
                _snackbarMessages.emit("Cloud Sync complete: $affected changes synchronized")
            }.onFailure { err ->
                _snackbarMessages.emit("Sync failed: ${err.message}")
            }
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        repository.setAutoSync(enabled)
        viewModelScope.launch {
            _snackbarMessages.emit(if (enabled) "Auto-Sync enabled" else "Auto-Sync paused")
        }
    }

    fun simulateRemoteDeviceSync() {
        viewModelScope.launch {
            val title = repository.simulateRemoteUpdate()
            _snackbarMessages.emit("Simulated remote device added: $title")
            repository.performCloudSync(isManual = true)
        }
    }

    fun clearCloudBackup() {
        viewModelScope.launch {
            repository.clearCloudData()
            _snackbarMessages.emit("Cloud backup data cleared")
        }
    }

    // --- Firebase Authentication Methods ---
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activityContext)
            result.onSuccess { user ->
                _snackbarMessages.emit("Signed in as ${user.displayName ?: user.email}")
                repository.performCloudSync(isManual = false)
            }.onFailure { err ->
                _snackbarMessages.emit("Google Sign-In: ${err.message}")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            val result = authManager.signInAnonymously()
            result.onSuccess { user ->
                _snackbarMessages.emit("Signed in as ${user.displayName}")
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        viewModelScope.launch {
            _snackbarMessages.emit("Signed out")
        }
    }

    // --- Gemini Chatbot Methods ---
    fun selectGeminiModel(model: GeminiModel) {
        _selectedGeminiModel.value = model
    }

    fun sendChatMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank() || _isChatLoading.value) return

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = trimmed
        )

        val updatedHistory = _chatMessages.value + userMessage
        _chatMessages.value = updatedHistory
        _isChatLoading.value = true

        viewModelScope.launch {
            val result = geminiService.sendMessage(
                history = updatedHistory,
                userMessage = trimmed,
                selectedModel = _selectedGeminiModel.value
            )

            result.onSuccess { modelMsg ->
                _chatMessages.value = _chatMessages.value + modelMsg
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    role = MessageRole.MODEL,
                    content = "Sorry, I couldn't reach the Gemini service (${err.message}). You can still ask questions or tap suggestions below."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                role = MessageRole.MODEL,
                content = "Chat cleared! How can I help you organize your tasks today?",
                suggestedTasks = listOf(
                    "Break down a big task into steps",
                    "Organize tasks by priority",
                    "Plan my afternoon focus block"
                )
            )
        )
    }

    fun addSuggestedTaskToDay(taskTitle: String, targetEpochDay: Long = _selectedDate.value) {
        addTask(
            title = taskTitle,
            description = "Suggested by Gemini AI",
            dateEpochDay = targetEpochDay,
            priority = Priority.MEDIUM,
            category = "General",
            hasReminder = false,
            reminderTimeMillis = null
        )
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val cloudManager = CloudSyncManager(db.taskDao())
                    val firestoreManager = FirestoreManager(application)
                    val authManager = FirebaseAuthManager(application)
                    val geminiService = GeminiChatService()
                    val repo = TaskRepository(
                        context = application,
                        taskDao = db.taskDao(),
                        cloudSyncManager = cloudManager,
                        firestoreManager = firestoreManager
                    )
                    return TaskViewModel(
                        application = application,
                        repository = repo,
                        authManager = authManager,
                        geminiService = geminiService
                    ) as T
                }
            }
        }
    }
}
