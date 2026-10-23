package com.anix.android.anixlifetracker.error

import kotlinx.serialization.Serializable

@Serializable
sealed class AppError(val userMessage: String, val isTransient: Boolean = false) {

    sealed class Network(message: String, isTransient: Boolean = true) : AppError(message, isTransient) {
        object Offline : Network("Device is offline. Work has been saved locally.", isTransient = true)
        object Timeout : Network("Connection timed out. Retrying automatically.", isTransient = true)
        data class ServerError(val code: Int, val serverMessage: String) : Network("Server error ($code): $serverMessage", isTransient = true)
        data class ConnectionFailed(val cause: String) : Network("Network connection failed: $cause", isTransient = true)
    }

    sealed class Auth(message: String) : AppError(message, isTransient = false) {
        object InvalidCredentials : Auth("Invalid email or password.")
        object SessionExpired : Auth("Your session has expired. Please sign in again.")
        object AccountRevoked : Auth("Your account access has been revoked.")
        object UserAlreadyExists : Auth("An account with this email already exists.")
        data class ReauthRequired(val reason: String) : Auth("Re-authentication required: $reason")
    }

    sealed class Forbidden(message: String) : AppError(message, isTransient = false) {
        object OwnershipMismatch : Forbidden("Access denied: Record owner mismatch.")
        object UnauthorizedOperation : Forbidden("You do not have permission to perform this action.")
    }

    sealed class Validation(message: String) : AppError(message, isTransient = false) {
        data class InvalidField(val fieldName: String, val reason: String) : Validation("Invalid field '$fieldName': $reason")
        data class ParsingFailed(val rawSnippet: String) : Validation("Failed to parse transaction source payload.")
    }

    sealed class Conflict(message: String) : AppError(message, isTransient = false) {
        data class RecordVersionMismatch(val entityId: String) : Conflict("Conflict detected for record $entityId.")
    }

    data class RateLimit(val retryAfterSeconds: Long? = null) : AppError("Rate limit exceeded. Please wait before retrying.", isTransient = true)

    sealed class Storage(message: String) : AppError(message, isTransient = false) {
        object DiskFull : Storage("Device storage is full. Operation could not be committed.")
        object QuotaExceeded : Storage("Storage quota exceeded.")
    }

    sealed class Encryption(message: String) : AppError(message, isTransient = false) {
        object KeyUnavailable : Encryption("Cryptographic key is unavailable. Database locked.")
        object DecryptionFailed : Encryption("Failed to decrypt sensitive database payload.")
        object KeyInvalidated : Encryption("Device security keys were invalidated or changed.")
        object MigrationRequired : Encryption("Database encryption migration is required.")
    }

    sealed class Collector(message: String) : AppError(message, isTransient = false) {
        object PermissionDenied : Collector("Required system permission was denied.")
        object ListenerServiceDisconnected : Collector("Notification listener service is disconnected.")
        object FeatureDisabled : Collector("This collector feature is currently disabled.")
    }

    sealed class Schema(message: String) : AppError(message, isTransient = false) {
        data class Corruption(val detail: String) : Schema("Database schema corruption: $detail")
    }

    data class Unknown(val detail: String) : AppError("An unexpected error occurred: $detail", isTransient = false)
}
