# cordova-plugin-system-bars

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Cordova](https://img.shields.io/badge/cordova-plugin-2C3E50.svg)](https://cordova.apache.org/)
[![Platforms](https://img.shields.io/badge/platforms-android%20%7C%20ios-blue.svg)](#requirements)
[![MABS](https://img.shields.io/badge/MABS-12%2B-orange.svg)](#requirements)

A Cordova port of **Capacitor's SystemBars API** for OutSystems 11 / MABS 12. The plugin exposes the same JavaScript surface as the Capacitor 8 bundled [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) plugin, reimplemented natively in **Kotlin** (Android) and **Swift** (iOS). There is no Capacitor dependency.

## Table of contents

- [Why this exists](#why-this-exists)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
  - [OutSystems 11 (Extensibility Configurations)](#outsystems-11-extensibility-configurations)
  - [Cordova CLI](#cordova-cli)
- [Usage](#usage)
- [API reference](#api-reference)
  - [`setStyle(options)`](#setstyleoptions)
  - [`show(options?)`](#showoptions)
  - [`hide(options?)`](#hideoptions)
  - [`setAnimation(options)`](#setanimationoptions)
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

- **Strict API parity** with Capacitor 8's `SystemBars`: exactly the same four methods (`setStyle`, `setAnimation`, `show`, `hide`) with the same argument shapes and the same semantics. Client Actions written against this plugin can be moved to an ODC / Capacitor app unchanged.
- Promise-based JavaScript API.
- Runtime style toggling per bar (`StatusBar` / `NavigationBar`) on Android.
- Runtime visibility control with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` on Android so the user can still swipe to reveal hidden bars.
- Configurable show/hide animation on iOS (`NONE` / `SLIDE` / `FADE`).
- Bakes `UIViewControllerBasedStatusBarAppearance = true` into the iOS `Info.plist` automatically.
- Does not fight MABS 12 platform preferences — declarative startup configuration is left entirely to the platform.

## Requirements

| Component | Minimum |
|-----------|---------|
| OutSystems | 11, MABS 12+ |
| `cordova-android` | 12+ (MABS 12 ships a compatible version) |
| `cordova-ios` | 6.2+ |
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
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.0"
  }
}
```

Pin the tag (`#1.0.0`) so MABS does not silently pull breaking changes.

### Cordova CLI

For local testing in a vanilla Cordova project:

```sh
cordova plugin add https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.0
```

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
cordova.plugins.SystemBars.setAnimation({ animation: 'SLIDE' });
```

All methods return a `Promise<void>` that rejects with a string error message on failure.

## API reference

All methods are exposed on **`cordova.plugins.SystemBars`** (the canonical Cordova access path). For backwards compatibility, the same object is also assigned to **`window.CustomSystemBars`** — existing Client Actions that use the legacy global continue to work, but new code should prefer `cordova.plugins.SystemBars`.

### `setStyle(options)`

Sets icon/text appearance on the system bars.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.style` | `'DARK' \| 'LIGHT' \| 'DEFAULT'` | yes | See [Style semantics](#style-semantics). |
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Apply to a single bar. Omit to apply to both (Android) or to the status bar (iOS). |

### `show(options?)`

Shows the system bars (or just one).

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Restrict to a single bar. Omit to show all. iOS treats `NavigationBar` as a no-op. |

### `hide(options?)`

Hides the system bars (or just one). On Android the controller is set to `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, so the user can swipe to reveal the bars temporarily.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.bar` | `'StatusBar' \| 'NavigationBar'` | no | Restrict to a single bar. Omit to hide all. iOS treats `NavigationBar` as a no-op. |

### `setAnimation(options)`

Sets the transition used when the status bar appearance changes.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `options.animation` | `'NONE' \| 'SLIDE' \| 'FADE'` | yes | iOS maps to `UIStatusBarAnimation`. Android records the value for API parity but the platform composes its own animation. |

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
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.0"
  }
}
```

All four preferences are optional and independent — drop any you don't need. The runtime JavaScript API (`setStyle`, `show`, `hide`, `setAnimation`) takes over after launch and overrides whatever appearance the preferences set.

## Platform notes

### Android

- Implemented in Kotlin. The plugin enables the Kotlin Gradle plugin via the `GradlePluginKotlinEnabled` Cordova preference and pins `GradlePluginKotlinVersion` to `1.9.24`.
- Edge-to-edge and bar colors are **not** forced by this plugin. They are controlled by the MABS 12 preferences described in [Declarative configuration](#declarative-configuration-mabs-12-preferences); on Android 15 (API 35+) the platform itself enforces edge-to-edge regardless of `AndroidEdgeToEdge`.
- Style changes use `WindowInsetsControllerCompat.isAppearanceLight*Bars`.
- Visibility uses `WindowInsetsControllerCompat.show()` / `hide()` with `WindowInsetsCompat.Type.statusBars()`, `navigationBars()`, or `systemBars()` as appropriate.
- `setStyle({ bar: 'NavigationBar' })` only has a *visible* effect in **3-button navigation mode**, where the back/home/recents icons clearly switch between dark and light. In **gesture navigation mode**, only a thin gesture handle is drawn, and recent Android versions auto-adapt its luminance to the underlying content — the appearance flag is set but the change is barely perceptible. This is platform behavior, not a plugin bug.

### iOS

- Implemented in Swift 5, targeting iOS 13+.
- iOS has no separately controllable navigation bar. Passing `bar: 'NavigationBar'` to `show()` or `hide()` resolves successfully but does nothing.
- Status bar style is driven by overrides on `CDVViewController` (`preferredStatusBarStyle`, `prefersStatusBarHidden`, `preferredStatusBarUpdateAnimation`).
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

- The plugin object lives at `cordova.plugins.SystemBars` (with `window.CustomSystemBars` as a deprecated alias), not at `Capacitor.Plugins.SystemBars`. Cross-runtime code: `const SystemBars = window.Capacitor?.Plugins?.SystemBars ?? cordova.plugins.SystemBars;`
- Capacitor's [legacy Status Bar plugin](https://capacitorjs.com/docs/apis/status-bar) (`setBackgroundColor`, `setOverlaysWebView`) is **not** ported — those methods are intentionally not part of Capacitor's `SystemBars` API and are out of scope here too.
- On Android, `setAnimation` is recorded for API parity but does not change the platform's system-bar animation.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow, the PR checklist, and how to file bug reports.

## License

[MIT](LICENSE) © Paulo Ricardo Oliveira Monteiro
