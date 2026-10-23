package com.anix.android.anixlifetracker.auth

import com.anix.android.anixlifetracker.error.AppError
import com.anix.android.anixlifetracker.error.AppResult
import com.anix.android.anixlifetracker.security.RedactingLogger
import com.anix.android.anixlifetracker.sync.RemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val sessionStorage: SessionStorage = PersistentSessionStorage()
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val refreshMutex = Mutex()

    init {
        val existingSession = sessionStorage.getSession()
        if (existingSession != null) {
            if (!existingSession.isExpired) {
                _authState.value = AuthState.Authenticated(existingSession)
            } else {
                _authState.value = AuthState.Error(AuthError.SessionExpired.userMessage)
            }
        }
    }

    override fun observeAuthState(): StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun getCurrentSession(): UserSession? {
        val session = sessionStorage.getSession() ?: return null
        if (session.isExpired) {
            val refreshResult = restoreSession()
            if (refreshResult is AuthResult.Success) {
                return refreshResult.session
            }
            return null
        }
        return session
    }

    override suspend fun signUp(email: String, password: String, displayName: String?): AuthResult {
        if (email.isBlank() || password.length < 6) {
            return AuthResult.Failure(AuthError.InvalidCredentials)
        }
        _authState.value = AuthState.Loading
        RedactingLogger.i("AuthRepository", "Initiating sign up for email: $email")

        return try {
            val result = remoteDataSource.registerUser(email = email, password = password, displayName = displayName)
            if (result is AuthResult.Success) {
                sessionStorage.saveSession(result.session)
                _authState.value = AuthState.Authenticated(result.session)
            } else if (result is AuthResult.Failure) {
                _authState.value = AuthState.Error(result.error.userMessage)
            }
            result
        } catch (e: Exception) {
            val failure = AuthResult.Failure(AuthError.NetworkError(e.message ?: "Network error"))
            _authState.value = AuthState.Error("Sign up failed: ${e.message}")
            failure
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Failure(AuthError.InvalidCredentials)
        }
        _authState.value = AuthState.Loading
        RedactingLogger.i("AuthRepository", "Initiating sign in for email: $email")

        return try {
            val result = remoteDataSource.loginUser(email = email, password = password)
            if (result is AuthResult.Success) {
                sessionStorage.saveSession(result.session)
                _authState.value = AuthState.Authenticated(result.session)
            } else if (result is AuthResult.Failure) {
                _authState.value = AuthState.Error(result.error.userMessage)
            }
            result
        } catch (e: Exception) {
            val failure = AuthResult.Failure(AuthError.NetworkError(e.message ?: "Connection refused"))
            _authState.value = AuthState.Error("Sign in failed: ${e.message}")
            failure
        }
    }

    override suspend fun signOut(): AuthResult {
        RedactingLogger.i("AuthRepository", "User signing out. Clearing session tokens and isolating account data.")
        sessionStorage.clearSession()
        _authState.value = AuthState.Unauthenticated
        return AuthResult.Success(
            session = UserSession(
                accessToken = "",
                refreshToken = "",
                expiresAtEpochSec = 0,
                user = UserProfile(id = "", email = "", createdAt = "")
            ),
            message = "Signed out successfully. Local records isolated."
        )
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        if (email.isBlank()) {
            return AuthResult.Failure(AuthError.InvalidCredentials)
        }
        RedactingLogger.i("AuthRepository", "Sending password reset email to: $email")
        return try {
            remoteDataSource.sendPasswordReset(email)
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.NetworkError(e.message ?: "Failed to request password reset"))
        }
    }

    override suspend fun restoreSession(): AuthResult {
        // Single-flight refresh coordinator using Mutex
        return refreshMutex.withLock {
            val current = sessionStorage.getSession() ?: return AuthResult.Failure(AuthError.SessionExpired)
            if (!current.isExpired) {
                return AuthResult.Success(current)
            }
            RedactingLogger.i("AuthRepository", "Single-flight token refresh executing for user: ${current.user.id}")

            try {
                val refreshed = remoteDataSource.refreshToken(current.refreshToken)
                if (refreshed is AuthResult.Success) {
                    sessionStorage.saveSession(refreshed.session)
                    _authState.value = AuthState.Authenticated(refreshed.session)
                } else {
                    sessionStorage.clearSession()
                    _authState.value = AuthState.Unauthenticated
                }
                refreshed
            } catch (e: Exception) {
                AuthResult.Failure(AuthError.NetworkError(e.message ?: "Session restoration failed"))
            }
        }
    }
}
