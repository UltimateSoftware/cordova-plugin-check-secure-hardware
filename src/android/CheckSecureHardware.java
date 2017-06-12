package com.arthurle.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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

public class CheckSecureHardware extends CordovaPlugin {

  private String messsage;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("checkSecureHardware")) {
      boolean hasHardware = (Build.VERSION.SDK_INT >= 23) ? this.checkSecureHardware() : this.checkSecureHardwareLegacy();
      if (hasHardware){
        callbackContext.success();
        return true;
      } else {
        callbackContext.error("Secure hardware not available, " + messsage);
        return false;
      }
    }
    callbackContext.error(action +" is not a valid action");
    return false;
  }

  //Requires at least Marshmallow (API 23)
  @TargetApi(Build.VERSION_CODES.M)
  private boolean checkSecureHardware() {
    KeyInfo keyInfo = null;
    // Generate dummy EC key
    // NoSuchAlgorithmException if EC unsupported
    try{
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,"AndroidKeyStore");
      kpg.initialize(new KeyGenParameterSpec.Builder(
        "checkSecureHardware",
        KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY)
        .setDigests(KeyProperties.DIGEST_SHA256,
          KeyProperties.DIGEST_SHA512)
        .build());

      KeyPair kp = kpg.generateKeyPair();
      KeyFactory factory = KeyFactory.getInstance(kp.getPrivate().getAlgorithm(), "AndroidKeyStore");

      // Find if key is actually in secure hardware
      // InvalidKeySpecException
      keyInfo = factory.getKeySpec(kp.getPrivate(),KeyInfo.class);
    }
    catch (InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException e) {
      messsage = "Failed to generate dummy key or get dummy key info";
    }
    if (keyInfo != null){
      try {
        // Either condition is true when HW keystore is avaiblable
        boolean result = (keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware() || keyInfo.isInsideSecureHardware());
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null, null);
        store.deleteEntry("checkSecureHardware");
        // Delete the key after with the alias used
        return result;
      } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |IOException e) {
        messsage = "Failed to delete dummy key";
      }
    }
    return false;
  }

  //Fallback to legacy/ JellyBean implementation (API 18)
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private boolean checkSecureHardwareLegacy(){
    // Deprecated for >= API 23
    return (KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_EC) && KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA));
  }
}
