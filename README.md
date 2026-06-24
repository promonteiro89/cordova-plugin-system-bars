# cordova-plugin-system-bars

[![Version](https://img.shields.io/github/v/tag/promonteiro89/cordova-plugin-system-bars?style=flat-square&label=version&color=2188FF)](https://github.com/promonteiro89/cordova-plugin-system-bars/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-2EA043?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-min%20API%2024-3DDC84?style=flat-square&logo=android&logoColor=white)](#requirements)
[![iOS](https://img.shields.io/badge/iOS-13%2B-000000?style=flat-square&logo=apple&logoColor=white)](#requirements)
[![OutSystems](https://img.shields.io/badge/OutSystems-O11%20%2B%20ODC-D52A2D?style=flat-square&logo=outsystems&logoColor=white)](#cross-runtime-usage-o11-cordova--odc-capacitor)

A Cordova port of **Capacitor's SystemBars API** for OutSystems 11 / MABS 12. The plugin exposes the same JavaScript surface as the Capacitor 8 bundled [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) plugin, reimplemented natively in **Kotlin** (Android) and **Swift** (iOS). There is no Capacitor dependency.

## Table of contents

- [Why this exists](#why-this-exists)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
  - [OutSystems 11 (Extensibility Configurations)](#outsystems-11-extensibility-configurations)
    - [Cross-runtime usage (O11 Cordova + ODC Capacitor)](#cross-runtime-usage-o11-cordova--odc-capacitor)
  - [Cordova CLI](#cordova-cli)
  - [Cross-runtime wrapper (`OSSystemBarsWrapper`)](#cross-runtime-wrapper-ossystembarswrapper)
- [Usage](#usage)
- [API reference](#api-reference)
  - [`setStyle(options)`](#setstyleoptions)
  - [`setAnimation(options)`](#setanimationoptions)
  - [`show(options?)`](#showoptions)
  - [`hide(options?)`](#hideoptions)
- [Error handling](#error-handling)
- [Style semantics](#style-semantics)
- [Declarative configuration (MABS 12 preferences)](#declarative-configuration-mabs-12-preferences)
  - [Where to set the preferences](#where-to-set-the-preferences)
- [Platform notes](#platform-notes)
- [Required theme CSS](#required-theme-css)
- [Differences from Capacitor's SystemBars](#differences-from-capacitors-systembars)
- [Contributing](#contributing)
- [License](#license)

## Why this exists

MABS 12 / `cordova-android` 14 already cover the **declarative** side of system-bar configuration through native preferences (`AndroidEdgeToEdge`, `EdgeToEdgeGlyphTheme`, `StatusBarBackgroundColor`, `NavigationBarBackgroundColor`). What they do **not** offer is a clean **runtime** Promise-based API to change the status-bar style, hide bars temporarily, or animate transitions from JavaScript. This plugin fills that gap, exposing the same JavaScript surface as Capacitor's `SystemBars` plugin so the contract is familiar and well-documented. The plugin deliberately does not override the MABS preferences — set them in your Extensibility Configurations and they take effect.

## Features

- **Strict API parity** with Capacitor 8's `SystemBars`: exactly the same four methods (`setStyle`, `setAnimation`, `show`, `hide`) with the same argument shapes and the same semantics. Combined with the bundled [`OSSystemBarsWrapper`](packages/outsystems-wrapper/README.md), a single Client Action can run unchanged on both O11 (Cordova) and ODC (Capacitor).
- Promise-based JavaScript API.
- Runtime style toggling per bar (`StatusBar` / `NavigationBar`) on Android.
- Runtime visibility control with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` on Android so the user can still swipe to reveal hidden bars.
- Configurable show/hide animation on iOS (`NONE` / `FADE`).
- Bakes `UIViewControllerBasedStatusBarAppearance = true` into the iOS `Info.plist` automatically.
- Does not fight MABS 12 platform preferences — declarative startup configuration is left entirely to the platform.

## Requirements

| Component | Minimum |
|-----------|---------|
| OutSystems | 11, MABS 12+ |
| `cordova-android` | 12+ (MABS 12 ships a compatible version) |
| `cordova-ios` | 7.0+ (MABS 12 ships a compatible version) |
| Android `compileSdkVersion` | 35 |
| Android `minSdkVersion` | 24 |
| iOS deployment target | 13.0 |
| Swift | 5 |

## Installation

### OutSystems 11 (Extensibility Configurations)

Paste this into the **Extensibility Configurations** property of your OutSystems mobile app:

```json
{
  "plugin": {
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2"
  }
}
```

Pin the tag (`#1.0.2`) so MABS does not silently pull breaking changes.

#### Cross-runtime usage (O11 Cordova + ODC Capacitor)

This plugin is a **Cordova plugin** and installs only on the **O11 / MABS** build target. On **ODC**, you don't need to install anything — `@capacitor/core` already exposes `Capacitor.Plugins.SystemBars` as a built-in. The two implementations expose the same Capacitor-spec API on different globals (`cordova.plugins.SystemBars` on O11, `Capacitor.Plugins.SystemBars` on ODC), so a single Client Action can run on both runtimes by resolving the access path once.

OutSystems Extensibility Configurations:

```json
{
  "plugin": {
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2"
  },
  "buildConfigurations": {
    "cordova": {
      "source": {
        "npm": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2"
      }
    }
  },
  "metadata": {
    "mabs-min": "12.0.0",
    "name": "SystemBars",
    "version": "1.0.2"
  }
}
```

To make a Client Action that works on both runtimes without branching, either use the [bundled wrapper](#cross-runtime-wrapper-ossystembarswrapper) (recommended), or pick the implementation manually:

```javascript
const SystemBars =
    (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.SystemBars)
    || (window.cordova && window.cordova.plugins && window.cordova.plugins.SystemBars);

SystemBars.setStyle({ style: 'DARK' });
```

> **Advanced**: if you'd rather route all SystemBars calls through *this* Cordova plugin on ODC too (instead of `@capacitor/core`'s built-in), add a `"capacitor": { "source": { "npm": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2" } }` block alongside `cordova` and Capacitor will install this plugin via its [Cordova-plugin compat layer](https://capacitorjs.com/docs/plugins/cordova). The iOS code already handles this case — it extends both `CDVViewController` and (under `#if canImport(Capacitor)`) `CAPBridgeViewController` so the status-bar overrides take effect on either root VC. This is unconventional (the OutSystems convention reserves `buildConfigurations.capacitor` for Capacitor-native packages) but functional.

### Cordova CLI

For local testing in a vanilla Cordova project:

```sh
cordova plugin add https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2
```

### Cross-runtime wrapper (`OSSystemBarsWrapper`)

For OutSystems modules that target both O11 and ODC, the repo also ships a small dispatcher under [`packages/outsystems-wrapper/`](packages/outsystems-wrapper/README.md) that hides the runtime difference behind a single API surface. Copy the prebuilt `packages/outsystems-wrapper/dist/outsystems.js` into your module's scripts folder, `RequireScript` it, then call:

```javascript
OSSystemBarsWrapper.Instance.setStyle({ style: 'DARK' });
OSSystemBarsWrapper.Instance.hide({ bar: 'StatusBar' });
```

The wrapper resolves to `Capacitor.Plugins.SystemBars` on ODC builds and `cordova.plugins.SystemBars` on O11 builds. No translation is done — both APIs are already shape-compatible. See the [wrapper README](packages/outsystems-wrapper/README.md) for full usage.

## Usage

```javascript
// Dark icons on a light background, both bars
cordova.plugins.SystemBars.setStyle({ style: 'LIGHT' });

// Light icons on a dark background, status bar only
cordova.plugins.SystemBars.setStyle({ style: 'DARK', bar: 'StatusBar' });

// Follow the device's system theme
cordova.plugins.SystemBars.setStyle({ style: 'DEFAULT' });

// Hide the status bar (Android: swipe to reveal)
cordova.plugins.SystemBars.hide({ bar: 'StatusBar' });

// Show everything again
cordova.plugins.SystemBars.show();

// Animate subsequent status-bar visibility changes on iOS
cordova.plugins.SystemBars.setAnimation({ animation: 'FADE' });
```

All methods return a `Promise<void>` that rejects with a structured `{ code, message }` error object on failure — see [Error handling](#error-handling).

## API reference

All methods are exposed on **`cordova.plugins.SystemBars`**.

### `setStyle(options)`

Sets icon/text appearance on the system bars.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.style` | `'DARK' \| 'LIGHT' \| 'DEFAULT'` | yes | See [Style semantics](#style-semantics). |
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Apply to a single bar. Omit to apply to both (Android) or to the status bar (iOS). |

### `setAnimation(options)`

Sets the transition used when the status bar appearance changes.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.animation` | `'NONE' \| 'FADE'` | yes | iOS maps to `UIStatusBarAnimation` and uses it for the next `show`/`hide`/`setStyle` transition. Android validates the value to match Capacitor's input contract; the platform composes its own system-bar animation. |

### `show(options?)`

Shows the system bars (or just one).

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Restrict to a single bar. Omit to show all. iOS treats `NavigationBar` as a no-op. |
| `options.animation` | `'NONE' \| 'FADE'` | no | Per-call animation override (iOS only). Applies to this transition only — the value set by `setAnimation` is preserved for subsequent calls. |

### `hide(options?)`

Hides the system bars (or just one). On Android the controller is set to `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, so the user can swipe to reveal the bars temporarily.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Restrict to a single bar. Omit to hide all. iOS treats `NavigationBar` as a no-op. |
| `options.animation` | `'NONE' \| 'FADE'` | no | Per-call animation override (iOS only). Applies to this transition only — the value set by `setAnimation` is preserved for subsequent calls. |

## Error handling

Every method returns `Promise<void>` and rejects with a structured error object — never a bare string — so consumers can branch on a stable `code`:

```javascript
cordova.plugins.SystemBars.setAnimation({ animation: 'SLIDE' })
    .catch(function (err) {
        console.error(err.code);    // "OS-PLUG-SYSBARS-0005"
        console.error(err.message); // "The 'setAnimation' input parameters aren't valid: ..."
    });
```

Out-of-range `style`, `bar`, or `animation` values are rejected with `OS-PLUG-SYSBARS-0005` rather than silently ignored.

| Code | Meaning |
|------|---------|
| `OS-PLUG-SYSBARS-0005` | Invalid input — an out-of-range `style`, `bar`, or `animation` value. |
| `OS-PLUG-SYSBARS-0010` | `WindowInsetsController` unavailable on the device (Android only). |
| `OS-PLUG-SYSBARS-0013` | Operation failed — caught native-exception fallback (Android only). |

## Style semantics

> **Watch out** — `'DARK'` refers to the *background*, not the icons. This is the most common bug when porting code.

| Value | Meaning | Android `appearanceLight*` | iOS `UIStatusBarStyle` |
|-------|---------|----------------------------|------------------------|
| `'DARK'`    | Light icons/text on a dark background | `false` | `.lightContent` |
| `'LIGHT'`   | Dark icons/text on a light background | `true`  | `.darkContent`  |
| `'DEFAULT'` | Follow the system theme               | derived from `uiMode` (night → light icons) | `.default` |

This mapping is taken verbatim from [Capacitor's `SystemBarsStyle`](https://capacitorjs.com/docs/apis/system-bars#systembarsstyle).

## Declarative configuration (MABS 12 preferences)

MABS 12 / `cordova-android` 14 exposes four native Cordova preferences that cover the **startup** appearance of the system bars. They are applied by the platform itself — this plugin does not override them, so they remain available to OutSystems developers via Extensibility Configurations.

| Preference | Type | Description |
|------------|------|-------------|
| `AndroidEdgeToEdge` | `true` / `false` | Whether the app draws edge-to-edge on Android. Enforced as `true` on Android 15 (API 35+) regardless of this setting. |
| `EdgeToEdgeGlyphTheme` | `dark` / `light` | Color of system-bar glyphs (icons and text) when edge-to-edge is enabled. `dark` = dark glyphs (use on light backgrounds); `light` = light glyphs (use on dark backgrounds). Falls back to luminance-based detection if not set or invalid. |
| `StatusBarBackgroundColor` | `#RRGGBB` | Status bar background color. Only takes effect when edge-to-edge is **disabled**. |
| `NavigationBarBackgroundColor` | `#RRGGBB` | Navigation bar background color. Only takes effect when edge-to-edge is **disabled**. |

> ⚠️ **Glyph-color vs background-tone**: MABS's `EdgeToEdgeGlyphTheme` is the *opposite* convention of Capacitor's `style`. MABS `light` (light glyphs) matches our `setStyle({ style: 'DARK' })` (dark background → light icons), and MABS `dark` (dark glyphs) matches `setStyle({ style: 'LIGHT' })`.

### Where to set the preferences

These preferences belong in the **consuming OutSystems mobile app's** Extensibility Configurations — the same property where you added the plugin install URL — and not in this plugin's repository. Add only the ones you care about:

```json
{
  "preferences": {
    "global": [
      { "name": "AndroidEdgeToEdge", "value": "true" },
      { "name": "EdgeToEdgeGlyphTheme", "value": "light" },
      { "name": "StatusBarBackgroundColor", "value": "#000000" },
      { "name": "NavigationBarBackgroundColor", "value": "#000000" }
    ]
  }
}
```

If you also need the plugin install entry in the same JSON, merge the two objects:

```json
{
  "preferences": {
    "global": [
      { "name": "AndroidEdgeToEdge", "value": "true" },
      { "name": "EdgeToEdgeGlyphTheme", "value": "light" }
    ]
  },
  "plugin": {
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.2"
  }
}
```

All four preferences are optional and independent — drop any you don't need. The runtime JavaScript API (`setStyle`, `show`, `hide`, `setAnimation`) takes over after launch and overrides whatever appearance the preferences set.

## Platform notes

### Android

- Implemented in Kotlin. The plugin opts the consuming app into the Kotlin Gradle plugin via the `GradlePluginKotlinEnabled` / `GradlePluginKotlinCodeStyle` Cordova preferences (injected into the Android platform's `config.xml` at install time) and ships the source under `app/src/main/kotlin/...`.
- Edge-to-edge and bar colors are **not** forced by this plugin. They are controlled by the MABS 12 preferences described in [Declarative configuration](#declarative-configuration-mabs-12-preferences); on Android 15 (API 35+) the platform itself enforces edge-to-edge regardless of `AndroidEdgeToEdge`.
- Style changes use `WindowInsetsControllerCompat.isAppearanceLight*Bars`.
- Visibility uses `WindowInsetsControllerCompat.show()` / `hide()` with `WindowInsetsCompat.Type.statusBars()`, `navigationBars()`, or `systemBars()` as appropriate.
- `setStyle({ bar: 'NavigationBar' })` only has a *visible* effect in **3-button navigation mode**, where the back/home/recents icons clearly switch between dark and light. In **gesture navigation mode**, only a thin gesture handle is drawn, and recent Android versions auto-adapt its luminance to the underlying content — the appearance flag is set but the change is barely perceptible. This is platform behavior, not a plugin bug.

### iOS

- Implemented in Swift 5, targeting iOS 13+.
- iOS has no separately controllable navigation bar. Passing `bar: 'NavigationBar'` to `show()` or `hide()` resolves successfully but does nothing.
- Status bar style is driven by overrides on **both** `CDVViewController` and (under `#if canImport(Capacitor)`) `CAPBridgeViewController`, since Capacitor's Cordova-compat layer routes through `CAPBridgeViewController` rather than `CDVViewController`. Same three overrides (`preferredStatusBarStyle`, `prefersStatusBarHidden`, `preferredStatusBarUpdateAnimation`) on each.
- The plugin writes `UIViewControllerBasedStatusBarAppearance = true` into the app's `Info.plist` automatically — no extensibility tweak required.

## Required theme CSS

When edge-to-edge is enabled (default on Android 15, or whenever `AndroidEdgeToEdge=true` is set), your app's chrome must paint behind the system bars and respect safe-area insets:

```css
:root {
    --sat: env(safe-area-inset-top, 0px);
    --sab: env(safe-area-inset-bottom, 0px);
}
.app-header { padding-top: var(--sat); }
.app-footer { padding-bottom: var(--sab); }
```

Make sure your viewport meta tag includes `viewport-fit=cover`, otherwise iOS reports safe-area insets as zero:

```html
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
```

## Differences from Capacitor's SystemBars

- The plugin object lives at `cordova.plugins.SystemBars` on O11 builds. ODC builds use `Capacitor.Plugins.SystemBars` from `@capacitor/core`'s built-in plugin (this Cordova plugin does not install on ODC by default). For Client Actions that run on both, use the [bundled wrapper](packages/outsystems-wrapper/README.md) or `const SystemBars = window.Capacitor?.Plugins?.SystemBars ?? cordova.plugins.SystemBars;`.
- Capacitor's [legacy Status Bar plugin](https://capacitorjs.com/docs/apis/status-bar) (`setBackgroundColor`, `setOverlaysWebView`) is **not** ported — those methods are intentionally not part of Capacitor's `SystemBars` API and are out of scope here too.
- On Android, `setAnimation` validates the value to match Capacitor's input contract but does not change the platform's system-bar animation (the OS composes its own).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow, the PR checklist, and how to file bug reports.

## License

[MIT](LICENSE) © Paulo Ricardo Oliveira Monteiro
