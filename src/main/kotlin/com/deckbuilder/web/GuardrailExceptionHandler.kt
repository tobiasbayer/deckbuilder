package com.deckbuilder.web

import dev.langchain4j.guardrail.InputGuardrailException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GuardrailExceptionHandler {

    @ExceptionHandler(InputGuardrailException::class)
    fun handleInputGuardrail(ex: InputGuardrailException): ChatResponse {
        // The framework wraps the guard's message as "The guardrail X failed with this message: <actual message>"
        val reply = ex.message
            ?.substringAfter("this message: ", missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
            ?: "Unknown error"
        return ChatResponse(reply = reply, sessionId = "")
    }
}
