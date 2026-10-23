package com.anix.android.anixlifetracker.security

import kotlinx.coroutines.flow.StateFlow

interface KeyStorage {
    fun getEncryptionState(): StateFlow<EncryptionState>
    fun getOrCreateMasterKey(alias: String = "anix_db_key"): ByteArray?
    fun storeSecret(key: String, value: String): Boolean
    fun getSecret(key: String): String?
    fun clearSecret(key: String)
    fun lockVault()
    fun unlockVault(passphrase: String? = null): Boolean
}

expect fun createPlatformKeyStorage(context: Any? = null): KeyStorage
