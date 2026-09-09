package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.data.model.Priority
import com.example.ui.theme.CloudPendingAmber
import com.example.ui.theme.CloudSyncedGreen
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.iosGlassmorphic
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: TaskEntity,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val checkScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.0f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .iosGlassmorphic(
                cornerRadius = 20.dp,
                isDark = isDark,
                hasSpecularSheen = !task.isCompleted,
                borderAlpha = if (task.isCompleted) 0.25f else 0.7f
            )
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Animated Futuristic iOS Glass Checkbox (Touch target 48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = { onToggleCompletion(task) }
                    )
                    .testTag("checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                val checkmarkBg = if (task.isCompleted) {
                    Brush.linearGradient(
                        colors = listOf(NeonEmerald, Color(0xFF059669), NeonCyan)
                    )
                } else {
                    Brush.linearGradient(
                        colors = if (isDark) listOf(Color(0x301E243A), Color(0x151E243A))
                        else listOf(Color(0xE0FFFFFF), Color(0xC0F1F5F9))
                    )
                }

                val checkBorder = if (task.isCompleted) {
                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), NeonCyan.copy(alpha = 0.6f)))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.8f),
                            Color(0xFF6366F1).copy(alpha = 0.25f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(checkmarkBg)
                        .border(1.5.dp, checkBorder, CircleShape)
                        .drawWithContent {
                            drawContent()
                            if (task.isCompleted) {
                                // Glossy specular curve on checkbox
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                                        startY = 0f,
                                        endY = size.height * 0.5f
                                    ),
                                    size = Size(size.width, size.height * 0.5f)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Task Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
            ) {
                // Title with modern typography
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                        fontSize = 15.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (task.isCompleted) 0.4f else 0.75f),
                            lineHeight = 16.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Badges Row: Category, Priority, Reminder, Cloud Sync Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    CategoryBadge(category = task.category, isDark = isDark)

                    // Priority Chip
                    PriorityChip(priorityName = task.priority, isDark = isDark)

                    // Reminder Badge if set
                    if (task.hasReminder && task.reminderTimeMillis != null) {
                        ReminderBadge(timeMillis = task.reminderTimeMillis, isDark = isDark)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Cloud Sync Indicator
                    CloudSyncBadge(isSynced = task.isSynced)
                }
            }

            // More Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("task_menu_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Task options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Task", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onEditTask(task)
                        },
                        modifier = Modifier.testTag("menu_edit_${task.id}")
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Task", color = PriorityHigh, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = PriorityHigh, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showMenu = false
                            onDeleteTask(task.id, task.title)
                        },
                        modifier = Modifier.testTag("menu_delete_${task.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: String, isDark: Boolean) {
    val icon: ImageVector = when (category) {
        "Work" -> Icons.Default.Work
        "Personal" -> Icons.Default.Person
        "Health" -> Icons.Default.FitnessCenter
        "Shopping" -> Icons.Default.ShoppingCart
        "Finance" -> Icons.Default.AttachMoney
        else -> Icons.Default.Checklist
    }

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isDark) Color(0x301E243A) else Color(0x70F1F5F9)
            )
            .border(
                0.8.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.7f), Color.Transparent)),
                shape
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) NeonCyan else IndigoPrimary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PriorityChip(priorityName: String, isDark: Boolean) {
    val (baseColor, label) = when (priorityName) {
        Priority.HIGH.name -> Pair(PriorityHigh, "High")
        Priority.MEDIUM.name -> Pair(PriorityMedium, "Med")
        else -> Pair(PriorityLow, "Low")
    }

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(baseColor.copy(alpha = if (isDark) 0.18f else 0.12f))
            .border(
                0.8.dp,
                Brush.linearGradient(listOf(baseColor.copy(alpha = 0.5f), Color.Transparent)),
                shape
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(baseColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = baseColor
                )
            )
        }
    }
}

@Composable
private fun ReminderBadge(timeMillis: Long, isDark: Boolean) {
    val formattedTime = remember(timeMillis) {
        val instant = Instant.ofEpochMilli(timeMillis)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        zonedDateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isDark) Color(0x3538BDF8) else Color(0x2038BDF8))
            .border(
                0.8.dp,
                Brush.linearGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.5f), Color.Transparent)),
                shape
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "Reminder",
                tint = if (isDark) NeonCyan else IndigoPrimary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = if (isDark) NeonCyan else IndigoPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun CloudSyncBadge(isSynced: Boolean) {
    val icon = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload
    val tint = if (isSynced) CloudSyncedGreen else CloudPendingAmber
    val label = if (isSynced) "Synced" else "Pending"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
    }
}

