import Foundation
import UIKit
import Cordova

@objc(CDVSystemBarsPlugin)
public class CDVSystemBarsPlugin: CDVPlugin {

    static var currentStyle: UIStatusBarStyle = .default
    static var isHidden: Bool = false
    static var currentAnimation: UIStatusBarAnimation = .fade

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
}
