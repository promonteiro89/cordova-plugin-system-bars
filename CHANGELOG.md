# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-06-24

Runtime input validation, plus documentation and release-tooling fixes.

### Changed

- **Docs**: corrected the README's error-handling description — Promise rejections carry a structured `{ code, message }` object (since 1.0.2), not a plain string — and added a dedicated "Error handling" section documenting the `OS-PLUG-SYSBARS-` code table.
- **Docs**: removed the stray `SLIDE` animation from the Features list. Supported values are `NONE` / `FADE`, matching Capacitor's `SystemBarsAnimation` union (the API reference, TypeScript types, and native code already agreed).

### Fixed

- `setStyle` now validates its `style` argument and rejects out-of-range values with `OS-PLUG-SYSBARS-0005` instead of silently falling back to `DEFAULT`, matching the validation `setAnimation` already performed.
- `setStyle`, `show`, and `hide` now validate the optional `bar` argument (`StatusBar` / `NavigationBar`) and reject unknown values with `OS-PLUG-SYSBARS-0005` on both Android and iOS.

### Added

- `scripts/version.mjs` — verifies the version is in sync across `plugin.xml`, `package.json`, `packages/outsystems-wrapper/package.json`, and the README install pins, or sets a new version across all of them in one command. Replaces the manual multi-file edit described in CONTRIBUTING.

## [1.0.2] - 2026-05-20

Structured error payloads, matching the convention used by OutSystems' own first-party Cordova plugins.

### Changed

- Native error responses are now JSON objects of the shape `{ code, message }` instead of plain message strings. The JavaScript bridge surfaces them unchanged, so a Promise rejection now exposes `error.code` and `error.message` directly — no defensive type-checking needed in the consumer.
- New error-code namespace under `OS-PLUG-SYSBARS-`:
  - `0005` — invalid input (e.g. `setAnimation({ animation: 'SLIDE' })`).
  - `0010` — `WindowInsetsController` unavailable on the device (Android-specific).
  - `0013` — generic operation failure (caught exception fallback).

### Added

- **Android**: `OSSystemBarsErrors` object + `CallbackContext.sendError(ErrorInfo)` extension to package structured errors as `PluginResult` JSON objects.
- **iOS**: `OSSystemBarsError` enum + `toDictionary()` helper to package structured errors via `CDVPluginResult(messageAs: [String: String])`.

## [1.0.1] - 2026-05-20

Post-1.0.0 polish, plus the iOS code needed to make this plugin's status-bar overrides actually take effect when installed on ODC via Capacitor's Cordova-compat layer.

### Added

- iOS: parallel `extension CAPBridgeViewController` carrying the same `preferredStatusBarStyle` / `prefersStatusBarHidden` / `preferredStatusBarUpdateAnimation` overrides as the existing `CDVViewController` extension, guarded by `#if canImport(Capacitor)`. Without this, calls like `setStyle({ style: 'DARK' })` resolved successfully from JS on ODC builds but the system bar never repainted, because the live root VC on ODC is `CAPBridgeViewController` (not a `CDVViewController` subclass).
- `CHANGELOG.md` entry split into per-version sections.

### Changed

- README's "Cross-runtime usage" section now leads with the recommended manifest (Cordova target only); the dual-install path is demoted to an "Advanced" note. The `buildConfigurations.capacitor` slot is conventionally for Capacitor-native packages, not Cordova plugins routed through the compat layer.
- README badges reworked to flat-square + brand colors, with a single "OutSystems · O11 + ODC" badge in place of the earlier multi-badge OutSystems row.
- Wrapper README reframed: the `OSSystemBarsWrapper` is now the recommended bridge between `cordova.plugins.SystemBars` (O11) and `Capacitor.Plugins.SystemBars` (ODC) for Client Actions that need to work unchanged on both.

### Removed

- `window.CustomSystemBars` legacy clobber and every doc mention of it. The only public access path on O11 is now `cordova.plugins.SystemBars`.

## [1.0.0] - 2026-05-20

Initial public release.

### Added

- Native Cordova plugin (Kotlin on Android, Swift 5 on iOS) exposing **strict API parity** with Capacitor 8's [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) JavaScript surface, verified against `@capacitor/core@8.3.4`:
  - `setStyle({ style, bar? })`
  - `setAnimation({ animation })` — iOS only, accepts `'NONE' | 'FADE'` to match Capacitor's `SystemBarsAnimation` union.
  - `show({ bar?, animation? })` — per-call `animation` override (iOS only).
  - `hide({ bar?, animation? })` — per-call `animation` override (iOS only).
- Plugin exposed on `cordova.plugins.SystemBars`.
- Android: edge-to-edge–aware style and visibility control via `WindowInsetsControllerCompat`. Hiding a bar uses `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. `DEFAULT` style derives icon appearance from the device's `uiMode` night flag.
- iOS: status-bar style, hidden state, and animation driven by `preferredStatusBarStyle` / `prefersStatusBarHidden` / `preferredStatusBarUpdateAnimation` overrides on `CDVViewController`. `UIViewControllerBasedStatusBarAppearance = true` is baked into the consuming app's `Info.plist` automatically.
- `packages/outsystems-wrapper/` — an optional TypeScript dispatcher that resolves to either `cordova.plugins.SystemBars` (O11 / MABS) or `Capacitor.Plugins.SystemBars` (ODC) at runtime. Prebuilt UMD / ESM / CJS bundles are committed under `dist/` so consumers do not need a Node toolchain.

### Notes

- This plugin deliberately does **not** override the MABS 12 platform preferences (`AndroidEdgeToEdge`, `EdgeToEdgeGlyphTheme`, `StatusBarBackgroundColor`, `NavigationBarBackgroundColor`). Edge-to-edge and bar colors are configured declaratively in the consuming app's Extensibility Configurations.
- On Android, `setAnimation` and the per-call `animation` parameter validate the value to honor Capacitor's contract; the platform composes its own system-bar animation either way.
- Capacitor 8's `SystemBars` is bundled with `@capacitor/core` — no separate npm package. ODC apps already have it; only the O11 build needs this plugin installed.

[1.0.3]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.3
[1.0.2]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.2
[1.0.1]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.1
[1.0.0]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.0
