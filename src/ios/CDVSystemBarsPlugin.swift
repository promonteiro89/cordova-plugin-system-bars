import Foundation
import UIKit
import Cordova

@objc(CDVSystemBarsPlugin)
public class CDVSystemBarsPlugin: CDVPlugin {

    static var currentStyle: UIStatusBarStyle = .default
    static var isHidden: Bool = false
    static var currentAnimation: UIStatusBarAnimation = .fade
    static var homeIndicatorHidden: Bool = false

    private static func animationDuration() -> TimeInterval {
        switch currentAnimation {
        case .none: return 0
        case .slide, .fade: return 0.25
        @unknown default: return 0.25
        }
    }

    public override func pluginInitialize() {
        super.pluginInitialize()
        DispatchQueue.main.async {
            self.viewController?.setNeedsStatusBarAppearanceUpdate()
        }
    }

    @objc(setStyle:)
    func setStyle(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let style = (opts["style"] as? String) ?? "DEFAULT"

        let resolved: UIStatusBarStyle
        switch style {
        case "DARK":
            // Dark background → light icons/text
            resolved = .lightContent
        case "LIGHT":
            // Light background → dark icons/text
            if #available(iOS 13.0, *) {
                resolved = .darkContent
            } else {
                resolved = .default
            }
        default:
            resolved = .default
        }

        CDVSystemBarsPlugin.currentStyle = resolved

        DispatchQueue.main.async {
            UIView.animate(withDuration: CDVSystemBarsPlugin.animationDuration()) {
                self.viewController?.setNeedsStatusBarAppearanceUpdate()
            }
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    @objc(hide:)
    func hide(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let bar = opts["bar"] as? String

        // iOS has no separately controllable navigation bar — no-op success.
        if bar == "NavigationBar" {
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }

        CDVSystemBarsPlugin.isHidden = true

        DispatchQueue.main.async {
            UIView.animate(withDuration: CDVSystemBarsPlugin.animationDuration()) {
                self.viewController?.setNeedsStatusBarAppearanceUpdate()
            }
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    @objc(setAnimation:)
    func setAnimation(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let animation = (opts["animation"] as? String) ?? "FADE"

        switch animation {
        case "NONE":
            CDVSystemBarsPlugin.currentAnimation = .none
        case "SLIDE":
            CDVSystemBarsPlugin.currentAnimation = .slide
        case "FADE":
            CDVSystemBarsPlugin.currentAnimation = .fade
        default:
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR,
                                         messageAs: "Invalid animation: \(animation)")
            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }

        let result = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc(show:)
    func show(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let bar = opts["bar"] as? String

        if bar == "NavigationBar" {
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }

        CDVSystemBarsPlugin.isHidden = false

        DispatchQueue.main.async {
            UIView.animate(withDuration: CDVSystemBarsPlugin.animationDuration()) {
                self.viewController?.setNeedsStatusBarAppearanceUpdate()
            }
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    @objc(setHomeIndicatorHidden:)
    func setHomeIndicatorHidden(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let hidden = (opts["hidden"] as? Bool) ?? false
        CDVSystemBarsPlugin.homeIndicatorHidden = hidden
        DispatchQueue.main.async {
            if #available(iOS 11.0, *) {
                self.viewController?.setNeedsUpdateOfHomeIndicatorAutoHidden()
            }
            let result = CDVPluginResult(status: CDVCommandStatus_OK)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    @objc(getInsets:)
    func getInsets(_ command: CDVInvokedUrlCommand) {
        DispatchQueue.main.async {
            let insets: UIEdgeInsets
            if #available(iOS 11.0, *) {
                insets = self.viewController?.view.safeAreaInsets ?? .zero
            } else {
                insets = .zero
            }
            // iOS returns points natively, which equal CSS pixels.
            let dict: [String: Any] = [
                "top": insets.top,
                "bottom": insets.bottom,
                "left": insets.left,
                "right": insets.right
            ]
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: dict)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    }

    @objc(getInfo:)
    func getInfo(_ command: CDVInvokedUrlCommand) {
        let style: String
        switch CDVSystemBarsPlugin.currentStyle {
        case .lightContent:
            style = "DARK"   // light icons → dark background semantics
        default:
            if #available(iOS 13.0, *), CDVSystemBarsPlugin.currentStyle == .darkContent {
                style = "LIGHT"
            } else {
                style = "DEFAULT"
            }
        }

        // iOS has no separately controllable navigation bar; report a stable
        // placeholder so cross-platform callers can rely on the same shape.
        let dict: [String: Any] = [
            "statusBar": [
                "visible": !CDVSystemBarsPlugin.isHidden,
                "style": style
            ],
            "navigationBar": [
                "visible": true,
                "style": "DEFAULT"
            ]
        ]
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: dict)
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc(setColor:)
    func setColor(_ command: CDVInvokedUrlCommand) {
        // iOS has no API to set the status-bar or navigation-bar background
        // separately from the app's own content. Resolve successfully for
        // cross-platform parity; the documented pattern is to paint the
        // header/footer in the app's view and let it extend behind the
        // system bars via safe-area insets.
        let result = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }
}

extension CDVViewController {
    open override var preferredStatusBarStyle: UIStatusBarStyle {
        return CDVSystemBarsPlugin.currentStyle
    }

    open override var prefersStatusBarHidden: Bool {
        return CDVSystemBarsPlugin.isHidden
    }

    open override var preferredStatusBarUpdateAnimation: UIStatusBarAnimation {
        return CDVSystemBarsPlugin.currentAnimation
    }

    open override var prefersHomeIndicatorAutoHidden: Bool {
        return CDVSystemBarsPlugin.homeIndicatorHidden
    }
}
