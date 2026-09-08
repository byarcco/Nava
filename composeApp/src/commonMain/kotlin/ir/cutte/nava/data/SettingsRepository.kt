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

    private val workerUrlKey = stringPreferencesKey("worker_url")
    private val whitelistKey = stringSetPreferencesKey("whitelist_senders")
    private val keywordsKey = stringSetPreferencesKey("keywords")
    private val serviceEnabledKey = booleanPreferencesKey("service_enabled")
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
            val workerUrl = preferences[workerUrlKey] ?: "https://sms.cutte.ir"
            val whitelist = preferences[whitelistKey] ?: emptySet()
            val keywords = preferences[keywordsKey] ?: setOf("کد ورود")
            val isEnabled = preferences[serviceEnabledKey] ?: true
            val totalDispatched = preferences[totalDispatchedKey] ?: 0L
            val startTimestamp = preferences[serviceStartTimestampKey] ?: 0L
            val activitiesJson = preferences[recentActivitiesKey] ?: "[]"
            val activities: List<ForwardingActivity> = try {
                json.decodeFromString(activitiesJson)
            } catch (exception: Exception) {
                emptyList()
            }

            AppSettings(
                workerUrl = workerUrl,
                whitelistSenders = whitelist,
                keywords = keywords,
                isServiceEnabled = isEnabled,
                totalDispatchedCount = totalDispatched,
                serviceStartTimestamp = startTimestamp,
                recentActivities = activities
            )
        }

    suspend fun getSnapshot(): AppSettings = settingsFlow.first()

    suspend fun updateWorkerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[workerUrlKey] = url.trim()
        }
    }

    suspend fun addWhitelistSender(sender: String) {
        val trimmed = sender.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            val current = preferences[whitelistKey] ?: emptySet()
            preferences[whitelistKey] = current + trimmed
        }
    }

    suspend fun removeWhitelistSender(sender: String) {
        dataStore.edit { preferences ->
            val current = preferences[whitelistKey] ?: emptySet()
            preferences[whitelistKey] = current - sender
        }
    }

    suspend fun addKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            val current = preferences[keywordsKey] ?: setOf("کد ورود")
            preferences[keywordsKey] = current + trimmed
        }
    }

    suspend fun removeKeyword(keyword: String) {
        dataStore.edit { preferences ->
            val current = preferences[keywordsKey] ?: setOf("کد ورود")
            preferences[keywordsKey] = current - keyword
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

    suspend fun setServiceStartTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[serviceStartTimestampKey] = timestamp
        }
    }

    suspend fun recordActivity(activity: ForwardingActivity) {
        dataStore.edit { preferences ->
            val currentCount = preferences[totalDispatchedKey] ?: 0L
            if (activity.isSuccess) {
                preferences[totalDispatchedKey] = currentCount + 1L
            }

            val currentJson = preferences[recentActivitiesKey] ?: "[]"
            val currentList: List<ForwardingActivity> = try {
                json.decodeFromString(currentJson)
            } catch (exception: Exception) {
                emptyList()
            }

            val updatedList = (listOf(activity) + currentList).take(50)
            preferences[recentActivitiesKey] = json.encodeToString(updatedList)
        }
    }
}
