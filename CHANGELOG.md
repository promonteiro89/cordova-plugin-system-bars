# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.0.1]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.1
[1.0.0]: https://github.com/promonteiro89/cordova-plugin-system-bars/releases/tag/1.0.0
