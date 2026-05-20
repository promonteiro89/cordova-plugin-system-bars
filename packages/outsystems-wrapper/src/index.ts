/*
 * OSSystemBarsWrapper
 *
 * A thin runtime dispatcher that lets OutSystems Client Actions use a
 * single API surface against either:
 *   - this Cordova plugin (cordova.plugins.SystemBars), on O11 / MABS
 *   - @capacitor/system-bars (Capacitor.Plugins.SystemBars), on ODC
 *
 * The two underlying APIs are already shape-compatible (same method names,
 * same option shapes, Promise return values), so this wrapper does no
 * argument translation — it just picks the available plugin and forwards
 * the call.
 *
 *   OSSystemBarsWrapper.Instance.setStyle({ style: 'DARK' });
 *
 * Mirrors the pattern of OutSystems' own first-party wrappers
 * (cordova-outsystems-file, cordova-outsystems-geolocation).
 */

export type SystemBarsStyle = 'DARK' | 'LIGHT' | 'DEFAULT';
export type SystemBar = 'StatusBar' | 'NavigationBar';
export type SystemBarsAnimation = 'NONE' | 'SLIDE' | 'FADE';

export interface SetStyleOptions {
    style: SystemBarsStyle;
    bar?: SystemBar;
}

export interface SetAnimationOptions {
    animation: SystemBarsAnimation;
}

export interface ShowOrHideOptions {
    bar?: SystemBar;
}

/**
 * Common shape implemented by both this Cordova plugin and
 * \@capacitor/system-bars.
 */
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

    /**
     * @returns `true` if @capacitor/system-bars is available at runtime.
     */
    isCapacitorPluginDefined(): boolean {
        if (typeof window === 'undefined') return false;
        const w = window as any;
        return !!(w.Capacitor?.Plugins?.SystemBars || w.CapacitorPlugins?.SystemBars);
    }

    /**
     * @returns `true` if this Cordova plugin is available at runtime.
     */
    isCordovaPluginDefined(): boolean {
        if (typeof window === 'undefined') return false;
        const w = window as any;
        return !!w.cordova?.plugins?.SystemBars;
    }

    #resolve(): SystemBarsPlugin {
        if (this.#cached) return this.#cached;

        if (typeof window !== 'undefined') {
            const w = window as any;

            // Prefer Capacitor when present (matches ODC builds).
            const capacitor: SystemBarsPlugin | undefined =
                w.Capacitor?.Plugins?.SystemBars ?? w.CapacitorPlugins?.SystemBars;
            if (capacitor) {
                this.#cached = capacitor;
                return capacitor;
            }

            const cordova: SystemBarsPlugin | undefined = w.cordova?.plugins?.SystemBars;
            if (cordova) {
                this.#cached = cordova;
                return cordova;
            }
        }

        throw new Error(
            'OSSystemBarsWrapper: no SystemBars implementation available — ' +
                'neither Capacitor.Plugins.SystemBars nor cordova.plugins.SystemBars is defined.'
        );
    }
}

export const Instance = new OSSystemBars();
