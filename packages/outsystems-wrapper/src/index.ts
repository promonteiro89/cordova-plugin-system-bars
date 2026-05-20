/**
 * OSSystemBarsWrapper
 *
 * Runtime dispatcher that lets a single OutSystems Client Action use the
 * same JavaScript API against:
 *   - cordova.plugins.SystemBars   (this Cordova plugin, on O11 / MABS)
 *   - Capacitor.Plugins.SystemBars (\@capacitor/system-bars, on ODC)
 *
 * Both implementations are already shape-compatible (same method names,
 * same option shapes, Promise return values) so the wrapper performs no
 * argument translation — it picks whichever is present at the first call
 * site, caches it, and forwards subsequent calls directly.
 *
 *   OSSystemBarsWrapper.Instance.setStyle({ style: 'DARK' });
 */

export type SystemBarsStyle = 'DARK' | 'LIGHT' | 'DEFAULT';
export type SystemBar = 'StatusBar' | 'NavigationBar';
export type SystemBarsAnimation = 'NONE' | 'FADE';

export interface SetStyleOptions {
    style: SystemBarsStyle;
    bar?: SystemBar;
}

export interface SetAnimationOptions {
    animation: SystemBarsAnimation;
}

export interface ShowOrHideOptions {
    bar?: SystemBar;
    /** Per-call animation override (iOS only). */
    animation?: SystemBarsAnimation;
}

/** Common shape implemented by both Cordova and Capacitor SystemBars plugins. */
export interface SystemBarsPlugin {
    setStyle(options: SetStyleOptions): Promise<void>;
    setAnimation(options: SetAnimationOptions): Promise<void>;
    show(options?: ShowOrHideOptions): Promise<void>;
    hide(options?: ShowOrHideOptions): Promise<void>;
}

class OSSystemBars implements SystemBarsPlugin {
    #cached: SystemBarsPlugin | null = null;

    setStyle(options: SetStyleOptions): Promise<void> {
        return this.#resolve().setStyle(options);
    }

    setAnimation(options: SetAnimationOptions): Promise<void> {
        return this.#resolve().setAnimation(options);
    }

    show(options?: ShowOrHideOptions): Promise<void> {
        return this.#resolve().show(options);
    }

    hide(options?: ShowOrHideOptions): Promise<void> {
        return this.#resolve().hide(options);
    }

    /** Whether \@capacitor/system-bars is available on the current runtime. */
    isCapacitorPluginDefined(): boolean {
        return OSSystemBars.#capacitorPlugin() !== undefined;
    }

    /** Whether this Cordova plugin is available on the current runtime. */
    isCordovaPluginDefined(): boolean {
        return OSSystemBars.#cordovaPlugin() !== undefined;
    }

    #resolve(): SystemBarsPlugin {
        if (this.#cached) return this.#cached;

        // Prefer Capacitor when present so ODC builds use the native API.
        const found = OSSystemBars.#capacitorPlugin() ?? OSSystemBars.#cordovaPlugin();
        if (!found) {
            throw new Error(
                'OSSystemBarsWrapper: no SystemBars implementation available — ' +
                    'neither Capacitor.Plugins.SystemBars nor cordova.plugins.SystemBars is defined.'
            );
        }
        this.#cached = found;
        return found;
    }

    static #capacitorPlugin(): SystemBarsPlugin | undefined {
        if (typeof window === 'undefined') return undefined;
        const w = window as any;
        return w.Capacitor?.Plugins?.SystemBars ?? w.CapacitorPlugins?.SystemBars;
    }

    static #cordovaPlugin(): SystemBarsPlugin | undefined {
        if (typeof window === 'undefined') return undefined;
        return (window as any).cordova?.plugins?.SystemBars;
    }
}

export const Instance = new OSSystemBars();
