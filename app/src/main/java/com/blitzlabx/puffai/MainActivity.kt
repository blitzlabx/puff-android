package com.blitzlabx.puffai

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blitzlabx.puffai.data.Message
import com.blitzlabx.puffai.databinding.ActivityMainBinding
import com.blitzlabx.puffai.network.PuffApi
import com.blitzlabx.puffai.ui.MessageAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private var userCount = 0
    private val maxMessages = 50
    private val api = PuffApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupLanding()
        setupChat()
        animateLandingPuff()
    }

    private fun setupRecycler() {
        adapter = MessageAdapter(messages) { id ->
            // Toggle thought expansion
            val idx = messages.indexOfFirst { it.id == id }
            if (idx >= 0) {
                messages[idx] = messages[idx].copy(expanded = !messages[idx].expanded)
                adapter.notifyItemChanged(idx)
            }
        }
        binding.recycler.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recycler.adapter = adapter
    }

    private fun setupLanding() {
        binding.btnStart.setOnClickListener {
            binding.landing.animate()
                .alpha(0f)
                .setDuration(220)
                .withEndAction {
                    binding.landing.isVisible = false
                    binding.chatContainer.alpha = 0f
                    binding.chatContainer.isVisible = true
                    binding.chatContainer.animate().alpha(1f).setDuration(280).start()
                }.start()
        }
    }

    private fun setupChat() {
        binding.btnSend.setOnClickListener { send() }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send(); true
            } else false
        }
        binding.btnNewChat.setOnClickListener {
            messages.clear()
            userCount = 0
            adapter.notifyDataSetChanged()
            updateCounter()
            Toast.makeText(this, "New chat started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateLandingPuff() {
        val view = binding.landingPuff
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -18f, 0f).apply {
            duration = 2400
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        view.scaleX = 0.6f
        view.scaleY = 0.6f
        view.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()
    }

    private fun updateCounter() {
        binding.counter.text = "$userCount / $maxMessages"
    }

    private fun send() {
        val text = binding.input.text?.toString()?.trim().orEmpty()
        if (text.isEmpty() || userCount >= maxMessages) return

        binding.input.setText("")
        userCount++
        updateCounter()

        val userMsg = Message(
            id = System.currentTimeMillis().toString(),
            role = "user",
            content = text,
            ts = System.currentTimeMillis()
        )
        messages.add(userMsg)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recycler.scrollToPosition(messages.lastIndex)

        // Snapshot history before the new message for API
        val history = messages.dropLast(1).map { it.role to it.content }

        val loadingId = "loading-${System.currentTimeMillis()}"
        messages.add(Message(id = loadingId, role = "assistant", content = "…", isLoading = true, ts = System.currentTimeMillis()))
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recycler.scrollToPosition(messages.lastIndex)

        val start = System.currentTimeMillis()

        lifecycleScope.launch {
            try {
                val result = api.chat(text, history)
                val elapsed = System.currentTimeMillis() - start
                val (thought, answer) = parseResponse(result)

                // Remove loading
                val loadIdx = messages.indexOfFirst { it.id == loadingId }
                if (loadIdx >= 0) {
                    messages.removeAt(loadIdx)
                    adapter.notifyItemRemoved(loadIdx)
                }

                messages.add(
                    Message(
                        id = System.currentTimeMillis().toString(),
                        role = "assistant",
                        content = answer.ifBlank { result },
                        thought = thought.takeIf { it.isNotBlank() },
                        thoughtMs = elapsed,
                        ts = System.currentTimeMillis()
                    )
                )
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recycler.smoothScrollToPosition(messages.lastIndex)
            } catch (e: Exception) {
                val loadIdx = messages.indexOfFirst { it.id == loadingId }
                if (loadIdx >= 0) {
                    messages.removeAt(loadIdx)
                    adapter.notifyItemRemoved(loadIdx)
                }
                messages.add(
                    Message(
                        id = System.currentTimeMillis().toString(),
                        role = "assistant",
                        content = "Sorry, something went wrong. Please try again.",
                        ts = System.currentTimeMillis()
                    )
                )
                adapter.notifyItemInserted(messages.lastIndex)
            }
        }
    }

    private fun parseResponse(raw: String): Pair<String, String> {
        if (raw.isBlank()) return "" to ""
        val seps = listOf("\n---\n", "\n---\n", "\n**Answer:**\n")
        for (sep in seps) {
            val parts = raw.split(sep)
            if (parts.size >= 2) {
                val thought = parts[0].trim()
                val answer = parts.drop(1).joinToString("\n").trim()
                if (thought.length > 60) return thought to answer.ifBlank { thought }
            }
        }
        return "" to raw.trim()
    }
}
