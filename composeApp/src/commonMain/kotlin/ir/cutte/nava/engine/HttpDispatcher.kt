package ir.cutte.nava.engine

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.cutte.nava.model.DispatchResult
import ir.cutte.nava.model.SmsPayload
import kotlinx.coroutines.delay

class HttpDispatcher(
    private val client: HttpClient = HttpClientProvider.client
) {

    suspend fun dispatch(endpointUrl: String, payload: SmsPayload): DispatchResult {
        val maxAttempts = 3
        var currentDelayMs = 1000L
        var lastStatusCode = 0
        var lastErrorMessage = ""

        for (attempt in 1..maxAttempts) {
            try {
                val response = client.post(endpointUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                val status = response.status.value
                if (status in 200..299) {
                    return DispatchResult(
                        isSuccess = true,
                        statusCode = status,
                        errorMessage = null
                    )
                }
                lastStatusCode = status
                lastErrorMessage = "HTTP error $status"
            } catch (exception: Exception) {
                lastErrorMessage = exception.message ?: "Network error"
            }

            if (attempt < maxAttempts) {
                delay(currentDelayMs)
                currentDelayMs *= 2
            }
        }

        return DispatchResult(
            isSuccess = false,
            statusCode = lastStatusCode,
            errorMessage = lastErrorMessage
        )
    }
}
