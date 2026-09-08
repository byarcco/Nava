package ir.cutte.nava.engine

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ir.cutte.nava.model.DispatchResult
import ir.cutte.nava.model.HeartbeatPayload
import ir.cutte.nava.model.ProbeStatus
import ir.cutte.nava.model.SmsPayload

class HttpDispatcher(
    private val client: HttpClient = HttpClientProvider.client
) {

    private fun resolveSmsEndpoint(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/api/sms")) trimmed else "$trimmed/api/sms"
    }

    private fun resolvePingEndpoint(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/api/ping")) trimmed else "$trimmed/api/ping"
    }

    suspend fun dispatchWithFailover(
        primaryUrl: String,
        secondaryUrl: String,
        authToken: String,
        payload: SmsPayload
    ): DispatchResult {
        val primaryEndpoint = resolveSmsEndpoint(primaryUrl)
        val primaryResult = executePost(primaryEndpoint, authToken, payload)

        if (primaryResult.isSuccess) {
            return primaryResult
        }

        val secondaryEndpoint = resolveSmsEndpoint(secondaryUrl)
        val secondaryResult = executePost(secondaryEndpoint, authToken, payload)

        return if (secondaryResult.isSuccess) {
            secondaryResult
        } else {
            DispatchResult(
                isSuccess = false,
                statusCode = if (secondaryResult.statusCode > 0) secondaryResult.statusCode else primaryResult.statusCode,
                errorMessage = secondaryResult.errorMessage ?: primaryResult.errorMessage ?: "Primary and secondary endpoints failed"
            )
        }
    }

    private suspend fun executePost(url: String, authToken: String, payload: Any): DispatchResult {
        val effectiveToken = if (authToken.isNotBlank()) authToken else DEFAULT_AUTH_TOKEN
        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $effectiveToken")
                setBody(payload)
            }
            val status = response.status.value
            if (response.status.isSuccess()) {
                DispatchResult(isSuccess = true, statusCode = status, errorMessage = null)
            } else {
                DispatchResult(isSuccess = false, statusCode = status, errorMessage = "HTTP status $status")
            }
        } catch (exception: Exception) {
            DispatchResult(isSuccess = false, statusCode = 0, errorMessage = exception.message ?: "Connection error")
        }
    }

    suspend fun probeHealth(
        primaryUrl: String,
        secondaryUrl: String,
        authToken: String,
        payload: HeartbeatPayload
    ): ProbeStatus {
        val primaryPingUrl = resolvePingEndpoint(primaryUrl)
        val primaryResult = executePost(primaryPingUrl, authToken, payload)
        if (primaryResult.isSuccess) {
            return ProbeStatus.CONNECTED_PRIMARY
        }

        val secondaryPingUrl = resolvePingEndpoint(secondaryUrl)
        val secondaryResult = executePost(secondaryPingUrl, authToken, payload)
        if (secondaryResult.isSuccess) {
            return ProbeStatus.CONNECTED_BACKUP
        }

        val probeInternet = try {
            val response = client.get("https://clients3.google.com/generate_204")
            response.status.value in 200..299
        } catch (ignored: Exception) {
            false
        }

        return if (probeInternet) {
            ProbeStatus.INTERNET_ONLY
        } else {
            ProbeStatus.OFFLINE
        }
    }

    companion object {
        const val DEFAULT_AUTH_TOKEN = "85d8ecf6-5641-451a-a41d-20927eeccd28"
    }
}
