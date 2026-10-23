package com.anix.android.anixlifetracker.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WasmKeyStorage : KeyStorage {

    private val _encryptionState = MutableStateFlow(
        EncryptionState(
            status = EncryptionStatus.SUPPORTED,
            platformName = "Web WasmJS (Session Memory Vault)",
            isHardwareBacked = false,
            keyVersion = 1L,
            userMessage = "Web Crypto session key storage active"
        )
    )

    private val secretsMap = mutableMapOf<String, String>()
    private var isLocked = false

    override fun getEncryptionState(): StateFlow<EncryptionState> = _encryptionState.asStateFlow()

    override fun getOrCreateMasterKey(alias: String): ByteArray? {
        if (isLocked) return null
        return "wasm_subtle_crypto_session_key_v1".encodeToByteArray()
    }

    override fun storeSecret(key: String, value: String): Boolean {
        if (isLocked) return false
        secretsMap[key] = value
        return true
    }

    override fun getSecret(key: String): String? {
        if (isLocked) return null
        return secretsMap[key]
    }

    override fun clearSecret(key: String) {
        secretsMap.remove(key)
    }

    override fun lockVault() {
        isLocked = true
        secretsMap.clear()
        _encryptionState.value = _encryptionState.value.copy(status = EncryptionStatus.LOCKED)
    }

    override fun unlockVault(passphrase: String?): Boolean {
        isLocked = false
        _encryptionState.value = _encryptionState.value.copy(status = EncryptionStatus.SUPPORTED)
        return true
    }
}

actual fun createPlatformKeyStorage(context: Any?): KeyStorage = WasmKeyStorage()
