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
declare class OSSystemBars implements SystemBarsPlugin {
    #private;
    setStyle(options: SetStyleOptions): Promise<void>;
    setAnimation(options: SetAnimationOptions): Promise<void>;
    show(options?: ShowOrHideOptions): Promise<void>;
    hide(options?: ShowOrHideOptions): Promise<void>;
    /**
     * @returns `true` if @capacitor/system-bars is available at runtime.
     */
    isCapacitorPluginDefined(): boolean;
    /**
     * @returns `true` if this Cordova plugin is available at runtime.
     */
    isCordovaPluginDefined(): boolean;
}
export declare const Instance: OSSystemBars;
export {};
