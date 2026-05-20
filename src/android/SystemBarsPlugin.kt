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

    companion object {
        private val VALID_ANIMATIONS = setOf("NONE", "SLIDE", "FADE")
    }

    override fun execute(action: String, args: JSONArray, callback: CallbackContext): Boolean {
        when (action) {
            "setStyle"     -> runOnUi(callback) { applyStyle(args.optJSONObject(0), callback) }
            "show"         -> runOnUi(callback) { applyVisibility(args.optJSONObject(0), show = true,  callback) }
            "hide"         -> runOnUi(callback) { applyVisibility(args.optJSONObject(0), show = false, callback) }
            "setAnimation" -> applyAnimation(args.optJSONObject(0), callback)
            else -> return false   // Cordova replies with INVALID_ACTION.
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
        val controller = insetsController()
        if (controller == null) {
            callback.error("WindowInsetsController unavailable")
            return
        }

        val style = opts?.optString("style", "DEFAULT") ?: "DEFAULT"
        val bar   = opts?.optString("bar", null)

        // Note: 'DARK' / 'LIGHT' describe the background, not the icons —
        // this matches Capacitor's enum semantics. 'LIGHT' background → dark icons.
        val appearanceLight: Boolean = when (style) {
            "LIGHT" -> true
            "DARK"  -> false
            else    -> !isNightMode()
        }

        when (bar) {
            "StatusBar"     -> controller.isAppearanceLightStatusBars     = appearanceLight
            "NavigationBar" -> controller.isAppearanceLightNavigationBars = appearanceLight
            else -> {
                controller.isAppearanceLightStatusBars     = appearanceLight
                controller.isAppearanceLightNavigationBars = appearanceLight
            }
        }

        callback.success()
    }

    private fun applyVisibility(opts: JSONObject?, show: Boolean, callback: CallbackContext) {
        val controller = insetsController()
        if (controller == null) {
            callback.error("WindowInsetsController unavailable")
            return
        }

        val types = when (opts?.optString("bar", null)) {
            "StatusBar"     -> WindowInsetsCompat.Type.statusBars()
            "NavigationBar" -> WindowInsetsCompat.Type.navigationBars()
            else            -> WindowInsetsCompat.Type.systemBars()
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

    // Android composes its own system-bar animations via WindowInsetsController,
    // so there is no platform hook for this on Android. We validate the value to
    // match Capacitor's input contract and acknowledge.
    private fun applyAnimation(opts: JSONObject?, callback: CallbackContext) {
        val animation = opts?.optString("animation", "FADE") ?: "FADE"
        if (animation !in VALID_ANIMATIONS) {
            callback.error("Invalid animation: $animation")
            return
        }
        callback.success()
    }

    private fun insetsController(): WindowInsetsControllerCompat? {
        val window = cordova.activity.window
        return WindowCompat.getInsetsController(window, window.decorView)
    }

    private fun isNightMode(): Boolean {
        val night = cordova.activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return night == Configuration.UI_MODE_NIGHT_YES
    }
}
