package com.example.data.gemini

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedTasks: List<String> = emptyList()
)

enum class MessageRole {
    USER,
    MODEL,
    SYSTEM
}

enum class GeminiModel(
    val modelId: String,
    val displayName: String,
    val description: String,
    val badge: String
) {
    FLASH(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        description = "General task assistance, smart scheduling & balanced speed",
        badge = "Default"
    ),
    PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        description = "Complex task breakdown, deep reasoning & project architecture",
        badge = "Complex"
    ),
    FLASH_LITE(
        modelId = "gemini-3.1-flash-lite",
        displayName = "Gemini 3.1 Flash Lite",
        description = "Ultra-fast response for rapid ideas, quick tips & brief answers",
        badge = "Fast"
    )
}
