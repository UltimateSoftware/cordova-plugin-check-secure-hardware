package com.arthurle.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyChain;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;

import javax.security.auth.x500.X500Principal;

public class CheckSecureHardware extends CordovaPlugin {

  private final String DUMMY_KEY_NAMESPACE = "checkSecureHardware";
  private final String STORE_TARGET = "AndroidKeyStore";

  private String keyErrorMessage;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    if (action.equals(DUMMY_KEY_NAMESPACE)) {
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

    try {
      KeyPair kp = generateKeyPairFromSpec(KeyProperties.KEY_ALGORITHM_RSA);
      PrivateKey privateKey = kp.getPrivate();
      KeyFactory factory = KeyFactory.getInstance(privateKey.getAlgorithm(), STORE_TARGET);
      // Generate dummy key and import it to see if it's in hardware backed Secure Storage
      keyInfo = factory.getKeySpec(privateKey, KeyInfo.class);
      keyInfoGeneratedInSecureHardware = keyInfo.isInsideSecureHardware();
    } catch (InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException | IllegalStateException e) {
      keyErrorMessage = "Failed to generate RSA dummy key.";
    }

    if (keyInfo != null) {
      // Delete the dummy key
      try {
        KeyStore store = KeyStore.getInstance(STORE_TARGET);
        store.load(null, null);
        store.deleteEntry(DUMMY_KEY_NAMESPACE);
      } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
        keyErrorMessage = "Failed to delete RSA dummy key.";
      }
    }

    return keyInfoGeneratedInSecureHardware;
  }

  /**
   * Inspired by cordova-secure-storage's KeyPair generation method.
   *
   * It seems that though KeyPairGeneratorSpec is deprecated for API 23 and above,
   * its new counterpart, KeyGenParameterSpec seems to fail on some devices
   * (notably, Samsung devices).
   *
   * For now, we'll use the deprecated KeyPairGeneratorSpec for two reasons:
   *  1) it's exactly how cordova-secure-storage does it
   *  2) it doesn't produce false-negatives (unlike KeyGenParameterSpec)
   *
   * @param keyAlgorithm
   * @return
   * @throws InvalidAlgorithmParameterException
   * @throws NoSuchProviderException
   * @throws NoSuchAlgorithmException
   * @throws IllegalStateException
   */
  @TargetApi(19)
  private KeyPair generateKeyPairFromSpec(String keyAlgorithm) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, IllegalStateException {
    Calendar notBefore = Calendar.getInstance();
    Calendar notAfter = Calendar.getInstance();
    notAfter.add(Calendar.YEAR, 100);

    Context ctx = cordova.getActivity().getApplicationContext();
    String principalString = String.format("CN=%s, OU=%s", DUMMY_KEY_NAMESPACE, ctx);
    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
            .setAlias(DUMMY_KEY_NAMESPACE)
            .setSubject(new X500Principal(principalString))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(notBefore.getTime())
            .setEndDate(notAfter.getTime())
            .setEncryptionRequired()
            .setKeySize(2048)
            .setKeyType(keyAlgorithm)
            .build();
    KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(keyAlgorithm, STORE_TARGET);
    kpGenerator.initialize(spec);
    return kpGenerator.generateKeyPair();
  }

  // Fallback to legacy / JellyBean implementation (API 18)
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private boolean checkSecureHardwareLegacy(){
    // Deprecated for >= API 23
    return (KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_EC) && KeyChain.isBoundKeyAlgorithm(KeyProperties.KEY_ALGORITHM_RSA));
  }
}