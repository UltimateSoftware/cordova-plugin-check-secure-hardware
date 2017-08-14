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

  private final String dummyKeyNamespace = "checkSecureHardware";
  private final String storeTarget = "AndroidKeyStore";

  private String keyErrorMessage;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    if (action.equals(dummyKeyNamespace)) {
      boolean hasHardware = (Build.VERSION.SDK_INT >= 23) ? this.checkSecureHardware() : this.checkSecureHardwareLegacy();

      if (hasHardware){
        callbackContext.success();
        return true;
      } else {
        callbackContext.error("Secure hardware not available, " + keyErrorMessage);
        return false;
      }
    }

    callbackContext.error(action +" is not a valid action");
    return false;
  }

  // Requires at least Marshmallow (API 23)
  @TargetApi(Build.VERSION_CODES.M)
  private boolean checkSecureHardware() {
    KeyInfo keyInfo = null;
    boolean keyInfoGeneratedInSecureHardware = false;

    String[] priorityListAlgs = {KeyProperties.KEY_ALGORITHM_RSA, KeyProperties.KEY_ALGORITHM_EC};

    for (int i = 0; i < priorityListAlgs.length && !keyInfoGeneratedInSecureHardware; i++) {
      try {
        KeyPair kp = generateKeyPair(priorityListAlgs[i]);
        KeyFactory factory = KeyFactory.getInstance(kp.getPrivate().getAlgorithm(), storeTarget);
        // Generate dummy key and import it to see if it's in hardware backed Secure Storage
        keyInfo = factory.getKeySpec(kp.getPrivate(), KeyInfo.class);
        keyInfoGeneratedInSecureHardware = keyInfo.isInsideSecureHardware();
      } catch (InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException e) {
        keyErrorMessage = "Failed to generate dummy key with algorithm: " + priorityListAlgs[i];
      }

      if (keyInfo != null) {
        // Delete the dummy key
        try {
          KeyStore store = KeyStore.getInstance(storeTarget);
          store.load(null, null);
          store.deleteEntry(dummyKeyNamespace);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
          keyErrorMessage = "Failed to delete dummy key for algorithm: " + priorityListAlgs[i];
        }
      }
    }

    return keyInfoGeneratedInSecureHardware;
  }

  @TargetApi(Build.VERSION_CODES.M)
  private KeyPair generateKeyPair(String keyAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance(
            keyAlgorithm, storeTarget);

    kpg.initialize(new KeyGenParameterSpec.Builder(
            dummyKeyNamespace,
            KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY)
            .setDigests(KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA512)
            .build());

    return kpg.generateKeyPair();
  }

  // Fallback to legacy / JellyBean implementation (API 18)
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private boolean checkSecureHardwareLegacy(){
    // Deprecated for >= API 23
    return (KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_EC) && KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA));
  }
}