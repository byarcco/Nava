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
        forceDispatch: Boolean = false,
        onEnqueueWorker: (String, SmsPayload) -> Unit
    ): IngestionOutcome {
        val matchResult = MessageMatcher.match(
            rawSender = rawSender,
            body = body,
            whitelist = settings.whitelistSenders,
            keywords = settings.keywords
        )

        val shouldForward = settings.isForwardAllEnabled || matchResult.isMatched || forceDispatch

        if (!shouldForward) {
            return IngestionOutcome(
                isMatched = false,
                isDispatched = false,
                statusDescription = "Ignored: No whitelist or keyword match",
                httpStatusCode = 0
            )
        }

        val effectiveKeyword = when {
            matchResult.matchedKeyword != null -> matchResult.matchedKeyword
            settings.isForwardAllEnabled -> "انتقال خودکار"
            else -> "آزمایشی"
        }
        val timestamp = currentTimeMillis()
        val activityId = "${timestamp}_${Random.nextInt(1000, 9999)}"
        val normalizedSender = SenderNormalizer.normalize(rawSender)
        val snippet = if (body.length > 60) body.take(60) + "..." else body
        val code = OtpExtractor.extractCode(body)

        val resolvedDeviceId = settings.deviceId.ifBlank { deviceInfo }
        val resolvedDeviceName = settings.deviceName.ifBlank { deviceInfo }

        val payload = SmsPayload(
            code = code,
            timestamp = timestamp / 1000L,
            deviceId = resolvedDeviceId,
            deviceName = resolvedDeviceName,
            sender = rawSender,
            body = body,
            simSlot = simSlot,
            matchedKeyword = effectiveKeyword,
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

            val activity = ForwardingActivity(
                id = activityId,
                timestamp = timestamp,
                normalizedSender = if (normalizedSender.isNotBlank()) normalizedSender else rawSender,
                matchedKeyword = effectiveKeyword,
                httpStatusCode = dispatchResult.statusCode,
                isSuccess = dispatchResult.isSuccess,
                snippet = snippet,
                deliveryStatus = if (dispatchResult.isSuccess) DeliveryStatus.DISPATCHED_INSTANT else DeliveryStatus.QUEUED_OFFLINE
            )
            repository.recordActivity(activity)

            if (dispatchResult.isSuccess) {
                repository.resetProbeBackoff()
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
            normalizedSender = if (normalizedSender.isNotBlank()) normalizedSender else rawSender,
            matchedKeyword = effectiveKeyword,
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
