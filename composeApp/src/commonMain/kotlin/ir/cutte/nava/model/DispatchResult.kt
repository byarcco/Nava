package ir.cutte.nava.model

import kotlinx.serialization.Serializable

@Serializable
data class DispatchResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val errorMessage: String? = null
)
