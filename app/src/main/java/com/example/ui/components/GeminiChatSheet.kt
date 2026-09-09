package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiModel
import com.example.data.gemini.MessageRole
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SkySecondary
import com.example.ui.theme.iosGlassmorphic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatSheet(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    selectedModel: GeminiModel,
    onSelectModel: (GeminiModel) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onAddSuggestedTask: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showModelMenu by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when messages update
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickStarters = listOf(
        "Break down a big project into steps",
        "Prioritize my 3 most important tasks today",
        "Suggest a balanced daily schedule",
        "Give me a 5-minute productivity tip"
    )

    val isDark = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark) Color(0xF20F1426) else Color(0xF8FFFFFF),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header with Model Selector and Clear Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
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
                                    listOf(NeonViolet, NeonCyan)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Gemini AI Planner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Futuristic task planning & coaching",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Model Selector Pill
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isDark) Color(0x306366F1) else Color(0x186366F1)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) NeonViolet.copy(alpha = 0.5f) else Color(0x406366F1),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { showModelMenu = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("model_selector_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (selectedModel) {
                                        GeminiModel.PRO -> Icons.Default.Psychology
                                        GeminiModel.FLASH_LITE -> Icons.Default.Speed
                                        else -> Icons.Default.Bolt
                                    },
                                    contentDescription = null,
                                    tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedModel.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) NeonCyan else Color(0xFF6366F1)
                                    )
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            GeminiModel.entries.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(model.displayName, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                model.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectModel(model)
                                        showModelMenu = false
                                    },
                                    modifier = Modifier.testTag("model_item_${model.name}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Scrollable Chat Thread
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chat_messages_list")
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { msg ->
                    ChatBubble(
                        message = msg,
                        isDark = isDark,
                        onAddSuggestedTask = onAddSuggestedTask
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini is synthesizing suggestions...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Quick Starter Suggestions Row with glossy pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickStarters) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDark) Color(0x351F293D) else Color(0x186366F1)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x356366F1) else Color(0x256366F1),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSendMessage(prompt) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("quick_starter_${prompt.take(10)}")
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) NeonCyan else Color(0xFF6366F1)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chat Input Row with iOS glossy glass styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Ask Gemini to plan, organize or prioritize...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x301E243A) else Color(0x30F8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0x201E243A) else Color(0x20F8FAFC),
                        focusedBorderColor = NeonCyan.copy(alpha = 0.7f),
                        unfocusedBorderColor = if (isDark) Color(0x25FFFFFF) else Color(0x30CBD5E1)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank() && !isLoading) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isLoading)
                                Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4)))
                            else
                                Brush.linearGradient(
                                    listOf(
                                        if (isDark) Color(0x30FFFFFF) else Color(0x20000000),
                                        if (isDark) Color(0x15FFFFFF) else Color(0x10000000)
                                    )
                                )
                        )
                        .border(
                            1.dp,
                            if (inputText.isNotBlank() && !isLoading)
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Transparent))
                            else
                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                            CircleShape
                        )
                        .clickable(enabled = inputText.isNotBlank() && !isLoading) {
                            if (inputText.isNotBlank() && !isLoading) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                        .testTag("send_message_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isDark: Boolean,
    onAddSuggestedTask: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                        ),
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        val sheenHeight = size.height * 0.35f
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = sheenHeight
                            ),
                            size = Size(size.width, sheenHeight)
                        )
                    }
                    .padding(14.dp)
                    .testTag("chat_bubble_${message.role.name}")
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .iosGlassmorphic(
                        cornerRadius = 18.dp,
                        isDark = isDark,
                        hasSpecularSheen = true,
                        borderAlpha = 0.7f
                    )
                    .padding(14.dp)
                    .testTag("chat_bubble_${message.role.name}")
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        // Suggested Tasks 1-tap add pills if present in Model response
        if (!isUser && message.suggestedTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Suggested Tasks (tap to add):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) NeonCyan else Color(0xFF6366F1)
                    )
                )
                message.suggestedTasks.forEach { taskTitle ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0x351F293D) else Color(0x186366F1)
                            )
                            .border(
                                1.dp,
                                if (isDark) NeonCyan.copy(alpha = 0.5f) else Color(0x406366F1),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onAddSuggestedTask(taskTitle) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("add_suggested_task_$taskTitle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDark) NeonCyan else Color(0xFF6366F1),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = taskTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) NeonCyan else Color(0xFF6366F1)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
