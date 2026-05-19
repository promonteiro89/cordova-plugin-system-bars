var exec = require('cordova/exec');

function call(action, options) {
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, "CustomSystemBars", action, [options || {}]);
    });
}

module.exports = {
    setStyle:     function (options) { return call("setStyle", options); },
    show:         function (options) { return call("show", options); },
    hide:         function (options) { return call("hide", options); },
    setAnimation: function (options) { return call("setAnimation", options); }
};
