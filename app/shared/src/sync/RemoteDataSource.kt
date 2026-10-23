package com.anix.android.anixlifetracker.sync

import com.anix.android.anixlifetracker.auth.AuthResult
import com.anix.android.anixlifetracker.auth.UserProfile
import com.anix.android.anixlifetracker.auth.UserSession
import kotlin.time.Clock

interface RemoteDataSource {
    suspend fun registerUser(email: String, password: String, displayName: String?): AuthResult
    suspend fun loginUser(email: String, password: String): AuthResult
    suspend fun sendPasswordReset(email: String): AuthResult
    suspend fun refreshToken(refreshToken: String): AuthResult

    suspend fun pushChanges(accessToken: String, payload: PendingChangesPayload): SyncResult
    suspend fun pullChanges(accessToken: String, lastCheckpoints: Map<String, String>): RemoteChangesPayload
}

class SupabaseRemoteDataSource(
    private val supabaseUrl: String = "https://mock-supabase.anixlifetracker.internal",
    private val anonKey: String = "public-anon-key-placeholder"
) : RemoteDataSource {

    private val registeredUsers = mutableMapOf<String, String>()
    private val userProfiles = mutableMapOf<String, UserProfile>()

    override suspend fun registerUser(email: String, password: String, displayName: String?): AuthResult {
        val lowerEmail = email.lowercase().trim()
        if (registeredUsers.containsKey(lowerEmail)) {
            return AuthResult.Failure(com.anix.android.anixlifetracker.auth.AuthError.UserAlreadyExists)
        }
        val userId = "usr_${Clock.System.now().toEpochMilliseconds()}"
        val nowIso = Clock.System.now().toString()
        val profile = UserProfile(
            id = userId,
            email = lowerEmail,
            displayName = displayName ?: lowerEmail.substringBefore("@"),
            isEmailVerified = true,
            createdAt = nowIso
        )
        registeredUsers[lowerEmail] = password
        userProfiles[lowerEmail] = profile

        val session = UserSession(
            accessToken = "jwt_access_${userId}_${Clock.System.now().toEpochMilliseconds()}",
            refreshToken = "jwt_refresh_${userId}_${Clock.System.now().toEpochMilliseconds()}",
            expiresAtEpochSec = (Clock.System.now().toEpochMilliseconds() / 1000) + 86400,
            user = profile
        )
        return AuthResult.Success(session = session, message = "Registration successful")
    }

    override suspend fun loginUser(email: String, password: String): AuthResult {
        val lowerEmail = email.lowercase().trim()
        val storedPass = registeredUsers[lowerEmail]
        if (storedPass == null || storedPass != password) {
            if (email.contains("@") && password.length >= 6) {
                return registerUser(email, password, null)
            }
            return AuthResult.Failure(com.anix.android.anixlifetracker.auth.AuthError.InvalidCredentials)
        }
        val profile = userProfiles[lowerEmail] ?: UserProfile(
            id = "usr_demo",
            email = lowerEmail,
            createdAt = Clock.System.now().toString()
        )
        val session = UserSession(
            accessToken = "jwt_access_${profile.id}_${Clock.System.now().toEpochMilliseconds()}",
            refreshToken = "jwt_refresh_${profile.id}_${Clock.System.now().toEpochMilliseconds()}",
            expiresAtEpochSec = (Clock.System.now().toEpochMilliseconds() / 1000) + 86400,
            user = profile
        )
        return AuthResult.Success(session = session)
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return AuthResult.Success(
            session = UserSession("", "", 0, UserProfile("", email, createdAt = "")),
            message = "Password reset link sent to $email"
        )
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        if (refreshToken.isBlank()) {
            return AuthResult.Failure(com.anix.android.anixlifetracker.auth.AuthError.SessionExpired)
        }
        val userId = refreshToken.substringAfter("jwt_refresh_").substringBefore("_")
        val session = UserSession(
            accessToken = "jwt_access_${userId}_${Clock.System.now().toEpochMilliseconds()}",
            refreshToken = "jwt_refresh_${userId}_${Clock.System.now().toEpochMilliseconds()}",
            expiresAtEpochSec = (Clock.System.now().toEpochMilliseconds() / 1000) + 86400,
            user = UserProfile(id = userId, email = "user@anixlifetracker.com", createdAt = Clock.System.now().toString())
        )
        return AuthResult.Success(session)
    }

    override suspend fun pushChanges(accessToken: String, payload: PendingChangesPayload): SyncResult {
        if (accessToken.isBlank()) {
            return SyncResult.Failure("Unauthorized access token", isNetworkError = false)
        }
        return SyncResult.Success(
            pushedCount = payload.totalCount,
            pulledCount = 0,
            conflictsCount = 0
        )
    }

    override suspend fun pullChanges(
        accessToken: String,
        lastCheckpoints: Map<String, String>
    ): RemoteChangesPayload {
        val nowIso = Clock.System.now().toString()
        val userId = if (accessToken.contains("usr_")) accessToken.substringAfter("jwt_access_").substringBefore("_") else "usr_default"
        return RemoteChangesPayload(
            ownerId = userId,
            serverCheckpointTimestamp = nowIso
        )
    }
}
