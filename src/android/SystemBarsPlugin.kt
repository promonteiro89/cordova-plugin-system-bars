package com.outsystemscloud.systembars

import android.content.res.Configuration
import android.graphics.Color
import androidx.core.view.ViewCompat
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
            "setColor" -> runOnUi(callback) { applyColor(args.optJSONObject(0), callback) }
            "setHomeIndicatorHidden" -> {
                // Android has no home indicator. Resolve successfully for
                // cross-platform parity (mirrors iOS NavigationBar no-op).
                callback.success()
            }
            "getInsets" -> runOnUi(callback) { applyGetInsets(callback) }
            "getInfo" -> runOnUi(callback) { applyGetInfo(callback) }
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

    private fun applyGetInsets(callback: CallbackContext) {
        val decor = cordova.activity.window.decorView
        val windowInsets = ViewCompat.getRootWindowInsets(decor)
        if (windowInsets == null) {
            callback.error("Window insets not available")
            return
        }
        // Return CSS pixels (== iOS points) for cross-platform consistency.
        // Android's WindowInsets are in physical pixels; divide by density.
        val density = cordova.activity.resources.displayMetrics.density
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val result = JSONObject()
            .put("top", (insets.top / density).toDouble())
            .put("bottom", (insets.bottom / density).toDouble())
            .put("left", (insets.left / density).toDouble())
            .put("right", (insets.right / density).toDouble())
        callback.success(result)
    }

    private fun applyGetInfo(callback: CallbackContext) {
        val window = cordova.activity.window
        val decor = window.decorView
        val controller = WindowCompat.getInsetsController(window, decor)
        val windowInsets = ViewCompat.getRootWindowInsets(decor)

        val statusVisible = windowInsets?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
        val navVisible = windowInsets?.isVisible(WindowInsetsCompat.Type.navigationBars()) ?: true
        val statusStyle = if (controller?.isAppearanceLightStatusBars == true) "LIGHT" else "DARK"
        val navStyle = if (controller?.isAppearanceLightNavigationBars == true) "LIGHT" else "DARK"

        val result = JSONObject()
            .put("statusBar", JSONObject().put("visible", statusVisible).put("style", statusStyle))
            .put("navigationBar", JSONObject().put("visible", navVisible).put("style", navStyle))
        callback.success(result)
    }

    private fun applyColor(opts: JSONObject?, callback: CallbackContext) {
        val raw = opts?.optString("color", null)
        if (raw.isNullOrEmpty()) {
            callback.error("Missing 'color' option (expected '#RRGGBB' or '#AARRGGBB')")
            return
        }
        val parsed: Int = try {
            Color.parseColor(raw)
        } catch (e: IllegalArgumentException) {
            callback.error("Invalid color: $raw")
            return
        }
        // window.statusBarColor / navigationBarColor are no-ops on Android 15
        // (API 35+), which enforces edge-to-edge at the platform level. The
        // call still succeeds so callers don't need to branch on OS version.
        val window = cordova.activity.window
        when (opts.optString("bar", null)) {
            "StatusBar" -> window.statusBarColor = parsed
            "NavigationBar" -> window.navigationBarColor = parsed
            else -> {
                window.statusBarColor = parsed
                window.navigationBarColor = parsed
            }
        }
        callback.success()
    }
}
