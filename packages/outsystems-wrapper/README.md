# OSSystemBarsWrapper

A thin runtime dispatcher that lets a single OutSystems Client Action use the same JavaScript API against either:

- **this Cordova plugin** (`cordova.plugins.SystemBars`) on **O11 / MABS** builds, or
- **`@capacitor/system-bars`** (`Capacitor.Plugins.SystemBars`) on **ODC** builds.

Mirrors the pattern used by OutSystems' own first-party plugins ([`cordova-outsystems-file`](https://github.com/ionic-team/cordova-outsystems-file), [`cordova-outsystems-geolocation`](https://github.com/ionic-team/cordova-outsystems-geolocation)).

The two underlying APIs are already **shape-compatible** (same method names, same option shapes, Promise return values), so this wrapper performs **no argument translation** — it simply picks the available plugin at the call site and forwards the call.

## Usage from OutSystems

1. `npm install && npm run build` in this directory.
2. Copy the produced `dist/outsystems.js` (the UMD build) into your OutSystems module's plugin scripts folder.
3. Reference it from your Common.Layout (or wherever) via the `RequireScript` Client Action.
4. Call methods on the singleton `Instance`:

```javascript
OSSystemBarsWrapper.Instance.setStyle({ style: 'DARK' });
OSSystemBarsWrapper.Instance.setStyle({ style: 'LIGHT', bar: 'StatusBar' });
OSSystemBarsWrapper.Instance.show();
OSSystemBarsWrapper.Instance.hide({ bar: 'StatusBar' });
OSSystemBarsWrapper.Instance.setAnimation({ animation: 'SLIDE' });
```

The same call works on both O11 (Cordova) and ODC (Capacitor) builds — the wrapper detects the host runtime once and forwards subsequent calls without overhead.

## API

The wrapper exposes the **exact** four methods of Capacitor 8's [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) plugin:

| Method | Arguments | Returns |
|---|---|---|
| `setStyle(options)` | `{ style: 'DARK' \| 'LIGHT' \| 'DEFAULT', bar?: 'StatusBar' \| 'NavigationBar' }` | `Promise<void>` |
| `setAnimation(options)` | `{ animation: 'NONE' \| 'SLIDE' \| 'FADE' }` | `Promise<void>` |
| `show(options?)` | `{ bar?: 'StatusBar' \| 'NavigationBar' }` | `Promise<void>` |
| `hide(options?)` | `{ bar?: 'StatusBar' \| 'NavigationBar' }` | `Promise<void>` |

For style semantics (`'DARK'` = light icons on a dark background, etc.) see the [parent README](../../README.md#style-semantics).

## Detection helpers

If your Client Action needs to branch on the host runtime explicitly:

```javascript
OSSystemBarsWrapper.Instance.isCapacitorPluginDefined();  // boolean
OSSystemBarsWrapper.Instance.isCordovaPluginDefined();    // boolean
```

## Error behaviour

If a method is called and neither plugin is available, the wrapper throws synchronously with:

```
OSSystemBarsWrapper: no SystemBars implementation available —
neither Capacitor.Plugins.SystemBars nor cordova.plugins.SystemBars is defined.
```

This usually means the script ran before Cordova/Capacitor finished initialising. Wrap your first call in `document.addEventListener('deviceready', ...)` (Cordova) or wait for Capacitor's `ready` event.

## License

[MIT](../../LICENSE) — same as the parent plugin.
