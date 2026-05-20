package com.outsystemscloud.systembars

import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginResult
import org.json.JSONObject

object OSSystemBarsErrors {
    private fun formatErrorCode(number: Int): String {
        return "OS-PLUG-SYSBARS-" + number.toString().padStart(4, '0')
    }

    data class ErrorInfo(
        val code: String,
        val message: String
    )

    fun invalidInput(method: String, reason: String): ErrorInfo = ErrorInfo(
        code = formatErrorCode(5),
        message = "The '$method' input parameters aren't valid: $reason"
    )

    val windowControllerUnavailable: ErrorInfo = ErrorInfo(
        code = formatErrorCode(10),
        message = "WindowInsetsController is unavailable on this device."
    )

    fun operationFailed(method: String, errorMessage: String): ErrorInfo = ErrorInfo(
        code = formatErrorCode(13),
        message = "'$method' failed with${if (errorMessage.isNotBlank()) ": $errorMessage" else " an unknown error."}"
    )
}

internal fun CallbackContext.sendError(error: OSSystemBarsErrors.ErrorInfo) {
    val pluginResult = PluginResult(
        PluginResult.Status.ERROR,
        JSONObject().apply {
            put("code", error.code)
            put("message", error.message)
        }
    )
    this.sendPluginResult(pluginResult)
}
