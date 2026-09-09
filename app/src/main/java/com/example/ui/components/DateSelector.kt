package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SkySecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateSelector(
    selectedDateEpochDay: Long,
    onSelectDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val todayEpoch = remember { today.toEpochDay() }
    val isDark = isSystemInDarkTheme()

    // Display range from 3 days before today to 14 days after today
    val dateList = remember {
        (-3..14).map { offset ->
            today.plusDays(offset.toLong())
        }
    }

    val listState = rememberLazyListState()

    // Scroll to selected date when needed
    LaunchedEffect(selectedDateEpochDay) {
        val index = dateList.indexOfFirst { it.toEpochDay() == selectedDateEpochDay }
        if (index >= 0) {
            val targetScroll = (index - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedLocalDate = LocalDate.ofEpochDay(selectedDateEpochDay)
            val headerText = when {
                selectedDateEpochDay == todayEpoch -> "Today, ${selectedLocalDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                selectedDateEpochDay == todayEpoch + 1 -> "Tomorrow, ${selectedLocalDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                selectedDateEpochDay == todayEpoch - 1 -> "Yesterday, ${selectedLocalDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                else -> selectedLocalDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            }

            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (selectedDateEpochDay != todayEpoch) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) listOf(Color(0x356366F1), Color(0x2038BDF8))
                                else listOf(Color(0xEEF1F5F9), Color(0xD9E2E8F0))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = if (isDark) 0.35f else 0.8f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = IndigoPrimary),
                            onClick = { onSelectDate(todayEpoch) }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("jump_to_today_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Jump to Today",
                            tint = if (isDark) NeonCyan else IndigoPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDark) NeonCyan else IndigoPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }
            }
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dateList) { date ->
                val epochDay = date.toEpochDay()
                val isSelected = epochDay == selectedDateEpochDay
                val isCurrentToday = epochDay == todayEpoch

                DatePill(
                    date = date,
                    isSelected = isSelected,
                    isToday = isCurrentToday,
                    isDark = isDark,
                    onClick = { onSelectDate(epochDay) }
                )
            }
        }
    }
}

@Composable
fun DatePill(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val dayOfWeek = remember(date) { date.format(DateTimeFormatter.ofPattern("EEE")) }
    val dayOfMonth = remember(date) { date.dayOfMonth.toString() }

    val shape = RoundedCornerShape(20.dp)

    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFF4F46E5),
                Color(0xFF4338CA)
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x351E243A),
                    Color(0x22171B2D)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xF2FFFFFF),
                    Color(0xD9F8FAFC)
                )
            )
        }
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                NeonCyan.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0.2f)
            )
        )
    } else {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    Color(0xFFCBD5E1).copy(alpha = 0.4f)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(72.dp)
            .clip(shape)
            .background(backgroundBrush)
            .border(if (isSelected) 1.5.dp else 1.dp, borderBrush, shape)
            .drawWithContent {
                drawContent()
                // Top glossy specular curved reflection
                val sheenHeight = size.height * 0.4f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isSelected) 0.32f else (if (isDark) 0.10f else 0.22f)),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = sheenHeight
                    ),
                    size = Size(size.width, sheenHeight)
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag("date_pill_${date.dayOfMonth}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayOfWeek.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.95f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = dayOfMonth,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
            if (isToday) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonCyan else IndigoPrimary)
                )
            }
        }
    }
}

