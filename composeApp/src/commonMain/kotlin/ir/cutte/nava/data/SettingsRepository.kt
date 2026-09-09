package ir.cutte.nava.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import ir.cutte.nava.model.AppSettings
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.ProbeStatus
import ir.cutte.nava.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    private val primaryWorkerUrlKey = stringPreferencesKey("primary_worker_url")
    private val secondaryWorkerUrlKey = stringPreferencesKey("secondary_worker_url")
    private val authTokenKey = stringPreferencesKey("auth_token")
    private val whitelistKey = stringSetPreferencesKey("whitelist_senders")
    private val keywordsKey = stringSetPreferencesKey("keywords")
    private val forwardAllEnabledKey = booleanPreferencesKey("forward_all_enabled")
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val deviceNameKey = stringPreferencesKey("device_name")
    private val serviceEnabledKey = booleanPreferencesKey("service_enabled")
    private val heartbeatEnabledKey = booleanPreferencesKey("heartbeat_enabled")
    private val oemAutostartConfiguredKey = booleanPreferencesKey("oem_autostart_configured")
    private val lastProbeStatusKey = stringPreferencesKey("last_probe_status")
    private val lastProbeTimestampKey = longPreferencesKey("last_probe_timestamp")
    private val firstFailedProbeTimestampKey = longPreferencesKey("first_failed_probe_timestamp")
    private val currentProbeIntervalMinutesKey = longPreferencesKey("current_probe_interval_minutes")
    private val totalDispatchedKey = longPreferencesKey("total_dispatched_count")
    private val serviceStartTimestampKey = longPreferencesKey("service_start_timestamp")
    private val recentActivitiesKey = stringPreferencesKey("recent_activities_json")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            val primaryUrl = preferences[primaryWorkerUrlKey] ?: "https://sms.cutte.ir"
            val secondaryUrl = preferences[secondaryWorkerUrlKey] ?: "https://nava.imartrioss.workers.dev"
            val authToken = preferences[authTokenKey] ?: "85d8ecf6-5641-451a-a41d-20927eeccd28"
            val whitelist = preferences[whitelistKey] ?: setOf("Raja.ir")
            val keywords = preferences[keywordsKey] ?: setOf("کد ورود", "کد تایید", "کد تائید")
            val isForwardAll = preferences[forwardAllEnabledKey] ?: false
            val deviceId = preferences[deviceIdKey] ?: ""
            val deviceName = preferences[deviceNameKey] ?: ""
            val isEnabled = preferences[serviceEnabledKey] ?: true
            val isHeartbeat = preferences[heartbeatEnabledKey] ?: true
            val isOem = preferences[oemAutostartConfiguredKey] ?: false
            val probeStatusString = preferences[lastProbeStatusKey]
            val probeStatus = probeStatusString?.let {
                try {
                    ProbeStatus.valueOf(it)
                } catch (ignored: Exception) {
                    ProbeStatus.CONNECTED_PRIMARY
                }
            } ?: ProbeStatus.CONNECTED_PRIMARY
            val lastProbeTimestamp = preferences[lastProbeTimestampKey] ?: 0L
            val firstFailedProbe = preferences[firstFailedProbeTimestampKey] ?: 0L
            val currentInterval = preferences[currentProbeIntervalMinutesKey] ?: 30L
            val totalDispatched = preferences[totalDispatchedKey] ?: 0L
            val startTimestamp = preferences[serviceStartTimestampKey] ?: 0L
            val activitiesJson = preferences[recentActivitiesKey] ?: "[]"
            val activities: List<ForwardingActivity> = try {
                json.decodeFromString(activitiesJson)
            } catch (exception: Exception) {
                emptyList()
            }

            AppSettings(
                primaryWorkerUrl = primaryUrl,
                secondaryWorkerUrl = secondaryUrl,
                authToken = authToken,
                whitelistSenders = whitelist,
                keywords = keywords,
                isForwardAllEnabled = isForwardAll,
                deviceId = deviceId,
                deviceName = deviceName,
                isServiceEnabled = isEnabled,
                isHeartbeatEnabled = isHeartbeat,
                isOemAutostartConfigured = isOem,
                lastProbeStatus = probeStatus,
                lastProbeTimestamp = lastProbeTimestamp,
                firstFailedProbeTimestamp = firstFailedProbe,
                currentProbeIntervalMinutes = currentInterval,
                totalDispatchedCount = totalDispatched,
                serviceStartTimestamp = startTimestamp,
                recentActivities = activities
            )
        }

    suspend fun getSnapshot(): AppSettings = settingsFlow.first()

    suspend fun updatePrimaryWorkerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[primaryWorkerUrlKey] = url.trim()
        }
    }

    suspend fun updateSecondaryWorkerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[secondaryWorkerUrlKey] = url.trim()
        }
    }

    suspend fun updateAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = token.trim()
        }
    }

    suspend fun updateDeviceName(name: String) {
        dataStore.edit { preferences ->
            preferences[deviceNameKey] = name.trim()
        }
    }

    suspend fun ensureDeviceIdentification(defaultId: String, defaultName: String) {
        dataStore.edit { preferences ->
            if (preferences[deviceIdKey].isNullOrBlank()) {
                preferences[deviceIdKey] = defaultId
            }
            if (preferences[deviceNameKey].isNullOrBlank()) {
                preferences[deviceNameKey] = defaultName
            }
            if ((preferences[serviceStartTimestampKey] ?: 0L) == 0L) {
                preferences[serviceStartTimestampKey] = currentTimeMillis()
            }
        }
    }

    suspend fun addWhitelistSender(sender: String) {
        val trimmed = sender.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            val current = preferences[whitelistKey] ?: setOf("Raja.ir")
            preferences[whitelistKey] = current + trimmed
        }
    }

    suspend fun removeWhitelistSender(sender: String) {
        dataStore.edit { preferences ->
            val current = preferences[whitelistKey] ?: setOf("Raja.ir")
            preferences[whitelistKey] = current - sender
        }
    }

    suspend fun addKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            val current = preferences[keywordsKey] ?: setOf("کد ورود", "کد تایید", "کد تائید")
            preferences[keywordsKey] = current + trimmed
        }
    }

    suspend fun removeKeyword(keyword: String) {
        dataStore.edit { preferences ->
            val current = preferences[keywordsKey] ?: setOf("کد ورود", "کد تایید", "کد تائید")
            preferences[keywordsKey] = current - keyword
        }
    }

    suspend fun setForwardAllEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[forwardAllEnabledKey] = enabled
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[serviceEnabledKey] = enabled
            if (enabled && (preferences[serviceStartTimestampKey] ?: 0L) == 0L) {
                preferences[serviceStartTimestampKey] = currentTimeMillis()
            } else if (!enabled) {
                preferences[serviceStartTimestampKey] = 0L
            }
        }
    }

    suspend fun setHeartbeatEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[heartbeatEnabledKey] = enabled
        }
    }

    suspend fun setOemAutostartConfigured(isConfigured: Boolean) {
        dataStore.edit { preferences ->
            preferences[oemAutostartConfiguredKey] = isConfigured
        }
    }

    suspend fun updateProbeStatus(status: ProbeStatus, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[lastProbeStatusKey] = status.name
            preferences[lastProbeTimestampKey] = timestamp
        }
    }

    suspend fun updateProbeBackoff(firstFailedProbeTimestamp: Long, intervalMinutes: Long) {
        dataStore.edit { preferences ->
            preferences[firstFailedProbeTimestampKey] = firstFailedProbeTimestamp
            preferences[currentProbeIntervalMinutesKey] = intervalMinutes
        }
    }

    suspend fun resetProbeBackoff() {
        dataStore.edit { preferences ->
            preferences[firstFailedProbeTimestampKey] = 0L
            preferences[currentProbeIntervalMinutesKey] = 30L
        }
    }

    suspend fun setServiceStartTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[serviceStartTimestampKey] = timestamp
        }
    }

    suspend fun incrementDispatchedCount() {
        dataStore.edit { preferences ->
            val current = preferences[totalDispatchedKey] ?: 0L
            preferences[totalDispatchedKey] = current + 1L
        }
    }

    suspend fun recordActivity(activity: ForwardingActivity) {
        dataStore.edit { preferences ->
            val currentJson = preferences[recentActivitiesKey] ?: "[]"
            val currentList: List<ForwardingActivity> = try {
                json.decodeFromString(currentJson)
            } catch (exception: Exception) {
                emptyList()
            }

            val existingIndex = currentList.indexOfFirst { it.id == activity.id }
            if (existingIndex < 0) {
                val currentCount = preferences[totalDispatchedKey] ?: 0L
                preferences[totalDispatchedKey] = currentCount + 1L
            }

            val updatedList = if (existingIndex >= 0) {
                currentList.toMutableList().apply { set(existingIndex, activity) }
            } else {
                listOf(activity) + currentList
            }.take(50)

            preferences[recentActivitiesKey] = json.encodeToString(updatedList)
        }
    }
}
