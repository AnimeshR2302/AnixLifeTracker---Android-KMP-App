package com.anix.android.anixlifetracker.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CapabilityState {
    AVAILABLE,
    PERMISSION_DENIED,
    CONSENT_WITHDRAWN,
    FEATURE_DISABLED,
    KEY_LOCKED,
    PLATFORM_UNSUPPORTED
}

data class IngestionCapabilityStatus(
    val smsCapability: CapabilityState = CapabilityState.AVAILABLE,
    val notificationCapability: CapabilityState = CapabilityState.AVAILABLE,
    val isAndroidPlatform: Boolean = false,
    val activeAllowedAppsCount: Int = 0,
    val statusSummaryMessage: String = ""
)

class PermissionAndCapabilityManager(
    private val keyStorage: KeyStorage
) {
    private val _capabilityStatus = MutableStateFlow(IngestionCapabilityStatus())
    val capabilityStatus: StateFlow<IngestionCapabilityStatus> = _capabilityStatus.asStateFlow()

    fun updateCapabilities(
        isAndroid: Boolean,
        isSmsPermissionGranted: Boolean,
        isSmsConsentGiven: Boolean,
        isSmsEnabled: Boolean,
        isNotificationAccessGranted: Boolean,
        isNotificationConsentGiven: Boolean,
        isNotificationEnabled: Boolean,
        allowedAppsCount: Int
    ) {
        val keyState = keyStorage.getEncryptionState().value

        val smsState = when {
            !isAndroid -> CapabilityState.PLATFORM_UNSUPPORTED
            keyState.status == EncryptionStatus.LOCKED -> CapabilityState.KEY_LOCKED
            !isSmsConsentGiven -> CapabilityState.CONSENT_WITHDRAWN
            !isSmsEnabled -> CapabilityState.FEATURE_DISABLED
            !isSmsPermissionGranted -> CapabilityState.PERMISSION_DENIED
            else -> CapabilityState.AVAILABLE
        }

        val notifState = when {
            !isAndroid -> CapabilityState.PLATFORM_UNSUPPORTED
            keyState.status == EncryptionStatus.LOCKED -> CapabilityState.KEY_LOCKED
            !isNotificationConsentGiven -> CapabilityState.CONSENT_WITHDRAWN
            !isNotificationEnabled -> CapabilityState.FEATURE_DISABLED
            !isNotificationAccessGranted -> CapabilityState.PERMISSION_DENIED
            else -> CapabilityState.AVAILABLE
        }

        val summary = if (!isAndroid) {
            "Automated SMS & notification financial collection originates on Android devices. Collected transactions sync after Android Sync."
        } else {
            "Android Ingestion Status: SMS (${smsState.name}), Notifications (${notifState.name}, $allowedAppsCount apps active)"
        }

        _capabilityStatus.value = IngestionCapabilityStatus(
            smsCapability = smsState,
            notificationCapability = notifState,
            isAndroidPlatform = isAndroid,
            activeAllowedAppsCount = allowedAppsCount,
            statusSummaryMessage = summary
        )
    }
}
