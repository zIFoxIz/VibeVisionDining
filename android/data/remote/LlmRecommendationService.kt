package com.example.vibevision.data.remote

import com.example.vibevision.model.Restaurant
import com.example.vibevision.model.UserProfile
import com.example.vibevision.model.VibePreference
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class LlmCandidate(
    val id: String,
    val name: String,
    val city: String,
    val cuisine: String,
    val priceLevel: Int,
    val vibeTags: List<String>
)

interface LlmRecommendationService {
    suspend fun recommendRestaurantIds(
        restaurants: List<Restaurant>,
        profile: UserProfile,
        vibePreferences: List<VibePreference>,
        favoriteIds: Set<String>,
        recentlyViewedIds: List<String>,
        maxResults: Int = 5
    ): List<String>
}

private interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun createCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): OpenAiChatResponse
}

data class OpenAiChatRequest(
    val model: String,
    val temperature: Double,
    val messages: List<OpenAiMessage>
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiChatResponse(
    val choices: List<OpenAiChoice> = emptyList()
)

data class OpenAiChoice(
    val message: OpenAiMessageContent = OpenAiMessageContent("")
)

data class OpenAiMessageContent(
    val content: String
)

class OpenAiLlmRecommendationService private constructor(
    private val api: OpenAiApi,
    private val apiKey: String,
    private val model: String
) : LlmRecommendationService {

    private val gson = Gson()

    override suspend fun recommendRestaurantIds(
        restaurants: List<Restaurant>,
        profile: UserProfile,
        vibePreferences: List<VibePreference>,
        favoriteIds: Set<String>,
        recentlyViewedIds: List<String>,
        maxResults: Int
    ): List<String> {
        if (apiKey.isBlank() || restaurants.isEmpty()) return emptyList()

        val candidates = restaurants.take(40).map {
            LlmCandidate(
                id = it.id,
                name = it.name,
                city = it.city,
                cuisine = it.cuisine,
                priceLevel = it.priceLevel,
                vibeTags = it.vibeTags
            )
        }

        val enabledVibes = vibePreferences.filter { it.enabled }.map { it.vibe }
        val prompt = buildPrompt(
            candidates = candidates,
            profile = profile,
            enabledVibes = enabledVibes,
            favoriteIds = favoriteIds,
            recentlyViewedIds = recentlyViewedIds,
            maxResults = maxResults
        )

        val response = api.createCompletion(
            authorization = "Bearer $apiKey",
            request = OpenAiChatRequest(
                model = model,
                temperature = 0.2,
                messages = listOf(
                    OpenAiMessage(
                        role = "system",
                        content = "You are a restaurant recommendation ranking assistant. Return only valid JSON."
                    ),
                    OpenAiMessage(role = "user", content = prompt)
                )
            )
        )

        val content = response.choices.firstOrNull()?.message?.content.orEmpty()
        val ids = parseIdsFromCompletion(content)
        val candidateIds = candidates.map { it.id }.toSet()

        return ids.filter { candidateIds.contains(it) }.distinct().take(maxResults)
    }

    private fun buildPrompt(
        candidates: List<LlmCandidate>,
        profile: UserProfile,
        enabledVibes: List<String>,
        favoriteIds: Set<String>,
        recentlyViewedIds: List<String>,
        maxResults: Int
    ): String {
        return """
Rank the candidate restaurants for this user.

User profile:
- Name: ${profile.name.ifBlank { "Unknown" }}
- Email: ${profile.email.ifBlank { "Unknown" }}
- Enabled vibes: ${if (enabledVibes.isEmpty()) "None" else enabledVibes.joinToString()}
- Favorite restaurant IDs: ${if (favoriteIds.isEmpty()) "None" else favoriteIds.joinToString()}
- Recently viewed IDs: ${if (recentlyViewedIds.isEmpty()) "None" else recentlyViewedIds.joinToString()}

Candidates (JSON):
${gson.toJson(candidates)}

Return a JSON object only in this exact shape:
{"recommended_ids":["id1","id2","id3"]}

Rules:
- Use only IDs from the provided candidates.
- Return up to $maxResults IDs.
- Prefer variety in cuisine and city while matching vibes.
- No markdown, no explanations.
""".trimIndent()
    }

    private fun parseIdsFromCompletion(content: String): List<String> {
        val trimmed = content.trim()
        val jsonSegment = when {
            trimmed.startsWith("{") -> trimmed
            else -> {
                val start = trimmed.indexOf('{')
                val end = trimmed.lastIndexOf('}')
                if (start >= 0 && end > start) trimmed.substring(start, end + 1) else ""
            }
        }
        if (jsonSegment.isBlank()) return emptyList()

        return runCatching {
            val json = gson.fromJson(jsonSegment, JsonObject::class.java)
            json.getAsJsonArray("recommended_ids")
                ?.mapNotNull { it.asString }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    companion object {
        fun create(apiKey: String, model: String): OpenAiLlmRecommendationService {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return OpenAiLlmRecommendationService(
                api = retrofit.create(OpenAiApi::class.java),
                apiKey = apiKey,
                model = model
            )
        }
    }
}
