var exec = require('cordova/exec');

exports.onSmsReceived = function(success, error, notifyWhenStarted) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "onSmsReceived", [ notifyWhenStarted ]);
};

exports.getAppHash = function(success, error) {
    exec(success, error, "CordovaPluginAndroidSmsRetriever", "getAppHash", []);
};
