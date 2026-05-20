package com.outsystemscloud.systembars

import android.content.res.Configuration
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONObject

class SystemBarsPlugin : CordovaPlugin() {

    // Persisted for parity with Capacitor's API. Android composes its own
    // system-bar animations via WindowInsetsController; this value is recorded
    // but not used to override the platform animation.
    private var currentAnimation: String = "FADE"

    override fun execute(action: String, args: JSONArray, callback: CallbackContext): Boolean {
        when (action) {
            "setStyle" -> runOnUi(callback) { applyStyle(args.optJSONObject(0), callback) }
            "show" -> runOnUi(callback) { applyVisibility(args.optJSONObject(0), true, callback) }
            "hide" -> runOnUi(callback) { applyVisibility(args.optJSONObject(0), false, callback) }
            "setAnimation" -> runOnUi(callback) { applyAnimation(args.optJSONObject(0), callback) }
            else -> {
                callback.error("Unknown action: $action")
                return true
            }
        }
        return true
    }

    private inline fun runOnUi(callback: CallbackContext, crossinline block: () -> Unit) {
        cordova.activity.runOnUiThread {
            try {
                block()
            } catch (e: Exception) {
                callback.error(e.message ?: e.toString())
            }
        }
    }

    private fun applyStyle(opts: JSONObject?, callback: CallbackContext) {
        val window = cordova.activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (controller == null) {
            callback.error("WindowInsetsController unavailable")
            return
        }

        val style = opts?.optString("style", "DEFAULT") ?: "DEFAULT"
        val bar = opts?.optString("bar", null)

        val appearanceLight: Boolean = when (style) {
            "LIGHT" -> true                  // Light background → dark icons
            "DARK" -> false                  // Dark background → light icons
            else -> {                        // DEFAULT — follow system theme
                val night = cordova.activity.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
                night != Configuration.UI_MODE_NIGHT_YES
            }
        }

        when (bar) {
            "StatusBar" -> controller.isAppearanceLightStatusBars = appearanceLight
            "NavigationBar" -> controller.isAppearanceLightNavigationBars = appearanceLight
            else -> {
                controller.isAppearanceLightStatusBars = appearanceLight
                controller.isAppearanceLightNavigationBars = appearanceLight
            }
        }

        callback.success()
    }

    private fun applyVisibility(opts: JSONObject?, show: Boolean, callback: CallbackContext) {
        val window = cordova.activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (controller == null) {
            callback.error("WindowInsetsController unavailable")
            return
        }

        val bar = opts?.optString("bar", null)
        val types = when (bar) {
            "StatusBar" -> WindowInsetsCompat.Type.statusBars()
            "NavigationBar" -> WindowInsetsCompat.Type.navigationBars()
            else -> WindowInsetsCompat.Type.systemBars()
        }

        if (show) {
            controller.show(types)
        } else {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(types)
        }

        callback.success()
    }

    private fun applyAnimation(opts: JSONObject?, callback: CallbackContext) {
        val animation = opts?.optString("animation", "FADE") ?: "FADE"
        if (animation !in setOf("NONE", "SLIDE", "FADE")) {
            callback.error("Invalid animation: $animation")
            return
        }
        currentAnimation = animation
        callback.success()
    }
}
