var exec = require("cordova/exec");
var CheckSecureHardware = function () {
};

CheckSecureHardware.prototype.checkSecureHardware = function (arg0, success, error) {
    console.log('executing checksecurehardware');
    cordova.exec(success, error, "CheckSecureHardware", "checkSecureHardware", null);
};

cordova.addConstructor(function () {

    if (!window.Cordova) {
        window.Cordova = cordova;
    }
    ;

    if (!window.plugins) window.plugins = {};
    window.plugins.CheckSecureHardware = new CheckSecureHardware();
});
