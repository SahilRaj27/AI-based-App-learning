package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val taskId = intent.getStringExtra(NotificationHelper.EXTRA_TASK_ID) ?: ""
        val taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskCategory = intent.getStringExtra(NotificationHelper.EXTRA_TASK_CATEGORY) ?: "General"

        when (action) {
            NotificationHelper.ACTION_TRIGGER_REMINDER -> {
                if (taskId.isNotEmpty()) {
                    NotificationHelper.showTaskNotification(context, taskId, taskTitle, taskCategory)
                }
            }

            NotificationHelper.ACTION_MARK_DONE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId.hashCode())

                if (taskId.isNotEmpty()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val task = db.taskDao().getTaskById(taskId)
                            if (task != null) {
                                val updated = task.copy(
                                    isCompleted = true,
                                    completedAt = System.currentTimeMillis(),
                                    isSynced = false,
                                    lastModified = System.currentTimeMillis()
                                )
                                db.taskDao().updateTask(updated)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            NotificationHelper.ACTION_SNOOZE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId.hashCode())

                if (taskId.isNotEmpty()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val task = db.taskDao().getTaskById(taskId)
                            if (task != null) {
                                val newReminderTime = System.currentTimeMillis() + (15 * 60 * 1000)
                                val updated = task.copy(
                                    reminderTimeMillis = newReminderTime,
                                    isSynced = false,
                                    lastModified = System.currentTimeMillis()
                                )
                                db.taskDao().updateTask(updated)
                                NotificationHelper.scheduleReminder(context, updated)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val now = System.currentTimeMillis()
                        val pendingTasks = db.taskDao().getPendingReminders(now)
                        for (task in pendingTasks) {
                            NotificationHelper.scheduleReminder(context, task)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
