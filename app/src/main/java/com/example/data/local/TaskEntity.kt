package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Priority

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val priority: String = Priority.MEDIUM.name,
    val category: String = "General",
    val hasReminder: Boolean = false,
    val reminderTimeMillis: Long? = null,
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val cloudVersion: Long = 1L
)
