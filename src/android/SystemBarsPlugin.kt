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
        private val VALID_ANIMATIONS = setOf("NONE", "FADE")
        private val VALID_STYLES = setOf("DARK", "LIGHT", "DEFAULT")
        private val VALID_BARS = setOf("StatusBar", "NavigationBar")
    }

    // Last setStyle request, re-applied on a theme change (see onConfigurationChanged).
    private var lastStyle: String? = null
    private var lastBar: String? = null

    override fun execute(action: String, args: JSONArray, callback: CallbackContext): Boolean {
        when (action) {
            "setStyle"     -> runOnUi(callback, "setStyle") { applyStyle(args.optJSONObject(0), callback) }
            "show"         -> runOnUi(callback, "show")    { applyVisibility(args.optJSONObject(0), show = true,  callback) }
            "hide"         -> runOnUi(callback, "hide")    { applyVisibility(args.optJSONObject(0), show = false, callback) }
            "setAnimation" -> applyAnimation(args.optJSONObject(0), callback)
            else -> return false   // Cordova replies with INVALID_ACTION.
        }
        return true
    }

    private inline fun runOnUi(
        callback: CallbackContext,
        method: String,
        crossinline block: () -> Unit
    ) {
        cordova.activity.runOnUiThread {
            try {
                block()
            } catch (e: Exception) {
                callback.sendError(OSSystemBarsErrors.operationFailed(method, e.message ?: e.toString()))
            }
        }
    }

    private fun applyStyle(opts: JSONObject?, callback: CallbackContext) {
        val controller = insetsController()
        if (controller == null) {
            callback.sendError(OSSystemBarsErrors.windowControllerUnavailable)
            return
        }

        val style = opts?.optString("style", "DEFAULT") ?: "DEFAULT"
        if (style !in VALID_STYLES) {
            callback.sendError(OSSystemBarsErrors.invalidInput(
                method = "setStyle",
                reason = "'style' must be one of ${VALID_STYLES.joinToString()} (got '$style')."
            ))
            return
        }

        val bar = opts?.optString("bar", null)
        if (bar != null && bar !in VALID_BARS) {
            callback.sendError(OSSystemBarsErrors.invalidInput(
                method = "setStyle",
                reason = "'bar' must be one of ${VALID_BARS.joinToString()} (got '$bar')."
            ))
            return
        }

        lastStyle = style
        lastBar = bar
        applyAppearance(controller, style, bar)
        callback.success()
    }

    // DARK/LIGHT name the background, not the icons (Capacitor semantics): LIGHT → dark icons.
    // DEFAULT follows the system theme.
    private fun applyAppearance(controller: WindowInsetsControllerCompat, style: String, bar: String?) {
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
    }

    // A light/dark switch resets the bars to the theme default, and cordova-android
    // keeps uiMode in configChanges so the WebView never reloads to re-run setStyle.
    // Re-apply the last style so the runtime choice survives the toggle.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val style = lastStyle ?: return
        val controller = insetsController() ?: return
        applyAppearance(controller, style, lastBar)
    }

    private fun applyVisibility(opts: JSONObject?, show: Boolean, callback: CallbackContext) {
        val controller = insetsController()
        if (controller == null) {
            callback.sendError(OSSystemBarsErrors.windowControllerUnavailable)
            return
        }

        // 'animation' is validated for Capacitor parity but unused — Android animates its own.
        val animation = opts?.optString("animation", null)
        if (animation != null && animation !in VALID_ANIMATIONS) {
            callback.sendError(OSSystemBarsErrors.invalidInput(
                method = if (show) "show" else "hide",
                reason = "'animation' must be one of ${VALID_ANIMATIONS.joinToString()} (got '$animation')."
            ))
            return
        }

        val bar = opts?.optString("bar", null)
        if (bar != null && bar !in VALID_BARS) {
            callback.sendError(OSSystemBarsErrors.invalidInput(
                method = if (show) "show" else "hide",
                reason = "'bar' must be one of ${VALID_BARS.joinToString()} (got '$bar')."
            ))
            return
        }

        val types = when (bar) {
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

    // No animation hook on Android (the OS composes its own); validate for parity and ack.
    private fun applyAnimation(opts: JSONObject?, callback: CallbackContext) {
        val animation = opts?.optString("animation", "FADE") ?: "FADE"
        if (animation !in VALID_ANIMATIONS) {
            callback.sendError(OSSystemBarsErrors.invalidInput(
                method = "setAnimation",
                reason = "'animation' must be one of ${VALID_ANIMATIONS.joinToString()} (got '$animation')."
            ))
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
