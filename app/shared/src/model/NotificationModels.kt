package com.anix.android.anixlifetracker.model

/**
 * Platform-neutral representation of an Android notification callback
 */
data class FinancialNotification(
    val sourcePackage: String,
    val title: String?,
    val content: String?,
    val postedAt: Long,
    val notificationIdentifier: String? = null
)

/**
 * Supported financial app profile configuration
 */
data class FinancialAppProfile(
    val id: String,
    val packageName: String,
    val displayName: String,
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = false,
    val parserProfileId: String = "default_upi_bank",
    val categoryHint: AccountCategoryType? = null,
    val lastProcessedAt: String? = null
)

/**
 * Listener service connection and permission state
 */
enum class NotificationListenerState {
    NOT_CONFIGURED,
    ACCESS_REQUIRED,
    CONNECTING,
    ACTIVE,
    DISCONNECTED,
    DISABLED,
    ERROR
}

/**
 * User preferences for notification tracking
 */
data class NotificationSettingsModel(
    val isNotificationFeatureEnabled: Boolean = false,
    val isConsentGiven: Boolean = false,
    val liveMonitoringEnabled: Boolean = false,
    val autoSaveHighConfidence: Boolean = true,
    val enabledPackageNames: Set<String> = emptySet(),
    val lastProcessedTimestamp: String? = null,
    val totalImportedCount: Int = 0
)

/**
 * Pending notification candidate awaiting user review
 */
data class NotificationReviewCandidateModel(
    val id: String,
    val candidate: TransactionCandidate,
    val sourcePackage: String,
    val createdAt: String = "",
    val status: String = "PENDING"
)
