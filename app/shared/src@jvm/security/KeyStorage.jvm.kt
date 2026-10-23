package com.anix.android.anixlifetracker.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

class JvmKeyStorage : KeyStorage {

    private val _encryptionState = MutableStateFlow(
        EncryptionState(
            status = EncryptionStatus.SUPPORTED,
            platformName = "Desktop JVM (Salted File Vault)",
            isHardwareBacked = false,
            keyVersion = 1L,
            userMessage = "JVM Key Vault initialized"
        )
    )

    private val secretsMap = mutableMapOf<String, String>()
    private var isLocked = false

    override fun getEncryptionState(): StateFlow<EncryptionState> = _encryptionState.asStateFlow()

    override fun getOrCreateMasterKey(alias: String): ByteArray? {
        if (isLocked) return null
        val userHome = System.getProperty("user.home") ?: "."
        val vaultFile = File(userHome, ".anixlifetracker/vault.key")
        val salt = "anix_jvm_salt_2026_v1".toByteArray(Charsets.UTF_8)

        val keyBytes = if (vaultFile.exists()) {
            vaultFile.readBytes()
        } else {
            vaultFile.parentFile?.mkdirs()
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            digest.update(alias.toByteArray(Charsets.UTF_8))
            val derived = digest.digest()
            vaultFile.writeBytes(derived)
            derived
        }
        return keyBytes
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

actual fun createPlatformKeyStorage(context: Any?): KeyStorage = JvmKeyStorage()
