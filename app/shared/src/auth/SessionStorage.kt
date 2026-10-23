package com.anix.android.anixlifetracker.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SessionStorage {
    fun getSession(): UserSession?
    fun saveSession(session: UserSession)
    fun clearSession()
    fun observeSession(): StateFlow<UserSession?>
}

class InMemorySessionStorage : SessionStorage {
    private val _sessionState = MutableStateFlow<UserSession?>(null)
    private var inMemorySession: UserSession? = null

    override fun getSession(): UserSession? = inMemorySession

    override fun saveSession(session: UserSession) {
        inMemorySession = session
        _sessionState.value = session
    }

    override fun clearSession() {
        inMemorySession = null
        _sessionState.value = null
    }

    override fun observeSession(): StateFlow<UserSession?> = _sessionState.asStateFlow()
}

class PersistentSessionStorage(
    private val memoryStorage: InMemorySessionStorage = InMemorySessionStorage()
) : SessionStorage by memoryStorage {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var serializedSessionCache: String? = null

    init {
        // Hydrate from cached representation if present
        serializedSessionCache?.let { raw ->
            try {
                val restored = json.decodeFromString<UserSession>(raw)
                memoryStorage.saveSession(restored)
            } catch (_: Exception) {
                // Ignore corrupt session cache
            }
        }
    }

    override fun saveSession(session: UserSession) {
        try {
            serializedSessionCache = json.encodeToString(session)
        } catch (_: Exception) {
            // Memory storage still holds reference
        }
        memoryStorage.saveSession(session)
    }

    override fun clearSession() {
        serializedSessionCache = null
        memoryStorage.clearSession()
    }
}
