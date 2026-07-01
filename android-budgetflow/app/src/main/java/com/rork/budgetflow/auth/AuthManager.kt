package com.rork.budgetflow.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rork.budgetflow.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom

/** Base URL for Rork Auth API. */
private const val RORK_AUTH_URL = "https://api.rork.com/v1/auth"

/**
 * User information decoded from the Rork Auth JWT.
 */
data class AuthUser(
    val id: String,
    val email: String,
    val name: String?,
)

/**
 * Manages Rork Auth OAuth flow and token storage for Android.
 *
 * Uses PKCE (Proof Key for Code Exchange) with Custom Tabs.
 * Tokens are stored in EncryptedSharedPreferences.
 */
class AuthManager(private val context: Context) {

    private val random = SecureRandom()

    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .build()
        )
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "rork_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _user = MutableStateFlow<AuthUser?>(loadUserFromToken())
    val user: StateFlow<AuthUser?> = _user.asStateFlow()

    /** Short-lived access token (1 hour). */
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    private fun loadUserFromToken(): AuthUser? {
        val token = getAccessToken() ?: return null
        return userFromToken(token)
    }

    /**
     * Start the OAuth sign-in flow. Returns an Intent to launch with Custom Tabs.
     * After the user completes OAuth, the redirect comes back via
     * `handleCallback(uri)`.
     */
    suspend fun beginSignIn(provider: String): String? = withContext(Dispatchers.IO) {
        try {
            val verifier = generateCodeVerifier()
            val challenge = generateCodeChallenge(verifier)

            // Store verifier for the token exchange step
            prefs.edit().putString(KEY_CODE_VERIFIER, verifier).apply()

            val body = JSONObject().apply {
                put("app_key", Config.EXPO_PUBLIC_RORK_APP_KEY)
                put("provider", provider)
                put("code_challenge", challenge)
                put("target", "rn")  // same flow as React Native
                put("env", "native")
            }

            val conn = URL("${RORK_AUTH_URL}/oauth/initiate").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray())

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val authUrl = json.optString("auth_url")
            if (authUrl.isNullOrBlank()) {
                prefs.edit().remove(KEY_CODE_VERIFIER).apply()
                null
            } else {
                authUrl
            }
        } catch (e: Exception) {
            prefs.edit().remove(KEY_CODE_VERIFIER).apply()
            null
        }
    }

    /**
     * Handle the OAuth callback URI. Extracts the authorization code and
     * exchanges it for tokens. Returns true on success.
     */
    suspend fun handleCallback(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val code = uri.getQueryParameter("code") ?: return@withContext false
        val verifier = prefs.getString(KEY_CODE_VERIFIER, null)
        if (verifier == null) return@withContext false
        prefs.edit().remove(KEY_CODE_VERIFIER).apply()

        try {
            val body = JSONObject().apply {
                put("app_key", Config.EXPO_PUBLIC_RORK_APP_KEY)
                put("code", code)
                put("code_verifier", verifier)
            }

            val conn = URL("${RORK_AUTH_URL}/oauth/token").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray())

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val accessToken = json.optString("access_token")
            val refreshToken = json.optString("refresh_token")

            if (accessToken.isNullOrBlank()) return@withContext false

            prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .apply()

            _user.value = userFromToken(accessToken)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Refresh the access token using the stored refresh token. */
    suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        val stored = prefs.getString(KEY_REFRESH, null) ?: return@withContext false
        try {
            val body = JSONObject().apply {
                put("app_key", Config.EXPO_PUBLIC_RORK_APP_KEY)
                put("refresh_token", stored)
            }

            val conn = URL("${RORK_AUTH_URL}/oauth/refresh").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray())

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val accessToken = json.optString("access_token")
            if (accessToken.isNullOrBlank()) {
                signOut()
                return@withContext false
            }

            prefs.edit().putString(KEY_ACCESS, accessToken).apply()
            _user.value = userFromToken(accessToken)
            true
        } catch (e: Exception) {
            signOut()
            false
        }
    }

    fun signOut() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_CODE_VERIFIER)
            .apply()
        _user.value = null
    }

    // ── PKCE helpers ────────────────────────────────────────

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    // ── JWT helpers ─────────────────────────────────────────

    companion object {
        private const val KEY_ACCESS = "rork:access_token"
        private const val KEY_REFRESH = "rork:refresh_token"
        private const val KEY_CODE_VERIFIER = "rork:pkce_verifier"

        /** Decode a JWT payload to extract user info. Returns null if expired or invalid. */
        fun userFromToken(token: String): AuthUser? {
            return try {
                val parts = token.split(".")
                if (parts.size != 3) return null

                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)

                // Check expiration
                val exp = json.optLong("exp", 0)
                if (exp > 0 && exp * 1000 < System.currentTimeMillis()) return null

                AuthUser(
                    id = json.optString("sub", ""),
                    email = json.optString("email", ""),
                    name = json.optString("name", null),
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
