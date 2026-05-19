# cordova-plugin-system-bars

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
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
- [Startup preferences (Extensibility Configurations)](#startup-preferences-extensibility-configurations)
- [Platform notes](#platform-notes)
- [Required theme CSS](#required-theme-css)
- [Differences from Capacitor's SystemBars](#differences-from-capacitors-systembars)
- [Contributing](#contributing)
- [License](#license)

## Why this exists

OutSystems 11 / MABS 12 apps run on Cordova and do not have a built-in way to drive edge-to-edge layout, status bar styling, or system-bar visibility through a clean Promise-based API. This plugin fills that gap. The JavaScript surface mirrors Capacitor's `SystemBars` plugin so the contract is familiar and well-documented — but the implementation is pure Cordova, with native code in Kotlin and Swift.

## Features

- Edge-to-edge layout on Android (API 24+), enforced on API 35+.
- Status bar and navigation bar control on Android; status bar control on iOS.
- Independent control of icon/text appearance per bar (`StatusBar` / `NavigationBar`).
- Promise-based JavaScript API, identical to Capacitor's.
- Configurable show/hide animation on iOS (`NONE` / `SLIDE` / `FADE`).
- Bakes `UIViewControllerBasedStatusBarAppearance = true` into the iOS `Info.plist` automatically.

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
    "url": "https://github.com/promonteiro/cordova-plugin-system-bars.git#1.0.0"
  }
}
```

Pin the tag (`#1.0.0`) so MABS does not silently pull breaking changes.

### Cordova CLI

For local testing in a vanilla Cordova project:

```sh
cordova plugin add https://github.com/promonteiro/cordova-plugin-system-bars.git#1.0.0
```

## Usage

```javascript
// Dark icons on a light background, both bars
window.CustomSystemBars.setStyle({ style: 'LIGHT' });

// Light icons on a dark background, status bar only
window.CustomSystemBars.setStyle({ style: 'DARK', bar: 'StatusBar' });

// Follow the device's system theme
window.CustomSystemBars.setStyle({ style: 'DEFAULT' });

// Hide the status bar (Android: swipe to reveal)
window.CustomSystemBars.hide({ bar: 'StatusBar' });

// Show everything again
window.CustomSystemBars.show();

// Animate subsequent status-bar visibility changes on iOS
window.CustomSystemBars.setAnimation({ animation: 'SLIDE' });
```

All methods return a `Promise<void>` that rejects with a string error message on failure.

## API reference

All methods are exposed on `window.CustomSystemBars`.

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

## Startup preferences (Extensibility Configurations)

The plugin reads a single OutSystems-style preference at app launch, matching the standard `cordova-plugin-statusbar` template name so existing OutSystems documentation snippets work without changes:

```json
{
  "preferences": {
    "global": [
      {
        "name": "StatusBarStyle",
        "value": "lightcontent"
      }
    ]
  },
  "plugin": {
    "url": "https://github.com/promonteiro89/cordova-plugin-system-bars.git#1.0.0"
  }
}
```

| Value | Effect | Equivalent JS call |
|-------|--------|--------------------|
| `lightcontent` | Light icons on a dark background | `setStyle({ style: 'DARK' })` |
| `darkcontent`  | Dark icons on a light background | `setStyle({ style: 'LIGHT' })` |
| `default`      | Follow the system theme          | `setStyle({ style: 'DEFAULT' })` |

The preference name is case-insensitive on Android and lowercased automatically on iOS. Calling `setStyle()` from JavaScript at runtime overrides whatever was set declaratively.

### Not supported

The following preferences from the OutSystems status-bar template are **intentionally not implemented** because they conflict with the edge-to-edge model this plugin enforces:

- `StatusBarBackgroundColor` — a solid background color contradicts the transparent, content-behind-bars layout the plugin sets up.
- `StatusBarOverlaysWebView` — the WebView already extends behind the system bars unconditionally; there is no opt-out.

Unknown preference values are silently ignored so a misconfigured template never breaks app launch.

## Platform notes

### Android

- Implemented in Kotlin. The plugin enables the Kotlin Gradle plugin via the `GradlePluginKotlinEnabled` Cordova preference and pins `GradlePluginKotlinVersion` to `1.9.24`.
- Edge-to-edge is enabled in `initialize()` via `WindowCompat.setDecorFitsSystemWindows(window, false)`.
- On API < 35, `statusBarColor` and `navigationBarColor` are forced to transparent. On API 35+ those setters are no-ops because edge-to-edge is enforced by the platform.
- Visibility uses `WindowInsetsControllerCompat.show()` / `hide()` with `WindowInsetsCompat.Type.statusBars()`, `navigationBars()`, or `systemBars()` as appropriate.

### iOS

- Implemented in Swift 5, targeting iOS 13+.
- iOS has no separately controllable navigation bar. Passing `bar: 'NavigationBar'` to `show()` or `hide()` resolves successfully but does nothing.
- Status bar style is driven by overrides on `CDVViewController` (`preferredStatusBarStyle`, `prefersStatusBarHidden`, `preferredStatusBarUpdateAnimation`).
- The plugin writes `UIViewControllerBasedStatusBarAppearance = true` into the app's `Info.plist` automatically — no extensibility tweak required.

## Required theme CSS

Because edge-to-edge is forced, your app's chrome must paint behind the system bars and respect safe-area insets:

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

- The global is `window.CustomSystemBars`, not `Capacitor.Plugins.SystemBars`.
- Capacitor's [legacy Status Bar plugin](https://capacitorjs.com/docs/apis/status-bar) (`setBackgroundColor`, `setOverlaysWebView`) is **not** ported — those methods are intentionally not part of Capacitor's `SystemBars` API and are out of scope here too.
- On Android, `setAnimation` is recorded for API parity but does not change the platform's system-bar animation.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow, the PR checklist, and how to file bug reports.

## License

[MIT](LICENSE) © Paulo Ricardo Oliveira Monteiro
