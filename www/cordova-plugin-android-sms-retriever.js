var exec = require('cordova/exec');

exports.onSmsReceived = function(success, error) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "onSmsReceived", []);
}

exports.getAppHash = function(success, error) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "getAppHash", []);
}
