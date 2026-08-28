package com.venkateshgowda.personallibrary.data

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

object EncryptedArchive {
    private const val VERSION: Byte = 1
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256

    fun encrypt(plain: ByteArray, passphrase: CharArray): ByteArray {
        require(passphrase.isNotEmpty()) { "A backup passphrase is required." }
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_BYTES).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        return byteArrayOf(VERSION) + salt + iv + cipher.doFinal(plain)
    }

    fun decrypt(encrypted: ByteArray, passphrase: CharArray): ByteArray {
        require(encrypted.size > 1 + SALT_BYTES + IV_BYTES + 16 && encrypted[0] == VERSION) { "This is not a supported encrypted backup." }
        val salt = encrypted.copyOfRange(1, 1 + SALT_BYTES)
        val iv = encrypted.copyOfRange(1 + SALT_BYTES, 1 + SALT_BYTES + IV_BYTES)
        val payload = encrypted.copyOfRange(1 + SALT_BYTES + IV_BYTES, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(passphrase, salt), GCMParameterSpec(128, iv))
        return cipher.doFinal(payload)
    }

    private fun key(passphrase: CharArray, salt: ByteArray) = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        .generateSecret(PBEKeySpec(passphrase, salt, ITERATIONS, KEY_BITS)).encoded
        .let { javax.crypto.spec.SecretKeySpec(it, "AES") }
}