package com.rioai.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rioai.app.R
import com.rioai.app.ai.AIEngine
import com.rioai.app.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.runButton.setOnClickListener {
            val input = binding.inputEdit.text.toString()
            if (input.isBlank()) return@setOnClickListener

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val intent = AIEngine.parseIntent(input)
                    Timber.i("Parsed intent: %s", intent)
                    binding.outputText.text = "Intent: $intent"

                    // Save a tiny memory example
                    AIEngine.saveMemory("latest_note", input)
                } catch (t: Throwable) {
                    Timber.e(t, "MainActivity: processing failed")
                    binding.outputText.text = "Error: ${t.localizedMessage}"
                }
            }
        }
    }
}
