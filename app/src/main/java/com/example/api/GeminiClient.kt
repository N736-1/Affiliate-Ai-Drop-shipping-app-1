package com.example.api

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

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateAdCreative(platform: String, product: String, details: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            return@withContext "" // Return empty to trigger high-quality fallback generator
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val promptText = """
                You are an award-winning, conversion-focused digital marketer.
                Create a high-density, highly engaging social media ad copywriting for the platform: $platform.
                Product Name: $product.
                Campaign Context/Details: $details.
                Provide structured sections:
                1. ⚡ EYE-CATCHING HOOK
                2. 📈 KEY BENEFITS (bullet points with emojis)
                3. 🔥 CALL TO ACTION (CTA)
                Keep it concise, modern, and aligned with a High Density digital e-commerce aesthetic.
            """.trimIndent()

            val partJson = JSONObject().put("text", promptText)
            val partsArray = JSONArray().put(partJson)
            val contentJson = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentJson)
            val requestBodyJson = JSONObject().put("contents", contentsArray)

            val body = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeminiClient", "Gemini call failed with code: ${response.code}")
                    return@withContext ""
                }
                val bodyString = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error calling Gemini REST API", e)
        }
        return@withContext ""
    }
}
