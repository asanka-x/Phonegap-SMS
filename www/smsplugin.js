var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var smsplugin = {
send:function (phone, message, method, successCallback, failureCallback) {    
    exec(successCallback, failureCallback, 'SmsPlugin', 'SEND_SMS', [phone, message, method]);
},
//Check if the device has a possibility to send and receive SMS
isSupported:function(successCallback,failureCallback) {
    exec(successCallback, failureCallback, 'SmsPlugin', 'HAS_SMS_POSSIBILITY', []);
},
//Start receiving sms, and the successCallback function receives one string as parameter formatted such as [phonenumber]>[message]
startReception:function(successCallback,failureCallback) {
    exec(successCallback, failureCallback, 'SmsPlugin', 'RECEIVE_SMS', []);
},
//Stop receiving sms
stopReception:function(successCallback,failureCallback) {
    exec(successCallback, failureCallback, 'SmsPlugin', 'STOP_RECEIVE_SMS', []);
}
};

module.exports=smsplugin;
