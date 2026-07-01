package com.rork.budgetflow.data

import android.content.Context
import com.rork.budgetflow.Config
import com.rork.budgetflow.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Status of the sync connection to the backend.
 */
sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Error(val message: String) : SyncStatus
    data object Connected : SyncStatus
}

/**
 * Result of a sync operation — either loaded data or an error with fallback.
 */
data class SyncResult(
    val data: BudgetData,
    val serverVersion: Int,
    val isFromServer: Boolean,
)

/**
 * Replaces [BudgetRepository] with cloud-synced storage via the Cloudflare Worker API.
 *
 * On first launch after sign-in, pulls the latest data from the server.
 * Every local save pushes to the server immediately.
 * Version-based conflict detection prevents overwrites.
 */
class SyncRepository(
    private val context: Context,
    private val authManager: AuthManager,
) {
    // Fallback local storage for offline support
    private val localRepo = BudgetRepository(context)

    private val json = Json { ignoreUnknownKeys = true }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    /** The currently loaded data — starts from local, then cloud takes over. */
    private val _data = MutableStateFlow(localRepo.load())
    val data: StateFlow<BudgetData> = _data.asStateFlow()

    private var serverVersion = 0

    /**
     * Initial sync — pull latest from cloud. If server has data and it differs
     * from local, use the server version. Otherwise push local data up.
     */
    suspend fun initialSync(): SyncResult = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.Syncing
        try {
            val result = apiGet("/api/data")
            val serverData = result?.optJSONObject("data")
            val svVersion = result?.optInt("version", 0) ?: 0

            if (serverData != null) {
                val parsed = json.decodeFromString(BudgetData.serializer(), serverData.toString())
                serverVersion = svVersion
                _data.value = parsed
                // Also persist locally for offline
                localRepo.save(parsed)
                _syncStatus.value = SyncStatus.Connected
                SyncResult(parsed, svVersion, isFromServer = true)
            } else {
                // No server data yet — push local
                pushData(_data.value)
                _syncStatus.value = SyncStatus.Connected
                SyncResult(_data.value, serverVersion, isFromServer = false)
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Sync failed")
            SyncResult(_data.value, serverVersion, isFromServer = false)
        }
    }

    /**
     * Save data locally AND push to cloud. Returns the updated data state.
     */
    suspend fun save(data: BudgetData): Boolean = withContext(Dispatchers.IO) {
        _data.value = data
        localRepo.save(data)
        pushData(data)
    }

    /** Force push current local data to cloud (e.g. after resolving a conflict). */
    suspend fun pushCurrent(): Boolean = withContext(Dispatchers.IO) {
        pushData(_data.value)
    }

    /** Pull latest from cloud and merge. */
    suspend fun pullLatest(): BudgetData? = withContext(Dispatchers.IO) {
        try {
            val result = apiGet("/api/data")
            val serverData = result?.optJSONObject("data") ?: return@withContext null
            val svVersion = result.optInt("version", 0)
            val parsed = json.decodeFromString(BudgetData.serializer(), serverData.toString())
            serverVersion = svVersion
            _data.value = parsed
            localRepo.save(parsed)
            parsed
        } catch (e: Exception) {
            null
        }
    }

    // ── Household API ───────────────────────────────────────

    data class HouseholdInfo(val id: String, val code: String, val name: String)

    suspend fun createHousehold(name: String = "My Household"): HouseholdInfo? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("name", name) }
            val result = apiPost("/api/households", body)
            val hh = result?.optJSONObject("household") ?: return@withContext null
            HouseholdInfo(
                id = hh.getString("id"),
                code = hh.getString("code"),
                name = hh.getString("name"),
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun joinHousehold(code: String): HouseholdInfo? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("code", code) }
            val result = apiPost("/api/households/join", body)
            val hh = result?.optJSONObject("household") ?: return@withContext null
            HouseholdInfo(
                id = hh.getString("id"),
                code = hh.getString("code"),
                name = hh.getString("name"),
            )
        } catch (_: Exception) {
            null
        }
    }

    // ── Internal ────────────────────────────────────────────

    private fun pushData(data: BudgetData): Boolean {
        return try {
            val dataJson = JSONObject(json.encodeToString(BudgetData.serializer(), data))
            val body = JSONObject().apply {
                put("data", dataJson)
                put("version", serverVersion)
            }
            val result = apiPut("/api/data", body)
            val newVersion = result?.optInt("version", serverVersion) ?: serverVersion
            serverVersion = newVersion
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun apiGet(path: String): JSONObject? {
        val token = authManager.getAccessToken() ?: return null
        val conn = URL("${Config.EXPO_PUBLIC_RORK_FUNCTIONS_URL}${path}").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return try {
            val code = conn.responseCode
            if (code in 200..299) {
                val body = conn.inputStream.bufferedReader().readText()
                JSONObject(body)
            } else null
        } finally {
            conn.disconnect()
        }
    }

    private fun apiPost(path: String, body: JSONObject): JSONObject? {
        val token = authManager.getAccessToken() ?: return null
        val conn = URL("${Config.EXPO_PUBLIC_RORK_FUNCTIONS_URL}${path}").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return try {
            conn.outputStream.write(body.toString().toByteArray())
            val code = conn.responseCode
            if (code in 200..299) {
                val resp = conn.inputStream.bufferedReader().readText()
                JSONObject(resp)
            } else null
        } finally {
            conn.disconnect()
        }
    }

    private fun apiPut(path: String, body: JSONObject): JSONObject? {
        val token = authManager.getAccessToken() ?: return null
        val conn = URL("${Config.EXPO_PUBLIC_RORK_FUNCTIONS_URL}${path}").openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return try {
            conn.outputStream.write(body.toString().toByteArray())
            val code = conn.responseCode
            if (code in 200..299) {
                val resp = conn.inputStream.bufferedReader().readText()
                JSONObject(resp)
            } else null
        } finally {
            conn.disconnect()
        }
    }
}
