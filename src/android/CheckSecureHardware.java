package com.arthurle.plugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
            return this.checkSecureHardware();
        }
        return false;
    }

    //Requires at least Marshmallow (API 23)
    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkSecureHardware() {
        KeyInfo keyInfo = null;
        System.out.println("check executed");
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

        System.out.println("got KPG, generating key");
        KeyPair kp = kpg.generateKeyPair();
        System.out.println("Key generated: "+kp.getPublic());
        System.out.println("Getting keyfactory: "+kp.getPublic());
        KeyFactory factory = KeyFactory.getInstance(kp.getPrivate().getAlgorithm(), "AndroidKeyStore");

        // find if key is actually in secure hardware
        // InvalidKeySpecException
        keyInfo = factory.getKeySpec(kp.getPrivate(),KeyInfo.class);
        }
        catch (InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (keyInfo != null){
        System.out.println("[DBG] Is auth required? : "+keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware());
        System.out.println("[DBG] Is key in secure hardware? : "+keyInfo.isInsideSecureHardware());
            return keyInfo.isInsideSecureHardware();
        }
        return false;
    }
}
