package com.anix.android.anixlifetracker.auth

import kotlin.time.Clock
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: String,
    val lastSignInAt: String? = null
)

@Serializable
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSec: Long,
    val user: UserProfile
) {
    val isExpired: Boolean
        get() {
            val nowMs = Clock.System.now().toEpochMilliseconds()
            return (expiresAtEpochSec * 1000) <= nowMs
        }
}

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val session: UserSession) : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface AuthResult {
    data class Success(val session: UserSession, val message: String? = null) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

sealed class AuthError(val userMessage: String) {
    data object InvalidCredentials : AuthError("Invalid email or password.")
    data object UserAlreadyExists : AuthError("An account with this email already exists.")
    data object SessionExpired : AuthError("Your session has expired. Please sign in again.")
    data class NetworkError(val causeMessage: String) : AuthError("Network connection error: $causeMessage")
    data class Unknown(val causeMessage: String) : AuthError("Authentication failed: $causeMessage")
}

enum class CredentialType {
    EMAIL_PASSWORD,
    MAGIC_LINK,
    REFRESH_TOKEN
}
