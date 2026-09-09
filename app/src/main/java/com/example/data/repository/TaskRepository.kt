package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreManager
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.model.CloudSyncStatus
import com.example.data.model.SyncLogEntry
import com.example.receiver.NotificationHelper
import com.example.sync.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao,
    private val cloudSyncManager: CloudSyncManager,
    private val firestoreManager: FirestoreManager? = null,
    private var currentUserId: (() -> String?)? = null
) {
    val cloudSyncStatus: StateFlow<CloudSyncStatus> = cloudSyncManager.syncStatus
    val syncLogs: StateFlow<List<SyncLogEntry>> = cloudSyncManager.syncLogs
    val unsyncedCount: Flow<Int> = taskDao.getUnsyncedCount()
    val totalActiveCount: Flow<Int> = taskDao.getTotalActiveTasksCount()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun setCurrentUserProvider(provider: () -> String?) {
        currentUserId = provider
    }

    fun getTasksForDay(dateEpochDay: Long): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDay(dateEpochDay)
    }

    fun getAllActiveTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllActiveTasks()
    }

    suspend fun addTask(task: TaskEntity) {
        val newTask = task.copy(
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )
        taskDao.insertTask(newTask)

        if (newTask.hasReminder && newTask.reminderTimeMillis != null) {
            NotificationHelper.scheduleReminder(context, newTask)
        }

        // Push to Firestore if authenticated
        currentUserId?.invoke()?.let { uid ->
            repositoryScope.launch {
                firestoreManager?.saveTask(uid, newTask)
            }
        }

        if (cloudSyncStatus.value.isAutoSyncEnabled) {
            repositoryScope.launch {
                cloudSyncManager.performSync(isManual = false)
            }
        }
    }

    suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)

        if (updated.hasReminder && updated.reminderTimeMillis != null && !updated.isCompleted) {
            NotificationHelper.scheduleReminder(context, updated)
        } else {
            NotificationHelper.cancelReminder(context, updated.id)
        }

        // Push to Firestore if authenticated
        currentUserId?.invoke()?.let { uid ->
            repositoryScope.launch {
                firestoreManager?.saveTask(uid, updated)
            }
        }

        if (cloudSyncStatus.value.isAutoSyncEnabled) {
            repositoryScope.launch {
                cloudSyncManager.performSync(isManual = false)
            }
        }
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val willBeCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = willBeCompleted,
            completedAt = if (willBeCompleted) System.currentTimeMillis() else null,
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)

        if (willBeCompleted) {
            NotificationHelper.cancelReminder(context, task.id)
        } else if (task.hasReminder && task.reminderTimeMillis != null) {
            NotificationHelper.scheduleReminder(context, updated)
        }

        currentUserId?.invoke()?.let { uid ->
            repositoryScope.launch {
                firestoreManager?.saveTask(uid, updated)
            }
        }

        if (cloudSyncStatus.value.isAutoSyncEnabled) {
            repositoryScope.launch {
                cloudSyncManager.performSync(isManual = false)
            }
        }
    }

    suspend fun deleteTask(id: String) {
        NotificationHelper.cancelReminder(context, id)
        taskDao.softDeleteTask(id, System.currentTimeMillis())

        currentUserId?.invoke()?.let { uid ->
            repositoryScope.launch {
                firestoreManager?.deleteTask(uid, id)
            }
        }

        if (cloudSyncStatus.value.isAutoSyncEnabled) {
            repositoryScope.launch {
                cloudSyncManager.performSync(isManual = false)
            }
        }
    }

    suspend fun performCloudSync(isManual: Boolean = true): Result<Int> {
        val uid = currentUserId?.invoke()
        if (uid != null && firestoreManager != null) {
            try {
                val firestoreTasks = firestoreManager.fetchTasks(uid).getOrNull()
                if (!firestoreTasks.isNullOrEmpty()) {
                    taskDao.insertTasks(firestoreTasks)
                }
            } catch (_: Exception) {}
        }
        return cloudSyncManager.performSync(isManual)
    }

    suspend fun simulateRemoteUpdate(): String {
        return cloudSyncManager.simulateRemoteDeviceAction()
    }

    fun setAutoSync(enabled: Boolean) {
        cloudSyncManager.toggleAutoSync(enabled)
    }

    suspend fun clearCloudData() {
        cloudSyncManager.clearCloudStorage()
    }

    suspend fun seedInitialSampleTasksIfEmpty(todayEpochDay: Long) {
        val raw = taskDao.getAllTasksRaw()
        if (raw.isEmpty()) {
            val sampleTasks = listOf(
                TaskEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Review daily schedule & goals",
                    description = "Prioritize top 3 high-impact deliverables for today",
                    dateEpochDay = todayEpochDay,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600000,
                    priority = com.example.data.model.Priority.HIGH.name,
                    category = "Work",
                    hasReminder = false,
                    isSynced = true
                ),
                TaskEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Team sync on cloud architecture",
                    description = "Discuss schema design and conflict resolution",
                    dateEpochDay = todayEpochDay,
                    isCompleted = false,
                    priority = com.example.data.model.Priority.HIGH.name,
                    category = "Work",
                    hasReminder = true,
                    reminderTimeMillis = System.currentTimeMillis() + (2 * 3600 * 1000),
                    isSynced = true
                ),
                TaskEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "30-minute cardio or gym workout",
                    description = "Stay active and hit 8,000 steps minimum",
                    dateEpochDay = todayEpochDay,
                    isCompleted = false,
                    priority = com.example.data.model.Priority.MEDIUM.name,
                    category = "Health",
                    hasReminder = true,
                    reminderTimeMillis = System.currentTimeMillis() + (4 * 3600 * 1000),
                    isSynced = true
                ),
                TaskEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Buy fresh groceries & essentials",
                    description = "Milk, greens, olive oil, and coffee beans",
                    dateEpochDay = todayEpochDay,
                    isCompleted = false,
                    priority = com.example.data.model.Priority.LOW.name,
                    category = "Shopping",
                    hasReminder = false,
                    isSynced = false
                )
            )
            taskDao.insertTasks(sampleTasks)
            for (t in sampleTasks) {
                if (t.hasReminder && t.reminderTimeMillis != null) {
                    NotificationHelper.scheduleReminder(context, t)
                }
            }
        }
    }
}
