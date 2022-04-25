package com.untitledkingdom.ueberapp.datastore

import android.util.Base64
import com.untitledkingdom.ueberapp.BuildConfig
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class SecureData @Inject constructor() {
    private companion object {
        const val AES = "AES"
        const val SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
        const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        const val ITERATION_COUNT = 10000
        const val KEY_LENGTH = 256
        const val SECRET_KEY = BuildConfig.CIPHER_KEY
        const val IV = BuildConfig.IV
        const val SALT = BuildConfig.SALT
    }

    fun encrypt(stringToEncrypt: String): String? {
        return try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(IV, Base64.DEFAULT))
            val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
            val spec =
                PBEKeySpec(
                    SECRET_KEY.toCharArray(), Base64.decode(SALT, Base64.DEFAULT),
                    ITERATION_COUNT, KEY_LENGTH
                )
            val generatedSecret = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(generatedSecret.encoded, AES)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            Base64.encodeToString(
                cipher.doFinal(stringToEncrypt.toByteArray(Charsets.UTF_8)),
                Base64.DEFAULT
            )
        } catch (e: Exception) {
            null
        }
    }

    fun decrypt(stringToDecrypt: String): String? {
        return try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(IV, Base64.DEFAULT))
            val factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
            val spec =
                PBEKeySpec(
                    SECRET_KEY.toCharArray(), Base64.decode(SALT, Base64.DEFAULT),
                    ITERATION_COUNT, KEY_LENGTH
                )
            val generatedSecret = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(generatedSecret.encoded, AES)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            String(cipher.doFinal(Base64.decode(stringToDecrypt, Base64.DEFAULT)))
        } catch (e: Exception) {
            null
        }
    }
}
