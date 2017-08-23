# CheckSecureHardware

A simple cordova plugin that generates a key on android devices and checks if the key is stored in hardware backed secure storage (Secure Element/ SE or TrustZone chip)

Installation:
  - Check out/ clone this project into the same directory containing the mobile app
  - Build the plugin with npm install
  - Navigate to ultipro-mobile-app project root and run "cordova plugin add ../cordova-plugin-check-secure-hardware"
  - Run "ionic build android && ionic run android"

Usage:

  - Plugin is loaded into window.plugins in the app
  - example usage: copy this block to app.module.js under the run method for debugging

        if (window.plugins) {
            this.checkSecureHardware()
                .then(() => {
                    console.log('Success callback');
                })
                .catch(error => {
                        console.log('Failure callback');
                        console.log(error);
                    );
                }
        }

This plugin works by generating an RSA dummy key, storing it in Android KeyStore, and retrieving the test key.
The retrieved key has a KeyInfo class where we can access isInsideSecureHardware()
to help us determine whether:

1. The Android device has hardware support for storing keys, to prevent key extraction by attackers

Note: Using the "keyInfo.isInsideSecureHardware()" method, it is not possible to ascertain what type of hardware implementation (SE or TrustZone) is used to provide this secure storage, but know that key extraction is effectively prevented

See https://ulti.quip.com/SM4JAY2sXiHi#TNcACAhNowa for a list of devices that were tested for hardware backed secure storage with this plugin