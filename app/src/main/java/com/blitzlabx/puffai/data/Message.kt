package com.blitzlabx.puffai.data

data class Message(
    val id: String,
    val role: String,
    val content: String,
    val thought: String? = null,
    val thoughtMs: Long? = null,
    val ts: Long,
    val expanded: Boolean = false,
    val isLoading: Boolean = false
)
