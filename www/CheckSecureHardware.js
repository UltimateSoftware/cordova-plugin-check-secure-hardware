var exec = require('cordova/exec');

CheckSecureHardware.prototype.checkSecureHardware = function(arg0, success, error) {
    cordova.exec(success, error, "cordova-plugin-check-secure-hardware", "checkSecureHardware", [arg0]);
};

cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };

    if(!window.plugins) window.plugins = {};
    window.plugins.CheckSecureHardware = new CheckSecureHardware();
});
