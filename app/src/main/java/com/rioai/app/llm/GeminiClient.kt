package com.rioai.app.llm

import android.util.Log
import com.rioai.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import timber.log.Timber

/**
 * GeminiClient
 * Lightweight HTTP client wrapper to call Google Generative Language / Gemini endpoints.
 *
 * Notes:
 * - This implementation uses the simple REST endpoint where an API key is provided.
 * - For production use, prefer OAuth2 service accounts or Google's official SDK.
 * - GEMINI_API_KEY and GEMINI_BASE_URL are injected via BuildConfig (from local.properties or environment variables).
 */
class GeminiClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val baseUrl: String = BuildConfig.GEMINI_BASE_URL
) {

    private val client by lazy {
        val logger = HttpLoggingInterceptor { message -> Timber.d("OkHttp: %s", message) }
        logger.level = HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    suspend fun generateText(model: String = "models/text-bison-001", prompt: String, temperature: Double = 0.0): String =
        withContext(Dispatchers.IO) {
            try {
                val url = "${baseUrl}/v1/${model}:generateText?key=${apiKey}"
                val json = JSONObject()
                val promptObj = JSONObject()
                promptObj.put("text", prompt)
                val promptList = org.json.JSONArray()
                promptList.put(promptObj)
                json.put("prompt", JSONObject().put("parts", promptList))
                json.put("temperature", temperature)

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Content-Type", "application/json")
                    // Some deployments may require Authorization: Bearer <token>
                    .build()

                client.newCall(request).execute().use { resp ->
                    val respBody = resp.body?.string() ?: ""
                    if (!resp.isSuccessful) {
                        Timber.e("Gemini generateText failed: %s", respBody)
                        throw RuntimeException("Gemini API error: ${resp.code} - ${respBody}")
                    }

                    // Try to parse common response shapes. The Generative Language API returns candidates/outputs.
                    // We try a few known keys to be resilient across versions.
                    val parsed = JSONObject(respBody)
                    // Try: "candidates" -> array of objects with "output"
                    if (parsed.has("candidates")) {
                        val cands = parsed.getJSONArray("candidates")
                        if (cands.length() > 0) {
                            val first = cands.getJSONObject(0)
                            if (first.has("output")) return@withContext first.getString("output")
                        }
                    }

                    // Try: "output" top-level
                    if (parsed.has("output")) {
                        return@withContext parsed.getString("output")
                    }

                    // Try: "outputs" array
                    if (parsed.has("outputs")) {
                        val outputs = parsed.getJSONArray("outputs")
                        if (outputs.length() > 0) {
                            val o = outputs.getJSONObject(0)
                            if (o.has("content")) {
                                val content = o.getJSONArray("content")
                                // join content parts
                                val sb = StringBuilder()
                                for (i in 0 until content.length()) {
                                    sb.append(content.getString(i))
                                }
                                return@withContext sb.toString()
                            }
                        }
                    }

                    // Fallback: return raw body
                    return@withContext respBody
                }
            } catch (t: Throwable) {
                Timber.e(t, "GeminiClient.generateText failed")
                throw t
            }
        }

}
