package cordova-plugin-check-secure-hardware;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class cordova-plugin-check-secure-hardware extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("checkSecureHardware")) {
            return this.isSecureHardwareAvailable();
        }
        return false;
    }

    private boolean isSecureHardwareAvailable() {
        keyInfo keyinfo = null;
        System.out.println("check executed");
        // generate dummy EC key
        // NoSuchAlgorithmException if EC unsupported
        try{
        KeyPairGenerator kpg=KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,"AndroidKeyStore");
        kpg.initialize(new KeyGenParameterSpec.Builder(
        "checkSecureHardware",
        KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_VERIFY)
        .setDigests(KeyProperties.DIGEST_SHA256,
        KeyProperties.DIGEST_SHA512)
        .build());
        // find if key is actually in secure hardware
        // InvalidKeySpecException
        KeyPair kp=kpg.generateKeyPair();
        keyInfo=factory.getKeySpec(kp.getPrivate(),KeyInfo.class);
        }
        catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (keyInfo != null){
            return keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware();
        }
        return false;
    }
}
