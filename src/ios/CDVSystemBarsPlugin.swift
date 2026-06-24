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
    private static let validStyles: Set<String> = ["DARK", "LIGHT", "DEFAULT"]
    private static let validBars: Set<String> = ["StatusBar", "NavigationBar"]

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

        guard CDVSystemBarsPlugin.validStyles.contains(style) else {
            sendError(command, .invalidInput(
                method: .setStyle,
                reason: "'style' must be one of \(Self.validStyleKeys()) (got '\(style)')."
            ))
            return
        }
        if let bar = opts["bar"] as? String,
           !CDVSystemBarsPlugin.validBars.contains(bar) {
            sendError(command, .invalidInput(
                method: .setStyle,
                reason: "'bar' must be one of \(Self.validBarKeys()) (got '\(bar)')."
            ))
            return
        }

        // DARK/LIGHT name the background, not the icons (Capacitor semantics): DARK → light icons.
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
            sendError(command, .invalidInput(
                method: .setAnimation,
                reason: "'animation' must be one of \(Self.validAnimationKeys()) (got '\(animation)')."
            ))
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

    private func setVisibility(hidden: Bool, for command: CDVInvokedUrlCommand) {
        let opts = command.argument(at: 0) as? [String: Any] ?? [:]
        let bar = opts["bar"] as? String

        if let bar = bar, !CDVSystemBarsPlugin.validBars.contains(bar) {
            sendError(command, .invalidInput(
                method: hidden ? .hide : .show,
                reason: "'bar' must be one of \(Self.validBarKeys()) (got '\(bar)')."
            ))
            return
        }

        if let raw = opts["animation"] as? String {
            guard CDVSystemBarsPlugin.validAnimations[raw] != nil else {
                sendError(command, .invalidInput(
                    method: hidden ? .hide : .show,
                    reason: "'animation' must be one of \(Self.validAnimationKeys()) (got '\(raw)')."
                ))
                return
            }
        }

        // No separate navigation bar on iOS — accept the call but do nothing.
        if bar == "NavigationBar" {
            sendOK(command)
            return
        }

        // Per-call override; applies to this transition only, setAnimation is preserved.
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

    private func sendError(_ command: CDVInvokedUrlCommand, _ error: OSSystemBarsError) {
        commandDelegate.send(
            CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.toDictionary()),
            callbackId: command.callbackId
        )
    }

    private static func validAnimationKeys() -> String {
        return validAnimations.keys.sorted().joined(separator: ", ")
    }

    private static func validStyleKeys() -> String {
        return validStyles.sorted().joined(separator: ", ")
    }

    private static func validBarKeys() -> String {
        return validBars.sorted().joined(separator: ", ")
    }

    private static func animationDuration() -> TimeInterval {
        switch currentAnimation {
        case .none: return 0
        default:    return 0.25
        }
    }
}

// MARK: - Status-bar overrides

// Cordova's root VC is a CDVViewController; UIKit reads the status-bar overrides from here.
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

// On ODC the root VC is CAPBridgeViewController (not a CDVViewController), so it needs
// the same overrides. The guard compiles this only when Capacitor is in the build.
#if canImport(Capacitor)
import Capacitor

extension CAPBridgeViewController {
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
#endif
