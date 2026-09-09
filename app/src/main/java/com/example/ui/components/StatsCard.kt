package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.iosGlassmorphic
import com.example.ui.viewmodel.DailyTaskStats
import com.example.ui.viewmodel.TaskFilter

@Composable
fun StatsCard(
    stats: DailyTaskStats,
    selectedFilter: TaskFilter,
    onSelectFilter: (TaskFilter) -> Unit,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val animatedProgress by animateFloatAsState(
        targetValue = stats.completionPercentage,
        label = "progress_anim"
    )

    val categories = listOf("All", "Work", "Personal", "Health", "Shopping", "Finance")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .iosGlassmorphic(
                cornerRadius = 24.dp,
                isDark = isDark,
                hasSpecularSheen = true,
                borderAlpha = 0.8f
            )
            .testTag("stats_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with percentage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DAILY OVERVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = if (isDark) NeonCyan else IndigoPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (stats.completed == stats.total && stats.total > 0) NeonEmerald else NeonCyan)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (stats.total == 0) "No tasks scheduled for today"
                        else "${stats.completed} of ${stats.total} completed • ${stats.pending} remaining",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Glossy Percentage Pill
                val isAllDone = stats.completionPercentage == 1f && stats.total > 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isAllDone) listOf(Color(0xFF10B981), Color(0xFF059669))
                                else listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF06B6D4))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .drawWithContent {
                            drawContent()
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
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(stats.completionPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Futuristic Liquid Neon Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isDark) Color(0x351F293D) else Color(0x35CBD5E1)
                    )
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = if (isDark) 0.1f else 0.4f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6),
                                        Color(0xFF06B6D4),
                                        Color(0xFF10B981)
                                    )
                                )
                            )
                            .drawWithContent {
                                drawContent()
                                // Glossy specular highlight
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                                        startY = 0f,
                                        endY = size.height * 0.5f
                                    ),
                                    size = Size(size.width, size.height * 0.5f)
                                )
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Segmented Tabs (All, Pending, Completed) in iOS Glass Segment style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0x350F1426) else Color(0x35E2E8F0))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = if (isDark) 0.12f else 0.5f), Color.Transparent)
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterTabItem(
                    label = "All (${stats.total})",
                    isSelected = selectedFilter == TaskFilter.ALL,
                    isDark = isDark,
                    onClick = { onSelectFilter(TaskFilter.ALL) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_all"
                )
                FilterTabItem(
                    label = "Pending (${stats.pending})",
                    isSelected = selectedFilter == TaskFilter.PENDING,
                    isDark = isDark,
                    onClick = { onSelectFilter(TaskFilter.PENDING) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_pending"
                )
                FilterTabItem(
                    label = "Done (${stats.completed})",
                    isSelected = selectedFilter == TaskFilter.COMPLETED,
                    isDark = isDark,
                    onClick = { onSelectFilter(TaskFilter.COMPLETED) },
                    modifier = Modifier.weight(1f),
                    testTag = "filter_completed"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isCatSelected = if (cat == "All") selectedCategory == null else selectedCategory == cat
                    val chipShape = RoundedCornerShape(12.dp)

                    Box(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(
                                if (isCatSelected) {
                                    Brush.linearGradient(
                                        colors = if (isDark) listOf(Color(0x506366F1), Color(0x3506B6D4))
                                        else listOf(Color(0x356366F1), Color(0x2038BDF8))
                                    )
                                } else {
                                    if (isDark) Brush.linearGradient(listOf(Color(0x251E243A), Color(0x151E243A)))
                                    else Brush.linearGradient(listOf(Color(0x80FFFFFF), Color(0x60F1F5F9)))
                                }
                            )
                            .border(
                                1.dp,
                                if (isCatSelected) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.4f)))
                                else Brush.linearGradient(listOf(Color.White.copy(alpha = if (isDark) 0.15f else 0.7f), Color.Transparent)),
                                chipShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = IndigoPrimary),
                                onClick = {
                                    if (cat == "All") onSelectCategory(null)
                                    else onSelectCategory(if (selectedCategory == cat) null else cat)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("cat_chip_$cat"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCatSelected) (if (isDark) NeonCyan else IndigoPrimary)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val shape = RoundedCornerShape(11.dp)

    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5)
            )
        )
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .then(
                if (isSelected) {
                    Modifier
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)),
                            shape
                        )
                        .drawWithContent {
                            drawContent()
                            val sheen = size.height * 0.4f
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    startY = 0f,
                                    endY = sheen
                                ),
                                size = Size(size.width, sheen)
                            )
                        }
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}

