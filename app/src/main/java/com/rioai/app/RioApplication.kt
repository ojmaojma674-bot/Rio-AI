package com.rioai.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.rioai.app.ai.AIEngine
import com.rioai.app.background.WorkManagerInitializer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class RioApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production use a crash reporting tree
            Timber.plant(ReleaseTree())
        }

        // Global coroutine exception handler
        val handler = CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Uncaught coroutine error")
            // TODO: integrate with crash reporting
        }

        // Initialize AI core
        try {
            AIEngine.initialize(applicationContext)
            Timber.i("AIEngine initialized")
        } catch (t: Throwable) {
            Timber.e(t, "Failed to initialize AIEngine")
        }

        // Run a lightweight startup task to warm up components
        runBlocking(Dispatchers.IO + handler) {
            AIEngine.warmup()
            Timber.i("AIEngine warmup complete")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

}

// Simple release tree to avoid logging sensitive info in production
class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= android.util.Log.INFO
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // TODO: send to crash reporting backend or analytics
    }
}
