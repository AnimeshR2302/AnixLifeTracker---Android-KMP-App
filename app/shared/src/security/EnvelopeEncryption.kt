package com.anix.android.anixlifetracker.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EncryptedEnvelope(
    val version: Int = 1,
    val nonceBase64: String,
    val ciphertextBase64: String,
    val keyVersion: Long = 1L,
    val associatedDataDigest: String? = null
)

object EnvelopeEncryption {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun buildEnvelope(
        nonceBase64: String,
        ciphertextBase64: String,
        keyVersion: Long = 1L,
        ownerId: String = "",
        recordType: String = ""
    ): EncryptedEnvelope {
        val aad = if (ownerId.isNotBlank()) "$ownerId:$recordType" else null
        return EncryptedEnvelope(
            version = 1,
            nonceBase64 = nonceBase64,
            ciphertextBase64 = ciphertextBase64,
            keyVersion = keyVersion,
            associatedDataDigest = aad
        )
    }

    fun serializeEnvelope(envelope: EncryptedEnvelope): String {
        return json.encodeToString(envelope)
    }

    fun deserializeEnvelope(serialized: String): EncryptedEnvelope? {
        return try {
            json.decodeFromString<EncryptedEnvelope>(serialized)
        } catch (_: Exception) {
            null
        }
    }
}
