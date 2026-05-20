"use strict";
var __typeError = (msg) => {
  throw TypeError(msg);
};
var __accessCheck = (obj, member, msg) => member.has(obj) || __typeError("Cannot " + msg);
var __privateGet = (obj, member, getter) => (__accessCheck(obj, member, "read from private field"), getter ? getter.call(obj) : member.get(obj));
var __privateAdd = (obj, member, value) => member.has(obj) ? __typeError("Cannot add the same private member more than once") : member instanceof WeakSet ? member.add(obj) : member.set(obj, value);
var __privateSet = (obj, member, value, setter) => (__accessCheck(obj, member, "write to private field"), setter ? setter.call(obj, value) : member.set(obj, value), value);
var __privateMethod = (obj, member, method) => (__accessCheck(obj, member, "access private method"), method);
var _cached, _OSSystemBars_instances, resolve_fn, _OSSystemBars_static, capacitorPlugin_fn, cordovaPlugin_fn;
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const _OSSystemBars = class _OSSystemBars {
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
  /** Whether \@capacitor/system-bars is available on the current runtime. */
  isCapacitorPluginDefined() {
    var _a;
    return __privateMethod(_a = _OSSystemBars, _OSSystemBars_static, capacitorPlugin_fn).call(_a) !== void 0;
  }
  /** Whether this Cordova plugin is available on the current runtime. */
  isCordovaPluginDefined() {
    var _a;
    return __privateMethod(_a = _OSSystemBars, _OSSystemBars_static, cordovaPlugin_fn).call(_a) !== void 0;
  }
};
_cached = new WeakMap();
_OSSystemBars_instances = new WeakSet();
resolve_fn = function() {
  var _a, _b;
  if (__privateGet(this, _cached)) return __privateGet(this, _cached);
  const found = __privateMethod(_a = _OSSystemBars, _OSSystemBars_static, capacitorPlugin_fn).call(_a) ?? __privateMethod(_b = _OSSystemBars, _OSSystemBars_static, cordovaPlugin_fn).call(_b);
  if (!found) {
    throw new Error(
      "OSSystemBarsWrapper: no SystemBars implementation available — neither Capacitor.Plugins.SystemBars nor cordova.plugins.SystemBars is defined."
    );
  }
  __privateSet(this, _cached, found);
  return found;
};
_OSSystemBars_static = new WeakSet();
capacitorPlugin_fn = function() {
  if (typeof window === "undefined") return void 0;
  const w = window;
  return w.Capacitor?.Plugins?.SystemBars ?? w.CapacitorPlugins?.SystemBars;
};
cordovaPlugin_fn = function() {
  if (typeof window === "undefined") return void 0;
  return window.cordova?.plugins?.SystemBars;
};
__privateAdd(_OSSystemBars, _OSSystemBars_static);
let OSSystemBars = _OSSystemBars;
const Instance = new OSSystemBars();
exports.Instance = Instance;
