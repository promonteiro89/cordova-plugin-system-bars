# OSSystemBarsWrapper

A thin runtime dispatcher that lets a single OutSystems Client Action use the same JavaScript API across both supported runtimes:

- `cordova.plugins.SystemBars` — this Cordova plugin, installed on **O11 / MABS** builds.
- `Capacitor.Plugins.SystemBars` — `@capacitor/core`'s built-in plugin, available on **ODC** builds.

The two underlying APIs are **shape-compatible** (same method names, same option shapes, Promise return values), so this wrapper performs **no argument translation** — it picks whichever is present at the first call, caches it, and forwards subsequent calls directly.

## Usage from OutSystems

The repo ships a prebuilt `dist/` so consumers do **not** need a Node toolchain:

1. Copy `dist/outsystems.js` (the UMD build) into your OutSystems module's plugin scripts folder.
2. Reference it via the `RequireScript` Client Action (typically from your Common.Layout).
3. Call methods on the singleton `Instance`:

```javascript
OSSystemBarsWrapper.Instance.setStyle({ style: 'DARK' });
OSSystemBarsWrapper.Instance.setStyle({ style: 'LIGHT', bar: 'StatusBar' });
OSSystemBarsWrapper.Instance.show();
OSSystemBarsWrapper.Instance.hide({ bar: 'StatusBar' });
OSSystemBarsWrapper.Instance.setAnimation({ animation: 'FADE' });
```

The same call works on both O11 (Cordova) and ODC (Capacitor) builds — the wrapper detects the host runtime once and forwards subsequent calls without overhead.

## API

The wrapper exposes the **exact** four methods of Capacitor 8's [`SystemBars`](https://capacitorjs.com/docs/apis/system-bars) plugin:

| Method | Arguments | Returns |
|---|---|---|
| `setStyle(options)` | `{ style: 'DARK' \| 'LIGHT' \| 'DEFAULT', bar?: 'StatusBar' \| 'NavigationBar' }` | `Promise<void>` |
| `setAnimation(options)` | `{ animation: 'NONE' \| 'FADE' }` | `Promise<void>` |
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

## Building from source

Only needed if you modify the wrapper's TypeScript source:

```sh
cd packages/outsystems-wrapper
npm install
npm run build       # produces dist/outsystems.{js,cjs,mjs} + index.d.ts
```

Commit the regenerated `dist/` along with your source change so downstream consumers stay on a runnable build.

## License

[MIT](../../LICENSE) — same as the parent plugin.
