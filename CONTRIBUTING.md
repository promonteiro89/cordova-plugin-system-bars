# Contributing

Thanks for your interest in improving `cordova-plugin-system-bars`. This plugin gives OutSystems 11 / MABS 12 apps a Cordova-native implementation of Capacitor's `SystemBars` API. Contributions that move the API closer to that contract, fix correctness issues, or improve the OutSystems integration are very welcome.

## Ground rules

- Keep the JavaScript surface identical to [Capacitor's `SystemBars` API](https://capacitorjs.com/docs/apis/system-bars). If you need to deviate, open an issue first so we can discuss.
- Do not introduce a Capacitor build dependency. This plugin is intentionally Cordova-only.
- Android is Kotlin; iOS is Swift 5. Please do not mix Java back in.
- Target environment is fixed: MABS 12, `compileSdkVersion` 35+, `minSdkVersion` 24, iOS 13+.

## Reporting issues

When filing a bug, please include:

1. The platform (Android / iOS) and OS version.
2. The MABS version and `cordova-android` / `cordova-ios` versions.
3. A minimal JavaScript snippet that reproduces the problem.
4. What you expected to happen and what actually happened (a screenshot helps for visual regressions like icon contrast).

## Development workflow

1. Fork the repo and create a feature branch from `main`:
   ```sh
   git checkout -b feat/short-description
   ```
2. Make your changes. Keep commits focused — one logical change per commit.
3. Test against a real OutSystems 11 / MABS 12 app. Cordova plugins are notoriously hard to test in isolation, so before opening a PR please verify:
   - The plugin installs cleanly via Extensibility Configurations.
   - Each method you touched works on both Android (test on API 24, API 30, and API 35+ if you can) and iOS (iOS 13 and the latest).
   - Edge-to-edge rendering is not broken — the app content should still draw behind the system bars.
4. Update the README if the public API or behavior changes.
5. Bump the version in `plugin.xml` and `package.json` following [SemVer](https://semver.org/) if your change is user-visible.

## Pull request checklist

- [ ] The JS surface still matches Capacitor's `SystemBars` API (or the deviation is documented and discussed in an issue).
- [ ] `plugin.xml` is valid XML (`xmllint --noout plugin.xml`).
- [ ] `package.json` is valid JSON.
- [ ] Style enum semantics (`DARK` = light icons, `LIGHT` = dark icons) are preserved.
- [ ] README is updated if behavior changed.
- [ ] You have tested on at least one Android API level and one iOS version.

## Style

- Kotlin: idiomatic Kotlin, no Java interop hacks unless required by the Cordova plugin contract.
- Swift: Swift 5, iOS 13+ APIs only.
- JavaScript: no transpilation, no dependencies. Plain ES5 (`var`, `function`) so the file loads in WebViews without a build step.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
