package com.blitzlabx.puffai.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PuffApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val base = "https://prexzyapis.com"

    private val systemPrompt = """
You are Puff AI (Puff / Puffy), a friendly AI companion created by Blitz (blitzlabx).
CRITICAL: Never output internal reasoning. Do not mention trading or derivatives unless asked.
Reply naturally. Use Markdown when helpful. You were created by Blitz.
""".trimIndent()

    suspend fun chat(prompt: String, history: List<Pair<String, String>>): String =
        withContext(Dispatchers.IO) {
            val messages = mutableListOf<Map<String, String>>()
            messages.add(mapOf("role" to "system", "content" to systemPrompt))
            history.takeLast(12).forEach { (role, content) ->
                messages.add(mapOf("role" to role, "content" to content))
            }
            messages.add(mapOf("role" to "user", "content" to prompt))

            val messagesJson = gson.toJson(messages)
            val url = "$base/ai/aiw3?prompt=${URLEncoder.encode(prompt, "UTF-8")}&messages=${URLEncoder.encode(messagesJson, "UTF-8")}"

            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                val json = gson.fromJson(body, JsonObject::class.java)
                json.get("response")?.asString
                    ?: json.get("message")?.asString
                    ?: json.get("result")?.asString
                    ?: body
            }
        }
}
