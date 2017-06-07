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
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;

public class CheckSecureHardware extends CordovaPlugin {

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("checkSecureHardware")) {
      boolean hasHardware = this.checkSecureHardware();
      System.out.println("[DBG] check result: " + hasHardware);
      if (hasHardware){
        callbackContext.success();
        return true;
      }
    }
    return false;
  }

  //Requires at least Marshmallow (API 23)
  @TargetApi(Build.VERSION_CODES.M)
  private boolean checkSecureHardware() {
    KeyInfo keyInfo = null;
    System.out.println("[DBG] check executed");
    // generate dummy EC key
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

      System.out.println("[DBG] got KPG, generating key");
      KeyPair kp = kpg.generateKeyPair();
      System.out.println("[DBG] Key generated: "+kp.getPublic());
      System.out.println("[DBG] Getting keyfactory: "+kp.getPublic());
      KeyFactory factory = KeyFactory.getInstance(kp.getPrivate().getAlgorithm(), "AndroidKeyStore");

      // find if key is actually in secure hardware
      // InvalidKeySpecException
      keyInfo = factory.getKeySpec(kp.getPrivate(),KeyInfo.class);
    }
    catch (InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    if (keyInfo != null){
      try {
        System.out.println("[DBG] Is auth required? : " + keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware());
        System.out.println("[DBG] Is key in secure hardware?  ABSBBSBDSA: " + keyInfo.isInsideSecureHardware());
        boolean result = (keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware() || keyInfo.isInsideSecureHardware());
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null, null);
        store.deleteEntry("checkSecureHardware");
        System.out.println("[DBG] Result TEST: "+result);
        // delete the key after with the alias used
        return result;
      } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |IOException e) {
        System.out.println("[DBG] KS exception: "+e.toString());
        e.printStackTrace();
      }
    }
    return false;
  }
}
