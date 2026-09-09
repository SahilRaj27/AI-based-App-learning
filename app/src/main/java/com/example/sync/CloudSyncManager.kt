package com.example.sync

import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.model.CloudSyncStatus
import com.example.data.model.Priority
import com.example.data.model.SyncLogEntry
import com.example.data.model.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Cloud Synchronization with conflict resolution, offline queue,
 * activity logging, and bidirectional multi-device sync emulation.
 */
class CloudSyncManager(private val taskDao: TaskDao) {

    // Simulated persistent Cloud Datastore (shared across sync cycles)
    private val cloudStore = ConcurrentHashMap<String, TaskEntity>()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus())
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<SyncLogEntry>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogEntry>> = _syncLogs.asStateFlow()

    init {
        // Seed initial cloud data with realistic tasks if cloud is empty
        seedCloudDataIfEmpty()
    }

    private fun seedCloudDataIfEmpty() {
        if (cloudStore.isEmpty()) {
            val today = LocalDate.now().toEpochDay()
            val seedTask1 = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Sync team deliverables with manager",
                description = "Discuss Q3 progress and upcoming sprint backlog",
                dateEpochDay = today,
                isCompleted = false,
                priority = Priority.HIGH.name,
                category = "Work",
                isSynced = true,
                lastModified = System.currentTimeMillis() - 3600_000
            )
            val seedTask2 = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Hydrate and take afternoon walk",
                description = "30 minutes brisk outdoor walk to reset focus",
                dateEpochDay = today,
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 7200_000,
                priority = Priority.LOW.name,
                category = "Health",
                isSynced = true,
                lastModified = System.currentTimeMillis() - 7200_000
            )
            cloudStore[seedTask1.id] = seedTask1
            cloudStore[seedTask2.id] = seedTask2
        }
    }

    suspend fun performSync(isManual: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        if (_syncStatus.value.state == SyncState.SYNCING) {
            return@withContext Result.success(0)
        }

        _syncStatus.value = _syncStatus.value.copy(
            state = SyncState.SYNCING,
            errorMessage = null
        )

        val startTime = System.currentTimeMillis()

        try {
            // Realistic cloud network latency simulation (400-800ms)
            delay(650)

            // Step 1: Query local unsynced tasks (including tombstones)
            val localUnsynced = taskDao.getUnsyncedTasks()
            var uploadedCount = 0

            for (localTask in localUnsynced) {
                if (localTask.isDeleted) {
                    // Tombstone: remove from cloud store
                    cloudStore.remove(localTask.id)
                    uploadedCount++
                } else {
                    // Push to cloud or resolve conflict if remote exists
                    val remote = cloudStore[localTask.id]
                    if (remote == null || localTask.lastModified >= remote.lastModified) {
                        cloudStore[localTask.id] = localTask.copy(isSynced = true)
                        uploadedCount++
                    }
                }
            }

            // Mark local successfully uploaded tasks as synced
            if (localUnsynced.isNotEmpty()) {
                val syncedIds = localUnsynced.map { it.id }
                taskDao.markTasksAsSynced(syncedIds)
                taskDao.purgeSyncedDeletedTasks()
            }

            // Step 2: Download remote tasks that are either missing locally or newer remotely
            val localTasksRaw = taskDao.getAllTasksRaw().associateBy { it.id }
            var downloadedCount = 0

            for ((remoteId, remoteTask) in cloudStore) {
                val local = localTasksRaw[remoteId]
                if (local == null) {
                    // New item from cloud
                    taskDao.insertTask(remoteTask.copy(isSynced = true))
                    downloadedCount++
                } else if (!local.isDeleted && remoteTask.lastModified > local.lastModified) {
                    // Cloud has a newer version (conflict resolved: cloud wins)
                    taskDao.updateTask(remoteTask.copy(isSynced = true))
                    downloadedCount++
                }
            }

            val totalAffected = uploadedCount + downloadedCount
            val now = System.currentTimeMillis()

            _syncStatus.value = _syncStatus.value.copy(
                state = SyncState.SUCCESS,
                lastSyncTime = now,
                totalSyncedCount = cloudStore.size,
                errorMessage = null
            )

            // Add to sync activity logs
            val logEntry = SyncLogEntry(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                action = if (isManual) "MANUAL_SYNC" else "AUTO_SYNC",
                itemsCount = totalAffected,
                details = "Uploaded $uploadedCount, Downloaded $downloadedCount items (Cloud total: ${cloudStore.size})",
                isSuccess = true
            )
            _syncLogs.value = listOf(logEntry) + _syncLogs.value.take(20)

            // Auto-reset state back to IDLE after 2.5 seconds
            delay(2500)
            if (_syncStatus.value.state == SyncState.SUCCESS) {
                _syncStatus.value = _syncStatus.value.copy(state = SyncState.IDLE)
            }

            Result.success(totalAffected)
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            _syncStatus.value = _syncStatus.value.copy(
                state = SyncState.ERROR,
                errorMessage = e.localizedMessage ?: "Sync failed"
            )
            val errorLog = SyncLogEntry(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                action = "SYNC_FAILED",
                itemsCount = 0,
                details = e.localizedMessage ?: "Network error during sync",
                isSuccess = false
            )
            _syncLogs.value = listOf(errorLog) + _syncLogs.value.take(20)
            Result.failure(e)
        }
    }

    /**
     * Simulates another device (e.g. tablet or web dashboard) adding a task to the user's cloud account.
     * When the user clicks "Sync Now" or auto-sync triggers, this task appears locally.
     */
    suspend fun simulateRemoteDeviceAction(): String = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toEpochDay()
        val simulatedTasks = listOf(
            Pair("Quarterly financial budget review", "Work"),
            Pair("Dentist appointment prep & paperwork", "Personal"),
            Pair("Order grocery delivery for week", "Shopping"),
            Pair("Evening mindfulness meditation 15m", "Health")
        )
        val selected = simulatedTasks.random()
        val remoteTaskId = UUID.randomUUID().toString()
        val remoteTask = TaskEntity(
            id = remoteTaskId,
            title = "[Cloud] ${selected.first}",
            description = "Synced from secondary device (Web / Cloud App)",
            dateEpochDay = today,
            isCompleted = false,
            priority = Priority.HIGH.name,
            category = selected.second,
            isSynced = true,
            lastModified = System.currentTimeMillis() + 1000
        )
        cloudStore[remoteTaskId] = remoteTask

        val log = SyncLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            action = "REMOTE_DEVICE_PUSH",
            itemsCount = 1,
            details = "Secondary device added '${remoteTask.title}' to cloud",
            isSuccess = true
        )
        _syncLogs.value = listOf(log) + _syncLogs.value.take(20)

        remoteTask.title
    }

    fun toggleAutoSync(enabled: Boolean) {
        _syncStatus.value = _syncStatus.value.copy(isAutoSyncEnabled = enabled)
    }

    suspend fun clearCloudStorage() = withContext(Dispatchers.IO) {
        cloudStore.clear()
        _syncStatus.value = _syncStatus.value.copy(
            totalSyncedCount = 0,
            lastSyncTime = System.currentTimeMillis()
        )
        val log = SyncLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            action = "CLOUD_WIPED",
            itemsCount = 0,
            details = "Cloud database wiped by user",
            isSuccess = true
        )
        _syncLogs.value = listOf(log) + _syncLogs.value.take(20)
    }

    fun getCloudItemCount(): Int = cloudStore.size
}
