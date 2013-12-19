var SmsPlugin = function () {};

SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {    
    return cordova.exec(successCallback, failureCallback, 'SmsPlugin', "SEND_SMS", [phone, message, method]);
};

//Check if the device has a possibility to send and receive SMS
SmsPlugin.prototype.isSupported = function(successCallback,failureCallback) {
    return cordova.exec(successCallback, failureCallback, 'SmsPlugin', 'HAS_SMS_POSSIBILITY', []);
};

//Start receiving sms, and the successCallback function receives one string as parameter formatted such as [phonenumber]>[message]
SmsPlugin.prototype.startReception = function(successCallback,failureCallback) {
    return cordova.exec(successCallback, failureCallback, 'SmsPlugin', 'RECEIVE_SMS', []);
};

//Stop receiving sms
SmsPlugin.prototype.stopReception = function(successCallback,failureCallback) {
    return cordova.exec(successCallback, failureCallback, 'SmsPlugin', 'STOP_RECEIVE_SMS', []);
};

SmsPlugin.install=function(){
    if(!window.plugins){
        window.plugins={};
    }

    window.plugins.sms= new SmsPlugin();
    return window.plugins.sms;
};

cordova.addConstructor(SmsPlugin.install);
