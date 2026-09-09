package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.TaskEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreManager(private val context: Context) {

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w("FirestoreManager", "Firestore not available: ${e.message}")
        }
    }

    suspend fun saveTask(userId: String, task: TaskEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            return@withContext Result.success(Unit) // Offline/local fallback
        }

        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "description" to task.description,
                "dateEpochDay" to task.dateEpochDay,
                "isCompleted" to task.isCompleted,
                "priority" to task.priority,
                "category" to task.category,
                "hasReminder" to task.hasReminder,
                "reminderTimeMillis" to task.reminderTimeMillis,
                "lastModified" to task.lastModified,
                "isDeleted" to task.isDeleted,
                "userId" to userId
            )

            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id)
                .set(taskMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to save task to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(userId: String, taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            return@withContext Result.success(Unit)
        }

        try {
            val taskMap = hashMapOf(
                "id" to taskId,
                "isDeleted" to true,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .set(taskMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to delete task in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun fetchTasks(userId: String): Result<List<TaskEntity>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.success(emptyList())

        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("isDeleted", false)
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    TaskEntity(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dateEpochDay = doc.getLong("dateEpochDay") ?: 0L,
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        priority = doc.getString("priority") ?: "MEDIUM",
                        category = doc.getString("category") ?: "General",
                        hasReminder = doc.getBoolean("hasReminder") ?: false,
                        reminderTimeMillis = doc.getLong("reminderTimeMillis"),
                        lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                        isSynced = true,
                        isDeleted = doc.getBoolean("isDeleted") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(tasks)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error fetching tasks from Firestore", e)
            Result.failure(e)
        }
    }

    fun listenToTasks(userId: String, onTasksChanged: (List<TaskEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("isDeleted", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreManager", "Listen failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val tasks = snapshot.documents.mapNotNull { doc ->
                            try {
                                TaskEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    dateEpochDay = doc.getLong("dateEpochDay") ?: 0L,
                                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                                    priority = doc.getString("priority") ?: "MEDIUM",
                                    category = doc.getString("category") ?: "General",
                                    hasReminder = doc.getBoolean("hasReminder") ?: false,
                                    reminderTimeMillis = doc.getLong("reminderTimeMillis"),
                                    lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                                    isSynced = true,
                                    isDeleted = false
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        onTasksChanged(tasks)
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Could not start snapshot listener", e)
            null
        }
    }
}
