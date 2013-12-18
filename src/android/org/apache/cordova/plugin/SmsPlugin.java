package org.apache.cordova.plugin;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class SmsPlugin extends CordovaPlugin {
    //for message sending
	public final String ACTION_SEND_SMS = "SendSMS";
    private SmsSender smsSender;

    //for message receiving
    public final String ACTION_HAS_SMS_POSSIBILITY = "HasSMSPossibility";
    public final String ACTION_RECEIVE_SMS = "StartReception";
    public final String ACTION_STOP_RECEIVE_SMS = "StopReception";

    private CallbackContext callback_receive;
    private SmsReceiver smsReceiver = null;
    private boolean isReceiving = false;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION_SEND_SMS)) {
			try {				
				String phoneNumber = args.getString(0);
				String message = args.getString(1);
				String method = args.getString(2);
                smsSender=new SmsSender(this.cordova.getActivity());
				if(method.equalsIgnoreCase("INTENT")){
                    smsSender.invokeSMSIntent(phoneNumber,message);
                    callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.NO_RESULT));
				} else{
                    smsSender.sendSMS(phoneNumber,message);
				}
				
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
				return true;
			}
			catch (JSONException ex) {
				callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.JSON_EXCEPTION));
			}			
		}else if(action.equals(ACTION_HAS_SMS_POSSIBILITY)){
            Activity ctx = this.cordova.getActivity();
            if(ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
            }
            return true;
        }else if (action.equals(ACTION_RECEIVE_SMS)) {

            // if already receiving (this case can happen if the startReception is called
            // several times
            if(this.isReceiving) {
                // close the already opened callback ...
                PluginResult pluginResult = new PluginResult(
                        PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(false);
                this.callback_receive.sendPluginResult(pluginResult);

                // ... before registering a new one to the sms receiver
            }
            this.isReceiving = true;

            if(this.smsReceiver == null) {
                this.smsReceiver = new SmsReceiver();
                IntentFilter fp = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                fp.setPriority(1000);
                // fp.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                this.cordova.getActivity().registerReceiver(this.smsReceiver, fp);
            }

            this.smsReceiver.startReceiving(callbackContext);

            PluginResult pluginResult = new PluginResult(
                    PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            this.callback_receive = callbackContext;

            return true;
        }else if(action.equals(ACTION_STOP_RECEIVE_SMS)) {

            if(this.smsReceiver != null) {
                smsReceiver.stopReceiving();
            }

            this.isReceiving = false;

            // 1. Stop the receiving context
            PluginResult pluginResult = new PluginResult(
                    PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            this.callback_receive.sendPluginResult(pluginResult);

            // 2. Send result for the current context
            pluginResult = new PluginResult(
                    PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);

            return true;
        }
		return false;
	}
}
