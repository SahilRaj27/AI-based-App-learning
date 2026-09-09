package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.data.model.Priority
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialDateEpochDay: Long,
    editingTask: TaskEntity? = null,
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        description: String,
        dateEpochDay: Long,
        priority: Priority,
        category: String,
        hasReminder: Boolean,
        reminderTimeMillis: Long?
    ) -> Unit
) {
    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(editingTask?.category ?: "Work") }
    var selectedPriority by remember {
        mutableStateOf(
            editingTask?.priority?.let {
                try { Priority.valueOf(it) } catch (_: Exception) { Priority.MEDIUM }
            } ?: Priority.MEDIUM
        )
    }

    var selectedDateEpochDay by remember {
        mutableLongStateOf(editingTask?.dateEpochDay ?: initialDateEpochDay)
    }

    var hasReminder by remember {
        mutableStateOf(editingTask?.hasReminder ?: false)
    }

    // Default reminder: 1 hour from now or existing
    var reminderTimeMillis by remember {
        mutableLongStateOf(
            editingTask?.reminderTimeMillis ?: (System.currentTimeMillis() + 3600_000)
        )
    }

    val categories = listOf("Work", "Personal", "Health", "Shopping", "Finance", "General")
    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingTask == null) "New Daily Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Task Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs to be done?") },
                    placeholder = { Text("e.g., Finish project proposal") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Task Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Details (optional)") },
                    placeholder = { Text("Add any context or checklist items...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date Selection Row (Today, Tomorrow, Day After)
                Text(
                    text = "Scheduled Day",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateChoiceChip(
                        label = "Today",
                        isSelected = selectedDateEpochDay == todayEpoch,
                        onClick = { selectedDateEpochDay = todayEpoch }
                    )
                    DateChoiceChip(
                        label = "Tomorrow",
                        isSelected = selectedDateEpochDay == todayEpoch + 1,
                        onClick = { selectedDateEpochDay = todayEpoch + 1 }
                    )
                    val otherDate = LocalDate.ofEpochDay(selectedDateEpochDay)
                    val isOther = selectedDateEpochDay != todayEpoch && selectedDateEpochDay != todayEpoch + 1
                    DateChoiceChip(
                        label = if (isOther) otherDate.format(DateTimeFormatter.ofPattern("MMM d")) else "+2 Days",
                        isSelected = isOther,
                        onClick = {
                            if (!isOther) selectedDateEpochDay = todayEpoch + 2
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PriorityChoiceChip(
                        priority = Priority.LOW,
                        isSelected = selectedPriority == Priority.LOW,
                        onClick = { selectedPriority = Priority.LOW }
                    )
                    PriorityChoiceChip(
                        priority = Priority.MEDIUM,
                        isSelected = selectedPriority == Priority.MEDIUM,
                        onClick = { selectedPriority = Priority.MEDIUM }
                    )
                    PriorityChoiceChip(
                        priority = Priority.HIGH,
                        isSelected = selectedPriority == Priority.HIGH,
                        onClick = { selectedPriority = Priority.HIGH }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("dialog_cat_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Notification Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Task Reminder",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Push notification alert",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it },
                                modifier = Modifier.testTag("reminder_switch")
                            )
                        }

                        if (hasReminder) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val reminderFormatted = remember(reminderTimeMillis) {
                                val instant = Instant.ofEpochMilli(reminderTimeMillis)
                                val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                                zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a"))
                            }

                            Text(
                                text = "Reminder set for: $reminderFormatted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quick Presets:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PresetPill("In 15m") {
                                    reminderTimeMillis = System.currentTimeMillis() + (15 * 60 * 1000)
                                }
                                PresetPill("In 1 hour") {
                                    reminderTimeMillis = System.currentTimeMillis() + (60 * 60 * 1000)
                                }
                                PresetPill("Tonight 8 PM") {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 20)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    reminderTimeMillis = cal.timeInMillis
                                }
                                PresetPill("Tomorrow 9 AM") {
                                    val cal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                        set(Calendar.HOUR_OF_DAY, 9)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    reminderTimeMillis = cal.timeInMillis
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSaveTask(
                            title,
                            description,
                            selectedDateEpochDay,
                            selectedPriority,
                            selectedCategory,
                            hasReminder,
                            reminderTimeMillis
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text(if (editingTask == null) "Create Task" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("cancel_task_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DateChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PriorityChoiceChip(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (color, label) = when (priority) {
        Priority.HIGH -> Pair(PriorityHigh, "High")
        Priority.MEDIUM -> Pair(PriorityMedium, "Medium")
        Priority.LOW -> Pair(PriorityLow, "Low")
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PresetPill(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
