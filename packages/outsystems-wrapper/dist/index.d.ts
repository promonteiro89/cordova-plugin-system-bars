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
/** Common shape implemented by both Cordova and Capacitor SystemBars plugins. */
export interface SystemBarsPlugin {
    setStyle(options: SetStyleOptions): Promise<void>;
    setAnimation(options: SetAnimationOptions): Promise<void>;
    show(options?: ShowOrHideOptions): Promise<void>;
    hide(options?: ShowOrHideOptions): Promise<void>;
}
declare class OSSystemBars implements SystemBarsPlugin {
    #private;
    setStyle(options: SetStyleOptions): Promise<void>;
    setAnimation(options: SetAnimationOptions): Promise<void>;
    show(options?: ShowOrHideOptions): Promise<void>;
    hide(options?: ShowOrHideOptions): Promise<void>;
    /** Whether \@capacitor/system-bars is available on the current runtime. */
    isCapacitorPluginDefined(): boolean;
    /** Whether this Cordova plugin is available on the current runtime. */
    isCordovaPluginDefined(): boolean;
}
export declare const Instance: OSSystemBars;
export {};
