package com.anix.android.anixlifetracker.security

object RedactingLogger {

    private val accountPattern = Regex("\\b\\d{10,16}\\b")
    private val amountPattern = Regex("(?i)(rs\\.?|INR|USD|EUR|GBP|\\$|€|£)\\s*\\d+([.,]\\d+)?")
    private val tokenPattern = Regex("(?i)(jwt_\\w+|bearer\\s+[a-zA-Z0-9._-]+|password=([^&\\s]+)|key=([^&\\s]+))")

    fun redact(message: String): String {
        if (message.isBlank()) return message
        return message
            .replace(accountPattern, "[REDACTED_ACCOUNT]")
            .replace(amountPattern, "[REDACTED_AMOUNT]")
            .replace(tokenPattern, "[REDACTED_SENSITIVE]")
    }

    fun d(tag: String, message: String) {
        println("DEBUG [$tag]: ${redact(message)}")
    }

    fun i(tag: String, message: String) {
        println("INFO [$tag]: ${redact(message)}")
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        println("WARN [$tag]: ${redact(message)}${throwable?.let { " | Throwable: ${it.message}" } ?: ""}")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        println("ERROR [$tag]: ${redact(message)}${throwable?.let { " | Throwable: ${it.message}" } ?: ""}")
    }
}
