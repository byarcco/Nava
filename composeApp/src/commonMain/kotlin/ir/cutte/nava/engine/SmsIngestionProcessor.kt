package ir.cutte.nava.engine

import ir.cutte.nava.data.SettingsRepository
import ir.cutte.nava.model.AppSettings
import ir.cutte.nava.model.DeliveryStatus
import ir.cutte.nava.model.ForwardingActivity
import ir.cutte.nava.model.SmsPayload
import ir.cutte.nava.util.currentTimeMillis
import kotlin.random.Random

data class IngestionOutcome(
    val isMatched: Boolean,
    val isDispatched: Boolean,
    val statusDescription: String,
    val httpStatusCode: Int
)

class SmsIngestionProcessor(
    private val repository: SettingsRepository,
    private val dispatcher: HttpDispatcher = HttpDispatcher()
) {

    suspend fun processIncoming(
        settings: AppSettings,
        rawSender: String,
        body: String,
        simSlot: Int,
        batteryLevel: Int,
        isCharging: Boolean,
        deviceInfo: String,
        isOnline: Boolean,
        onEnqueueWorker: (String, SmsPayload) -> Unit
    ): IngestionOutcome {
        val matchResult = MessageMatcher.match(
            rawSender = rawSender,
            body = body,
            whitelist = settings.whitelistSenders,
            keywords = settings.keywords
        )

        if (!matchResult.isMatched) {
            return IngestionOutcome(
                isMatched = false,
                isDispatched = false,
                statusDescription = "Ignored: No whitelist or keyword match",
                httpStatusCode = 0
            )
        }

        val timestamp = currentTimeMillis()
        val activityId = "${timestamp}_${Random.nextInt(1000, 9999)}"
        val normalizedSender = SenderNormalizer.normalize(rawSender)
        val snippet = if (body.length > 60) body.take(60) + "..." else body

        val payload = SmsPayload(
            sender = rawSender,
            body = body,
            timestamp = timestamp,
            simSlot = simSlot,
            matchedKeyword = matchResult.matchedKeyword,
            deviceInfo = deviceInfo,
            batteryLevel = batteryLevel,
            isCharging = isCharging
        )

        if (isOnline) {
            val dispatchResult = dispatcher.dispatchWithFailover(
                primaryUrl = settings.primaryWorkerUrl,
                secondaryUrl = settings.secondaryWorkerUrl,
                authToken = settings.authToken,
                payload = payload
            )

            if (dispatchResult.isSuccess) {
                val activity = ForwardingActivity(
                    id = activityId,
                    timestamp = timestamp,
                    normalizedSender = normalizedSender,
                    matchedKeyword = matchResult.matchedKeyword,
                    httpStatusCode = dispatchResult.statusCode,
                    isSuccess = true,
                    snippet = snippet,
                    deliveryStatus = DeliveryStatus.DISPATCHED_INSTANT
                )
                repository.recordActivity(activity)
                return IngestionOutcome(
                    isMatched = true,
                    isDispatched = true,
                    statusDescription = "Dispatched instantly: HTTP ${dispatchResult.statusCode}",
                    httpStatusCode = dispatchResult.statusCode
                )
            }
        }

        onEnqueueWorker(activityId, payload)

        val queuedActivity = ForwardingActivity(
            id = activityId,
            timestamp = timestamp,
            normalizedSender = normalizedSender,
            matchedKeyword = matchResult.matchedKeyword,
            httpStatusCode = 0,
            isSuccess = false,
            snippet = snippet,
            deliveryStatus = DeliveryStatus.QUEUED_OFFLINE
        )
        repository.recordActivity(queuedActivity)

        return IngestionOutcome(
            isMatched = true,
            isDispatched = false,
            statusDescription = "Queued in persistent WorkManager for offline replay",
            httpStatusCode = 0
        )
    }
}
