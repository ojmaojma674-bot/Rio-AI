package com.rioai.app.ai

import android.content.Context

/**
 * MemoryStore
 * Lightweight wrapper around SharedPreferences for long-term contextual memory.
 * This is intentionally simple and synchronous; for heavy usage swap in Room or a file DB.
 */
class MemoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("rio_memory", Context.MODE_PRIVATE)

    fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun get(key: String): String? = prefs.getString(key, null)

    fun remove(key: String) { prefs.edit().remove(key).apply() }
}
