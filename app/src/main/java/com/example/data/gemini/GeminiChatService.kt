package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiChatService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val systemInstructionText = """
        You are an intelligent, thoughtful daily task planning assistant and productivity coach for the Daily Tasks app.
        Your role is to help users organize their daily priorities, break down big goals into manageable checklist steps,
        estimate realistic time limits, suggest morning/evening routines, and maintain clear focus.
        When providing actionable steps or tasks that the user could perform, prefix each task item on its own line with
        '- [ ] Task title' so the app can detect it and allow the user to 1-tap add it to their daily task list.
        Keep your advice clear, encouraging, structured, and practical.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        selectedModel: GeminiModel
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide a graceful simulated AI response if API key is not configured yet in Secrets
            val simulatedResponse = getFallbackResponse(userMessage, selectedModel)
            return@withContext Result.success(
                ChatMessage(
                    role = MessageRole.MODEL,
                    content = simulatedResponse,
                    suggestedTasks = extractTasks(simulatedResponse)
                )
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${selectedModel.modelId}:generateContent?key=$apiKey"

            // Construct contents array for multi-turn history
            val contentsArray = JSONArray()

            // Add previous conversation turns (limit to last 10 to keep within token budgets)
            val turnsToInclude = history.takeLast(10)
            for (msg in turnsToInclude) {
                if (msg.role == MessageRole.SYSTEM) continue
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == MessageRole.USER) "user" else "model")
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.content)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            // Append current user message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            val currentPart = JSONObject()
            currentPart.put("text", userMessage)
            currentParts.put(currentPart)
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            // System instruction
            val systemInstructionObj = JSONObject()
            val sysParts = JSONArray()
            val sysPart = JSONObject()
            sysPart.put("text", systemInstructionText)
            sysParts.put(sysPart)
            systemInstructionObj.put("parts", sysParts)

            // Generation config
            val generationConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionObj)
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiChatService", "API error: ${response.code} $responseBody")
                val errorMsg = try {
                    val errorJson = JSONObject(responseBody).optJSONObject("error")
                    errorJson?.optString("message") ?: "API Error code ${response.code}"
                } catch (_: Exception) {
                    "API Error code ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No response generated by Gemini."))
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("text")) {
                        textBuilder.append(part.getString("text"))
                    }
                }
            }

            val responseText = textBuilder.toString().ifBlank { "I've processed your request." }
            val extractedTasks = extractTasks(responseText)

            Result.success(
                ChatMessage(
                    role = MessageRole.MODEL,
                    content = responseText,
                    suggestedTasks = extractedTasks
                )
            )
        } catch (e: Exception) {
            Log.e("GeminiChatService", "Exception calling Gemini", e)
            Result.failure(e)
        }
    }

    private fun extractTasks(text: String): List<String> {
        val tasks = mutableListOf<String>()
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("- [ ]")) {
                val task = trimmed.removePrefix("- [ ]").trim()
                if (task.isNotBlank()) tasks.add(task)
            } else if (trimmed.startsWith("* [ ]")) {
                val task = trimmed.removePrefix("* [ ]").trim()
                if (task.isNotBlank()) tasks.add(task)
            }
        }
        return tasks
    }

    private fun getFallbackResponse(prompt: String, model: GeminiModel): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("break down") || lower.contains("project") || lower.contains("steps") -> {
                """
                Here is a structured breakdown using ${model.displayName} to help you tackle this efficiently:

                - [ ] Define project scope and main milestone
                - [ ] Gather required references and materials
                - [ ] Complete first draft of the primary task
                - [ ] Review deliverables and refine details
                - [ ] Send update or finalize completion

                You can tap any of the suggested tasks below to add them directly to your schedule!
                """.trimIndent()
            }
            lower.contains("morning") || lower.contains("routine") || lower.contains("schedule") -> {
                """
                A focused daily routine built for maximum productivity:

                - [ ] Review top 3 priority tasks for today
                - [ ] 45-minute deep focus block on hardest task
                - [ ] 10-minute movement and hydration break
                - [ ] Clear pending messages and quick errands
                - [ ] Evening review and plan tomorrow's list

                Tap any task below to schedule it into your day!
                """.trimIndent()
            }
            else -> {
                """
                I'm ready to help you plan and accomplish your goals with ${model.displayName}!

                Here are a few quick recommendations for today:
                - [ ] Identify your single most impactful task
                - [ ] Set a 25-minute timer for uninterrupted focus
                - [ ] Mark completed tasks to maintain momentum

                Feel free to ask me to break down any goal, prioritize your agenda, or suggest focus strategies.
                """.trimIndent()
            }
        }
    }
}
