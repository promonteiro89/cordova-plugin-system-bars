import Foundation
import UIKit
import Cordova

@objc(CDVSystemBarsPlugin)
public class CDVSystemBarsPlugin: CDVPlugin {

    fileprivate static var currentStyle: UIStatusBarStyle = .default
    fileprivate static var isHidden: Bool = false
    fileprivate static var currentAnimation: UIStatusBarAnimation = .fade

    private static let validAnimations: [String: UIStatusBarAnimation] = [
        "NONE": .none,
        "FADE": .fade
    ]

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

        // 'DARK' / 'LIGHT' describe the background, not the icons — matches
        // Capacitor's enum semantics. 'DARK' background → light icons.
        switch style {
        case "DARK":  CDVSystemBarsPlugin.currentStyle = .lightContent
        case "LIGHT": CDVSystemBarsPlugin.currentStyle = .darkContent
        default:      CDVSystemBarsPlugin.currentStyle = .default
        }

        applyAppearanceUpdate(for: command)
    }

    @objc(setAnimation:)
    func setAnimation(_ command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let animation = (opts["animation"] as? String) ?? "FADE"

        guard let value = CDVSystemBarsPlugin.validAnimations[animation] else {
            sendError(command, "Invalid animation: \(animation)")
            return
        }
        CDVSystemBarsPlugin.currentAnimation = value
        sendOK(command)
    }

    @objc(show:)
    func show(_ command: CDVInvokedUrlCommand) {
        setVisibility(hidden: false, for: command)
    }

    @objc(hide:)
    func hide(_ command: CDVInvokedUrlCommand) {
        setVisibility(hidden: true, for: command)
    }

    // MARK: - Helpers

    /// iOS has no separately controllable navigation bar — `bar: "NavigationBar"`
    /// resolves successfully but does not change visibility.
    ///
    /// Supports a per-call `animation` override (Capacitor's
    /// `SystemBarsVisibilityOptions.animation`). The override applies to this
    /// transition only — the value set by `setAnimation` is preserved for
    /// subsequent transitions.
    private func setVisibility(hidden: Bool, for command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        if (opts["bar"] as? String) == "NavigationBar" {
            sendOK(command)
            return
        }

        if let raw = opts["animation"] as? String {
            guard CDVSystemBarsPlugin.validAnimations[raw] != nil else {
                sendError(command, "Invalid animation: \(raw)")
                return
            }
        }
        let overrideAnimation = (opts["animation"] as? String).flatMap { CDVSystemBarsPlugin.validAnimations[$0] }

        CDVSystemBarsPlugin.isHidden = hidden
        applyAppearanceUpdate(for: command, animationOverride: overrideAnimation)
    }

    private func applyAppearanceUpdate(
        for command: CDVInvokedUrlCommand,
        animationOverride: UIStatusBarAnimation? = nil
    ) {
        let savedAnimation = CDVSystemBarsPlugin.currentAnimation
        if let override = animationOverride {
            CDVSystemBarsPlugin.currentAnimation = override
        }
        DispatchQueue.main.async {
            UIView.animate(
                withDuration: CDVSystemBarsPlugin.animationDuration(),
                animations: {
                    self.viewController?.setNeedsStatusBarAppearanceUpdate()
                },
                completion: { _ in
                    if animationOverride != nil {
                        CDVSystemBarsPlugin.currentAnimation = savedAnimation
                    }
                }
            )
            self.sendOK(command)
        }
    }

    private func sendOK(_ command: CDVInvokedUrlCommand) {
        commandDelegate.send(
            CDVPluginResult(status: CDVCommandStatus_OK),
            callbackId: command.callbackId
        )
    }

    private func sendError(_ command: CDVInvokedUrlCommand, _ message: String) {
        commandDelegate.send(
            CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message),
            callbackId: command.callbackId
        )
    }

    private static func animationDuration() -> TimeInterval {
        switch currentAnimation {
        case .none:         return 0
        case .slide, .fade: return 0.25
        @unknown default:   return 0.25
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
