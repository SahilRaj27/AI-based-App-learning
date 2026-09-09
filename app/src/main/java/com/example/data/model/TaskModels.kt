package com.example.data.model

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}

data class SyncLogEntry(
    val id: String,
    val timestamp: Long,
    val action: String, // "UPLOAD", "DOWNLOAD", "CONFLICT_RESOLVED"
    val itemsCount: Int,
    val details: String,
    val isSuccess: Boolean
)

data class CloudSyncStatus(
    val state: SyncState = SyncState.IDLE,
    val lastSyncTime: Long? = null,
    val pendingUploadsCount: Int = 0,
    val totalSyncedCount: Int = 0,
    val isAutoSyncEnabled: Boolean = true,
    val deviceId: String = "Pixel-Device-01",
    val errorMessage: String? = null
)

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}
