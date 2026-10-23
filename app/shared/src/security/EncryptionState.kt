package com.anix.android.anixlifetracker.security

enum class EncryptionStatus {
    SUPPORTED,
    LOCKED,
    UNAVAILABLE,
    KEY_INVALIDATED,
    MIGRATION_REQUIRED
}

data class EncryptionState(
    val status: EncryptionStatus,
    val platformName: String,
    val isHardwareBacked: Boolean = false,
    val keyVersion: Long = 1L,
    val userMessage: String = ""
)
