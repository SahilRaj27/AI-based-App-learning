package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE dateEpochDay = :dateEpochDay AND isDeleted = 0 ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, lastModified DESC")
    fun getTasksForDay(dateEpochDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY dateEpochDay ASC, isCompleted ASC")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDeleted = 0")
    fun getTotalActiveTasksCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE hasReminder = 1 AND isCompleted = 0 AND isDeleted = 0 AND reminderTimeMillis > :afterTime")
    suspend fun getPendingReminders(afterTime: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isDeleted = 1, isSynced = 0, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteTask(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun hardDeleteTask(id: String)

    @Query("UPDATE tasks SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTasksAsSynced(ids: List<String>)

    @Query("DELETE FROM tasks WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun purgeSyncedDeletedTasks()

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksRaw(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}
