package info.asankan.phonegap.smsplugin;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class SmsPlugin extends CordovaPlugin {
    public enum ActionType{
        SEND_SMS,HAS_SMS_POSSIBILITY,RECEIVE_SMS,STOP_RECEIVE_SMS;
    }
    private SmsSender smsSender;
    private SmsReceiver smsReceiver;

    private CallbackContext callback_receive;

    private boolean isReceiving = false;
    private boolean result=false;

    private PluginResult pluginResult;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        action=action.toUpperCase();

        switch(ActionType.valueOf(action)){
            case SEND_SMS:
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
                    result=true;
                }
                catch (JSONException ex) {
                    callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.JSON_EXCEPTION));
                }
                break;
            case HAS_SMS_POSSIBILITY:
                Activity ctx = this.cordova.getActivity();
                if(ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
                result=true;
                break;
            case RECEIVE_SMS:
                // if already receiving (this case can happen if the startReception is called
                // several times
                if(this.isReceiving) {
                    // close the already opened callback ...
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
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

                pluginResult = new PluginResult(
                        PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
                this.callback_receive = callbackContext;

                result=true;
                break;
            case STOP_RECEIVE_SMS:

                if(this.smsReceiver != null) {
                    smsReceiver.stopReceiving();
                }

                this.isReceiving = false;

                // 1. Stop the receiving context
                pluginResult = new PluginResult(
                        PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(false);
                this.callback_receive.sendPluginResult(pluginResult);

                // 2. Send result for the current context
                pluginResult = new PluginResult(
                        PluginResult.Status.OK);
                callbackContext.sendPluginResult(pluginResult);

                result=true;
                break;
            default:
                result=false;
        }
        return result;
	}
}
