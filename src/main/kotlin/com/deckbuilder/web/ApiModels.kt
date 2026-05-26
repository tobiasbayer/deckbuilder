package com.deckbuilder.web

data class ChatRequest(
    val message: String,
    val sessionId: String,
    val maxBudgetUsd: Int? = null,
)

data class ChatResponse(
    val reply: String,
    val sessionId: String
)