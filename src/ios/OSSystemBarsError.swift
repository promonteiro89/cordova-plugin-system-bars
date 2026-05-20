import Foundation

enum OSSystemBarsMethod: String {
    case setStyle
    case setAnimation
    case show
    case hide
}

enum OSSystemBarsError: Error {
    case invalidInput(method: OSSystemBarsMethod, reason: String)
    case operationFailed(method: OSSystemBarsMethod, _ error: Error?)

    func toDictionary() -> [String: String] {
        return [
            "code": "OS-PLUG-SYSBARS-\(String(format: "%04d", code))",
            "message": description
        ]
    }
}

private extension OSSystemBarsError {
    var code: Int {
        switch self {
        case .invalidInput:    return 5
        case .operationFailed: return 13
        }
    }

    var description: String {
        switch self {
        case .invalidInput(let method, let reason):
            return "The '\(method.rawValue)' input parameters aren't valid: \(reason)"
        case .operationFailed(let method, let error):
            let errorMessage = error?.localizedDescription ?? "an unknown error"
            return "'\(method.rawValue)' failed with: \(errorMessage)"
        }
    }
}
