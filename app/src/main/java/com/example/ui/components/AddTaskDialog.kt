package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.TaskEntity
import com.example.data.model.Priority
import com.example.ui.theme.CosmicBackgroundDark
import com.example.ui.theme.CosmicSurfaceDark
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.iosGlassmorphic
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

    var reminderTimeMillis by remember {
        mutableLongStateOf(
            editingTask?.reminderTimeMillis ?: (System.currentTimeMillis() + 3600_000)
        )
    }

    val categories = listOf("Work", "Personal", "Health", "Shopping", "Finance", "General")
    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val isDark = isSystemInDarkTheme()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .iosGlassmorphic(
                    cornerRadius = 28.dp,
                    isDark = isDark,
                    hasSpecularSheen = true,
                    borderAlpha = 0.85f
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with glowing icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (editingTask == null) "New Daily Task" else "Edit Task",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task Title Field with Glass Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("e.g. Design sprint presentation") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x351E243A) else Color(0x60F8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x25171B2D) else Color(0x40F1F5F9),
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = if (isDark) Color(0x30FFFFFF) else Color(0x40CBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Details (optional)", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Add any details or bullet points...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x351E243A) else Color(0x60F8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x25171B2D) else Color(0x40F1F5F9),
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = if (isDark) Color(0x30FFFFFF) else Color(0x40CBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scheduled Day Selector
                Text(
                    text = "SCHEDULED DAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = if (isDark) NeonCyan else IndigoPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateChoiceChip(
                        label = "Today",
                        isSelected = selectedDateEpochDay == todayEpoch,
                        isDark = isDark,
                        onClick = { selectedDateEpochDay = todayEpoch }
                    )
                    DateChoiceChip(
                        label = "Tomorrow",
                        isSelected = selectedDateEpochDay == todayEpoch + 1,
                        isDark = isDark,
                        onClick = { selectedDateEpochDay = todayEpoch + 1 }
                    )
                    val otherDate = LocalDate.ofEpochDay(selectedDateEpochDay)
                    val isOther = selectedDateEpochDay != todayEpoch && selectedDateEpochDay != todayEpoch + 1
                    DateChoiceChip(
                        label = if (isOther) otherDate.format(DateTimeFormatter.ofPattern("MMM d")) else "+2 Days",
                        isSelected = isOther,
                        isDark = isDark,
                        onClick = {
                            if (!isOther) selectedDateEpochDay = todayEpoch + 2
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection
                Text(
                    text = "PRIORITY LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = if (isDark) NeonCyan else IndigoPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PriorityChoiceChip(
                        priority = Priority.LOW,
                        isSelected = selectedPriority == Priority.LOW,
                        isDark = isDark,
                        onClick = { selectedPriority = Priority.LOW }
                    )
                    PriorityChoiceChip(
                        priority = Priority.MEDIUM,
                        isSelected = selectedPriority == Priority.MEDIUM,
                        isDark = isDark,
                        onClick = { selectedPriority = Priority.MEDIUM }
                    )
                    PriorityChoiceChip(
                        priority = Priority.HIGH,
                        isSelected = selectedPriority == Priority.HIGH,
                        isDark = isDark,
                        onClick = { selectedPriority = Priority.HIGH }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = if (isDark) NeonCyan else IndigoPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isCatSelected = selectedCategory == cat
                        val shape = RoundedCornerShape(12.dp)

                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(
                                    if (isCatSelected) {
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                        )
                                    } else {
                                        if (isDark) Brush.linearGradient(listOf(Color(0x301E243A), Color(0x181E243A)))
                                        else Brush.linearGradient(listOf(Color(0xE0FFFFFF), Color(0xC0F1F5F9)))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isCatSelected) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent))
                                    else Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.7f), Color.Transparent)),
                                    shape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                    onClick = { selectedCategory = cat }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("dialog_cat_$cat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reminder Notification Glass Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isDark) Color(0x35182035) else Color(0x35E2E8F0)
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.6f), Color.Transparent)),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0x4006B6D4) else Color(0x256366F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = if (isDark) NeonCyan else IndigoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Task Reminder",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Push notification alert",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = IndigoPrimary
                                ),
                                modifier = Modifier.testTag("reminder_switch")
                            )
                        }

                        if (hasReminder) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val reminderFormatted = remember(reminderTimeMillis) {
                                val instant = Instant.ofEpochMilli(reminderTimeMillis)
                                val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                                zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a"))
                            }

                            Text(
                                text = "Reminder set for: $reminderFormatted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDark) NeonCyan else IndigoPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quick Presets:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PresetPill("In 15m", isDark) {
                                    reminderTimeMillis = System.currentTimeMillis() + (15 * 60 * 1000)
                                }
                                PresetPill("In 1 hour", isDark) {
                                    reminderTimeMillis = System.currentTimeMillis() + (60 * 60 * 1000)
                                }
                                PresetPill("Tonight 8 PM", isDark) {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 20)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    reminderTimeMillis = cal.timeInMillis
                                }
                                PresetPill("Tomorrow 9 AM", isDark) {
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

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Create/Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDark) Color(0x301E243A) else Color(0x40E2E8F0)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = if (isDark) 0.15f else 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                onClick = onDismiss
                            )
                            .padding(vertical = 14.dp)
                            .testTag("cancel_task_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    val canSave = title.isNotBlank()
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (canSave) {
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF06B6D4))
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color(0x3094A3B8), Color(0x2094A3B8)))
                                }
                            )
                            .then(
                                if (canSave) {
                                    Modifier
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), Color.Transparent)),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .drawWithContent {
                                            drawContent()
                                            // Top glossy specular highlight
                                            val sheen = size.height * 0.45f
                                            drawRect(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                                    startY = 0f,
                                                    endY = sheen
                                                ),
                                                size = Size(size.width, sheen)
                                            )
                                        }
                                } else Modifier
                            )
                            .clickable(
                                enabled = canSave,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                                onClick = {
                                    if (canSave) {
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
                                }
                            )
                            .padding(vertical = 14.dp)
                            .testTag("save_task_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (editingTask == null) "Create Task" else "Save Changes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (canSave) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChoiceChip(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) {
                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                } else {
                    if (isDark) Brush.linearGradient(listOf(Color(0x301E243A), Color(0x181E243A)))
                    else Brush.linearGradient(listOf(Color(0xE0FFFFFF), Color(0xC0F1F5F9)))
                }
            )
            .border(
                1.dp,
                if (isSelected) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent))
                else Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.7f), Color.Transparent)),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun PriorityChoiceChip(
    priority: Priority,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val (color, label) = when (priority) {
        Priority.HIGH -> Pair(PriorityHigh, "High")
        Priority.MEDIUM -> Pair(PriorityMedium, "Medium")
        Priority.LOW -> Pair(PriorityLow, "Low")
    }

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) color.copy(alpha = if (isDark) 0.35f else 0.25f)
                else (if (isDark) Color(0x251E243A) else Color(0x70F1F5F9))
            )
            .border(
                1.dp,
                if (isSelected) Brush.linearGradient(listOf(color.copy(alpha = 0.8f), Color.Transparent))
                else Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.7f), Color.Transparent)),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = color.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) color else color.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PresetPill(label: String, isDark: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isDark) Color(0x351E243A) else Color(0xD9FFFFFF)
            )
            .border(
                0.8.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.2f else 0.8f), Color.Transparent)),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = IndigoPrimary),
                onClick = onClick
            )
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = if (isDark) NeonCyan else IndigoPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

