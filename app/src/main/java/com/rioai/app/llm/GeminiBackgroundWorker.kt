package com.rioai.app.llm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rioai.app.ai.AIEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * GeminiBackgroundWorker
 * Example WorkManager worker that sends a short context to Gemini and stores the reply as memory.
 */
class GeminiBackgroundWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        try {
            val client = GeminiClient()
            val latest = AIEngine.readMemory("latest_note") ?: "Hello, Rio here."
            Timber.i("GeminiBackgroundWorker: prompting Gemini with latest: %s", latest)
            val response = client.generateText(prompt = "User context: ${latest}\nProvide a concise summary:")
            Timber.i("GeminiBackgroundWorker: received reply length=%d", response.length)

            AIEngine.saveMemory("latest_gemini_reply", response)
            Result.success()
        } catch (t: Throwable) {
            Timber.e(t, "GeminiBackgroundWorker: failed")
            Result.retry()
        }
    }
}
