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

    // Last style/bar requested via setStyle, so a runtime system-theme change
    // can re-apply the active style (see onConfigurationChanged).
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

        // Remember the request so a runtime theme change can re-derive DEFAULT.
        lastStyle = style
        lastBar = bar
        applyAppearance(controller, style, bar)
        callback.success()
    }

    // Note: 'DARK' / 'LIGHT' describe the background, not the icons — this
    // matches Capacitor's enum semantics. 'LIGHT' background → dark icons.
    // 'DEFAULT' derives the icon appearance from the current system theme.
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

    // A runtime light/dark switch makes Android reset the system-bar appearance to
    // the activity theme's default, discarding whatever setStyle applied. Because
    // cordova-android keeps `uiMode` in the activity's configChanges, the activity
    // is NOT recreated and the WebView is NOT reloaded, so the page's setStyle is
    // not re-run to restore it. Re-apply the last requested style so the app's
    // runtime choice survives the toggle (DARK/LIGHT stay put; DEFAULT re-derives
    // from the new theme). Only acts once the app has set a style — otherwise the
    // platform/MABS default is left untouched. Mirrors Capacitor SystemBars.
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

        // The optional per-call 'animation' parameter is validated for API
        // parity with Capacitor's SystemBarsVisibilityOptions. The platform
        // animates the transition itself on Android, so the value is accepted
        // but unused.
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

    // Android composes its own system-bar animations via WindowInsetsController,
    // so there is no platform hook for this on Android. We validate the value to
    // match Capacitor's input contract and acknowledge.
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
