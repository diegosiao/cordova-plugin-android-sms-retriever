package org.cordova.plugin.android.sms.retriever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaPluginAndroidSmsRetriever extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("onSmsReceived")) {
            this.onSmsReceived(callbackContext);
            return true;
        }

        if (action.equals("getAppHash")) {
            this.getAppHash(callbackContext);
            return true;
        }

        callbackContext.error("Android SMS Retriever Plugin: Unknown action (" + action + ")");
        return false;
    }

    private void onSmsReceived(CallbackContext callbackContext) {
        
        final Context context = this.cordova.getActivity().getApplicationContext();
        SmsRetrieverClient smsRetrieverClient = SmsRetriever.getClient(context);
        smsRetrieverClient.startSmsRetriever();

        // Documentation from Google:
        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent
        // with action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = smsRetrieverClient.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // Registrar broadcast receiver
                IntentFilter intent = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                            Bundle extras = intent.getExtras();
                            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                            switch (status.getStatusCode()) {
                            case CommonStatusCodes.SUCCESS:
                                
                                // Get SMS message contents
                                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                                callbackContext.success(message);
                                break;
                            case CommonStatusCodes.TIMEOUT:
                                
                                callbackContext.error("Waiting for SMS timed out (5 minutes)");
                                break;
                            }
                        }
                    }
                }, intent);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callbackContext.error("Failed to start retriever: " + e.getMessage());
            }
        });

    }

    private void getAppHash(CallbackContext callbackContext) {
        
        try {
            
            for(String hash : getAppSignatures(this.cordova.getActivity().getApplicationContext(), callbackContext)) {
                callbackContext.success("App's Hash: " + hash + " - DO NOT USE THIS UTILITY IN PRODUCTION - FOLLOW THE INSTRUCTIONS HERE: ");
                return;
            }

            callbackContext.error("The app's hash could not be computed. Please refer to Google documentation for an alternative: https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string");
        } catch (Exception e) {

            callbackContext.error("Something bad happened... :( Exception message: " + e.getMessage());
        }
    }

    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;

    private ArrayList<String> getAppSignatures(Context context, CallbackContext callbackContext) {
        ArrayList<String> appCodes = new ArrayList<>();

        try {
            // Get all package signatures for the current package
            String packageName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES).signatures;

            // For each signature create a compatible hash
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString(), callbackContext);
                if (hash != null) {
                    appCodes.add(String.format("%s", hash));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            callbackContext.error("Unable to find package to obtain hash.");
        }

        return appCodes;
    }

    private static String hash(String packageName, String signature, CallbackContext callbackContext) {
        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            byte[] hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            // encode into Base64
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
            
            callbackContext.error("hash:NoSuchAlgorithm");
        }
        return null;
    }

}
