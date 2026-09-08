package ir.cutte.nava.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.data.navaDataStore
import ir.cutte.nava.engine.HttpDispatcher
import ir.cutte.nava.model.DeliveryStatus
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.SmsPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class SmsDispatchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun doWork(): Result {
        val activityId = inputData.getString(KEY_ACTIVITY_ID) ?: return Result.failure()
        val payloadJson = inputData.getString(KEY_PAYLOAD_JSON) ?: return Result.failure()
        val primaryUrl = inputData.getString(KEY_PRIMARY_URL) ?: return Result.failure()
        val secondaryUrl = inputData.getString(KEY_SECONDARY_URL) ?: return Result.failure()
        val authToken = inputData.getString(KEY_AUTH_TOKEN) ?: ""

        val payload = try {
            json.decodeFromString<SmsPayload>(payloadJson)
        } catch (exception: Exception) {
            return Result.failure()
        }

        val repository = SettingsRepository(applicationContext.navaDataStore)
        val now = System.currentTimeMillis()
        val timestampMillis = if (payload.timestamp < 10000000000L) payload.timestamp * 1000L else payload.timestamp
        val ageMillis = now - timestampMillis

        if (ageMillis > TTL_EXPIRY_MILLIS) {
            val expiredActivity = ForwardingActivity(
                id = activityId,
                timestamp = timestampMillis,
                normalizedSender = payload.sender,
                matchedKeyword = payload.matchedKeyword,
                httpStatusCode = 408,
                isSuccess = false,
                snippet = payload.body.take(60),
                deliveryStatus = DeliveryStatus.FAILED_EXPIRED
            )
            repository.recordActivity(expiredActivity)
            return Result.success()
        }

        val dispatcher = HttpDispatcher()
        val dispatchResult = dispatcher.dispatchWithFailover(
            primaryUrl = primaryUrl,
            secondaryUrl = secondaryUrl,
            authToken = authToken,
            payload = payload
        )

        return if (dispatchResult.isSuccess) {
            val replayedActivity = ForwardingActivity(
                id = activityId,
                timestamp = timestampMillis,
                normalizedSender = payload.sender,
                matchedKeyword = payload.matchedKeyword,
                httpStatusCode = dispatchResult.statusCode,
                isSuccess = true,
                snippet = payload.body.take(60),
                deliveryStatus = DeliveryStatus.DISPATCHED_REPLAYED
            )
            repository.recordActivity(replayedActivity)
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_ACTIVITY_ID = "key_activity_id"
        const val KEY_PAYLOAD_JSON = "key_payload_json"
        const val KEY_PRIMARY_URL = "key_primary_url"
        const val KEY_SECONDARY_URL = "key_secondary_url"
        const val KEY_AUTH_TOKEN = "key_auth_token"
        const val TTL_EXPIRY_MILLIS = 15 * 60 * 1000L

        fun enqueue(
            context: Context,
            activityId: String,
            primaryUrl: String,
            secondaryUrl: String,
            authToken: String,
            payload: SmsPayload
        ) {
            val json = Json { encodeDefaults = true }
            val data = Data.Builder()
                .putString(KEY_ACTIVITY_ID, activityId)
                .putString(KEY_PAYLOAD_JSON, json.encodeToString(payload))
                .putString(KEY_PRIMARY_URL, primaryUrl)
                .putString(KEY_SECONDARY_URL, secondaryUrl)
                .putString(KEY_AUTH_TOKEN, authToken)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SmsDispatchWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "nava_sms_$activityId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
