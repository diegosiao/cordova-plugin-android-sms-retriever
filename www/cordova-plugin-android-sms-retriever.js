var exec = require('cordova/exec');

exports.onSmsReceived = function(args, success, error) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "onSmsReceived", args);
}

exports.getAppHash = function(args, success, error) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "getAppHash", args);
}
