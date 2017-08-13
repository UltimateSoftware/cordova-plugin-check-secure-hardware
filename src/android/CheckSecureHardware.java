package com.arthurle.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.KeyChain;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;


public class CheckSecureHardware extends CordovaPlugin {

  private String message;
    private FingerprintManager mFingerPrintManager;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mFingerPrintManager = cordova.getActivity().getApplicationContext()
                .getSystemService(FingerprintManager.class);

    }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("checkSecureHardware")) {
      boolean hasHardware = (Build.VERSION.SDK_INT >= 23) ? this.checkSecureHardware() : this.checkSecureHardwareLegacy();
      if (hasHardware){
        callbackContext.success();
        return true;
      } else {
        callbackContext.error("[DBG] Secure hardware not available, error was " + message);
        return false;
      }
    }
    callbackContext.error(action +" is not a valid action");
    return false;
  }

  //Requires at least Marshmallow (API 23)
  @TargetApi(Build.VERSION_CODES.M)
  private boolean checkSecureHardware() {
    message += " using non-legacy method";
    KeyInfo keyInfo = null;
      boolean authRequired = false;
    try{
      // Only for devices with fingerprint reader
        if (mFingerPrintManager.isHardwareDetected() && mFingerPrintManager.hasEnrolledFingerprints()){
            authRequired = true
        }
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_RSA,"AndroidKeyStore");
      kpg.initialize(new KeyGenParameterSpec.Builder(
        "checkSecureHardware",
        KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY)
        .setDigests(KeyProperties.DIGEST_SHA256,
          KeyProperties.DIGEST_SHA512)
              .setUserAuthenticationRequired(authRequired)
              .build());
      KeyPair kp = kpg.generateKeyPair();
      KeyFactory factory = KeyFactory.getInstance(kp.getPrivate().getAlgorithm(), "AndroidKeyStore");

      // Find if key is actually in secure hardware
      keyInfo = factory.getKeySpec(kp.getPrivate(),KeyInfo.class);
    }
    catch (Exception e) {
      message = Log.getStackTraceString(e);
    }
    if (keyInfo != null){
      try {
        // Either condition is true when HW keystore is avaiblable
        boolean result = (keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware() || keyInfo.isInsideSecureHardware());
//        System.out.println("[DBG] keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware() " + keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware());
//        System.out.println("[DBG] keyInfo.isInsideSecureHardware() "+keyInfo.isInsideSecureHardware());
//        System.out.println("[DBG] KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_EC) "+KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_EC));
//        System.out.println("[DBG] KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA) "+KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA));
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null, null);
        store.deleteEntry("checkSecureHardware");
        // Delete the key after with the alias used
        return result;
      } catch (Exception e) {
        message = Log.getStackTraceString(e);
      }
    }
    return false;
  }

  //Fallback to legacy/ JellyBean implementation (API 18)
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private boolean checkSecureHardwareLegacy(){
    message += " using legacy method";
    // Deprecated for >= API 23
    return (KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA));
  }
}
