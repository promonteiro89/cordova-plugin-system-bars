var __typeError = (msg) => {
  throw TypeError(msg);
};
var __accessCheck = (obj, member, msg) => member.has(obj) || __typeError("Cannot " + msg);
var __privateGet = (obj, member, getter) => (__accessCheck(obj, member, "read from private field"), getter ? getter.call(obj) : member.get(obj));
var __privateAdd = (obj, member, value) => member.has(obj) ? __typeError("Cannot add the same private member more than once") : member instanceof WeakSet ? member.add(obj) : member.set(obj, value);
var __privateSet = (obj, member, value, setter) => (__accessCheck(obj, member, "write to private field"), setter ? setter.call(obj, value) : member.set(obj, value), value);
var __privateMethod = (obj, member, method) => (__accessCheck(obj, member, "access private method"), method);
var _cached, _OSSystemBars_instances, resolve_fn;
class OSSystemBars {
  constructor() {
    __privateAdd(this, _OSSystemBars_instances);
    __privateAdd(this, _cached, null);
  }
  setStyle(options) {
    return __privateMethod(this, _OSSystemBars_instances, resolve_fn).call(this).setStyle(options);
  }
  setAnimation(options) {
    return __privateMethod(this, _OSSystemBars_instances, resolve_fn).call(this).setAnimation(options);
  }
  show(options) {
    return __privateMethod(this, _OSSystemBars_instances, resolve_fn).call(this).show(options);
  }
  hide(options) {
    return __privateMethod(this, _OSSystemBars_instances, resolve_fn).call(this).hide(options);
  }
  /**
   * @returns `true` if @capacitor/system-bars is available at runtime.
   */
  isCapacitorPluginDefined() {
    if (typeof window === "undefined") return false;
    const w = window;
    return !!(w.Capacitor?.Plugins?.SystemBars || w.CapacitorPlugins?.SystemBars);
  }
  /**
   * @returns `true` if this Cordova plugin is available at runtime.
   */
  isCordovaPluginDefined() {
    if (typeof window === "undefined") return false;
    const w = window;
    return !!w.cordova?.plugins?.SystemBars;
  }
}
_cached = new WeakMap();
_OSSystemBars_instances = new WeakSet();
resolve_fn = function() {
  if (__privateGet(this, _cached)) return __privateGet(this, _cached);
  if (typeof window !== "undefined") {
    const w = window;
    const capacitor = w.Capacitor?.Plugins?.SystemBars ?? w.CapacitorPlugins?.SystemBars;
    if (capacitor) {
      __privateSet(this, _cached, capacitor);
      return capacitor;
    }
    const cordova = w.cordova?.plugins?.SystemBars;
    if (cordova) {
      __privateSet(this, _cached, cordova);
      return cordova;
    }
  }
  throw new Error(
    "OSSystemBarsWrapper: no SystemBars implementation available — neither Capacitor.Plugins.SystemBars nor cordova.plugins.SystemBars is defined."
  );
};
const Instance = new OSSystemBars();
export {
  Instance
};
