package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncStatus
import com.example.data.model.SyncLogEntry
import com.example.data.model.SyncState
import com.example.ui.theme.CloudErrorRed
import com.example.ui.theme.CloudPendingAmber
import com.example.ui.theme.CloudSyncedGreen
import com.example.ui.theme.CloudSyncingBlue
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.iosGlassmorphic
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncSheet(
    syncStatus: CloudSyncStatus,
    unsyncedCount: Int,
    syncLogs: List<SyncLogEntry>,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onSimulateRemoteDevice: () -> Unit,
    onClearCloudData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark) Color(0xF20F1426) else Color(0xF8FFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonCyan, Color(0xFF6366F1))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cloud Synchronization",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Real-time encrypted Firebase sync",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Status Banner Card with Glassmorphic styling
            val (statusColor, statusTitle, statusSubtitle) = when (syncStatus.state) {
                SyncState.SYNCING -> Triple(
                    CloudSyncingBlue,
                    "Synchronizing Tasks...",
                    "Pushing local modifications & fetching remote updates"
                )
                SyncState.SUCCESS -> Triple(
                    CloudSyncedGreen,
                    "Cloud Sync Complete",
                    "All tasks and reminder states are fully up-to-date"
                )
                SyncState.ERROR -> Triple(
                    CloudErrorRed,
                    "Synchronization Issue",
                    syncStatus.errorMessage ?: "Failed to reach cloud endpoint"
                )
                SyncState.IDLE -> {
                    if (unsyncedCount > 0) {
                        Triple(
                            CloudPendingAmber,
                            "$unsyncedCount Changes Pending Upload",
                            "Local edits are queued to push to your cloud backup"
                        )
                    } else {
                        Triple(
                            CloudSyncedGreen,
                            "Fully Synchronized",
                            "Your daily task list is backed up and synchronized"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .iosGlassmorphic(
                        cornerRadius = 20.dp,
                        isDark = isDark,
                        hasSpecularSheen = true,
                        borderAlpha = 0.8f
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (syncStatus.state == SyncState.SYNCING) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Syncing",
                                tint = statusColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(rotation)
                            )
                        } else {
                            Icon(
                                imageVector = if (unsyncedCount > 0) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata info row: Last Sync + Device + Cloud Total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .iosGlassmorphic(
                        cornerRadius = 16.dp,
                        isDark = isDark,
                        hasSpecularSheen = false,
                        borderAlpha = 0.5f
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val lastSyncFormatted = remember(syncStatus.lastSyncTime) {
                        syncStatus.lastSyncTime?.let {
                            val zdt = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            zdt.format(DateTimeFormatter.ofPattern("h:mm:ss a"))
                        } ?: "Pending"
                    }

                    InfoItem(label = "Last Sync", value = lastSyncFormatted)
                    InfoItem(label = "Device", value = syncStatus.deviceId)
                    InfoItem(label = "Cloud Items", value = "${syncStatus.totalSyncedCount}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Sync Now" Button with Liquid Neon Sheen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (syncStatus.state != SyncState.SYNCING)
                            Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF06B6D4)))
                        else
                            Brush.linearGradient(listOf(Color(0x506366F1), Color(0x304F46E5)))
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)),
                        RoundedCornerShape(16.dp)
                    )
                    .drawWithContent {
                        drawContent()
                        val sheenHeight = size.height * 0.4f
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = sheenHeight
                            ),
                            size = Size(size.width, sheenHeight)
                        )
                    }
                    .clickable(
                        enabled = syncStatus.state != SyncState.SYNCING,
                        onClick = onSyncNow
                    )
                    .testTag("sync_now_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = if (syncStatus.state == SyncState.SYNCING) Modifier
                            .size(18.dp)
                            .rotate(rotation) else Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (syncStatus.state == SyncState.SYNCING) "Synchronizing..." else "Sync Now",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Sync Toggle Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .iosGlassmorphic(
                        cornerRadius = 16.dp,
                        isDark = isDark,
                        hasSpecularSheen = false,
                        borderAlpha = 0.5f
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automatic Cloud Sync",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Sync instantly whenever tasks change",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Switch(
                        checked = syncStatus.isAutoSyncEnabled,
                        onCheckedChange = onToggleAutoSync,
                        modifier = Modifier.testTag("auto_sync_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-Device Simulation Card (Interactive Demo of 2-way cloud sync)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .iosGlassmorphic(
                        cornerRadius = 16.dp,
                        isDark = isDark,
                        hasSpecularSheen = false,
                        borderAlpha = 0.5f
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Device Sync Test",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Simulate another device pushing a task to your cloud database, then automatically sync it to this phone.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0x351F293D) else Color(0x186366F1)
                            )
                            .border(
                                1.dp,
                                if (isDark) NeonCyan.copy(alpha = 0.5f) else Color(0x406366F1),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onSimulateRemoteDevice)
                            .padding(vertical = 8.dp)
                            .testTag("simulate_remote_sync_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Simulate Secondary Device Update",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) NeonCyan else Color(0xFF6366F1)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Activity Log
            Text(
                text = "Sync Activity History",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (syncLogs.isEmpty()) {
                Text(
                    text = "No sync activity recorded yet",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    syncLogs.take(3).forEach { log ->
                        val timeStr = remember(log.timestamp) {
                            val zdt = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault())
                            zdt.format(DateTimeFormatter.ofPattern("h:mm a"))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (log.isSuccess) CloudSyncedGreen else CloudErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                }
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
