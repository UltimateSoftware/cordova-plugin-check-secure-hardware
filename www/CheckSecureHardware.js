var CheckSecureHardware = function(){};

CheckSecureHardware.prototype.checkSecureHardware = function(arg0, success, error) {
    cordova.exec(success, error, "CheckSecureHardware", "checkSecureHardware", []);
};

cordova.addConstructor(function() {

    if (!window.Cordova) {
        window.Cordova = cordova;
    };

    if(!window.plugins) window.plugins = {};
    window.plugins.CheckSecureHardware = new CheckSecureHardware();
});
