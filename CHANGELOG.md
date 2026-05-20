# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-05-20

Initial public release.

### Added

- Native Cordova plugin (Kotlin on Android, Swift 5 on iOS) exposing **strict API parity** with Capacitor 8's [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) JavaScript surface, verified against `@capacitor/core@8.3.4`:
  - `setStyle({ style, bar? })`
  - `setAnimation({ animation })` — iOS only, accepts `'NONE' | 'FADE'` to match Capacitor's `SystemBarsAnimation` union.
  - `show({ bar?, animation? })` — per-call `animation` override (iOS only).
  - `hide({ bar?, animation? })` — per-call `animation` override (iOS only).
- Plugin exposed on `cordova.plugins.SystemBars` (primary) and `window.CustomSystemBars` (backwards-compatible alias).
- Android: edge-to-edge–aware style and visibility control via `WindowInsetsControllerCompat`. Hiding a bar uses `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. `DEFAULT` style derives icon appearance from the device's `uiMode` night flag.
- iOS: status-bar style, hidden state, and animation driven by `preferredStatusBarStyle` / `prefersStatusBarHidden` / `preferredStatusBarUpdateAnimation` overrides on **both** `CDVViewController` (for O11 / MABS Cordova builds) and `CAPBridgeViewController` (for ODC builds via Capacitor's Cordova-compat layer; guarded by `#if canImport(Capacitor)`). `UIViewControllerBasedStatusBarAppearance = true` is baked into the consuming app's `Info.plist` automatically.
- `packages/outsystems-wrapper/` — a TypeScript dispatcher that resolves to either `cordova.plugins.SystemBars` (O11 / MABS) or `Capacitor.Plugins.SystemBars` (ODC) at runtime, mirroring the consumption pattern of OutSystems' own `cordova-outsystems-file` and `cordova-outsystems-geolocation` plugins. Prebuilt UMD / ESM / CJS bundles are committed under `dist/` so consumers do not need a Node toolchain.
- README recipes for both single-runtime (Cordova-only) and dual-runtime (Cordova + Capacitor) Extensibility Configurations, including a 3-line runtime-detection helper for cross-runtime Client Actions.

### Notes

- This plugin deliberately does **not** override the MABS 12 platform preferences (`AndroidEdgeToEdge`, `EdgeToEdgeGlyphTheme`, `StatusBarBackgroundColor`, `NavigationBarBackgroundColor`). Edge-to-edge and bar colors are configured declaratively in the consuming app's Extensibility Configurations.
- On Android, `setAnimation` and the per-call `animation` parameter validate the value to honor Capacitor's contract; the platform composes its own system-bar animation either way.
- Capacitor 8's `SystemBars` is bundled with `@capacitor/core` — no separate npm package. ODC apps already have it; only the O11 build needs this plugin installed.

[1.0.0]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.0
