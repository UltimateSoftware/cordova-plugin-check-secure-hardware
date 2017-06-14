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


        if (window.plugins){
          const secureHardwareExists = window.plugins.CheckSecureHardware.checkSecureHardware({},
            () => {
              console.log("Success check hardware");
            },
            () => {
              console.log("Failed check hardware");
            });
          console.log(`[DBG] ==== ${secureHardwareExists}`);
        }

   - Output (using google pixel): 06-06 13:37:49.277 18309 18444 I System.out: Is key in secure hardware? : true

Note: Using the "keyInfo.isInsideSecureHardware()" method, it is not possible to ascertain what type of hardware implementation (SE or TrustZone) is used to provide this secure storage, but know that key extraction is effectively prevented

Tested with:

Google Pixel 32gb   (7.1.2): Has HW secure storage

Oneplus One         (7.1.1): Does not have HW secure storage

Google Nexus 5      (6.0.1): Does not have HW secure storage

Samsung Galaxy Nexus (4.3.3): Does not have HW secure storage

Asus Zenfone 3      (6.0.1): Has HW secure storage

06-06 15:03:05.417 22188 22271 I System.out: [DBG] Is auth required? : false
06-06 15:03:05.417 22188 22271 I System.out: [DBG] Is key in secure hardware? : true
