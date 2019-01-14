# cordova-plugin-android-sms-retriever

This plugin uses the Android [**SMS Retriever API**](https://developers.google.com/identity/sms-retriever/overview) to intercept text messages sent to the phone.

## Why is that?

On **January/2019** Google started warning developers about critical changes in SMS and CALL LOGS access policy. Android used to allow apps to read or intercept SMS indiscriminately since they had the permissions: SMS_READ and/or CALL_LOG. That is an **obvious security issue** since several financial operations rely on tokens or OTP (One Time Passwords) sent through SMS to their users. [More info...](https://www.zdnet.com/article/google-restricts-which-android-apps-can-request-call-log-and-sms-permissions/)

Regarding to SMS, Google presents as an alternative the [**SMS Retriever API**](https://developers.google.com/identity/sms-retriever/overview) used by this plugin.

## Installation

    cordova plugin add cordova-plugin-android-sms-retriever
---

# API Reference <a name="reference"></a>

* [cordova.plugins.AndroidSmsRetriever](#module_androidSmsRetriever)
    * [.getAppHash(successCallback, errorCallback)](#module_androidSmsRetriever.getAppHash)
    * [.onSmsReceived(successCallback, errorCallback, notifyWhenStarted)](#module_androidSmsRetriever.onSmsReceived)

<a name="module_androidSmsRetriever"></a>

## AndroidSmsRetriever
<a name="module_androidSmsRetriever.getAppHash"></a>

### .getAppHash(successCallback, errorCallback)
Since computing the 11 characters portion of your app's hash required by SMS retriever API to be in the text message body can be a tedious workload, this plugin provides this function as an utility. 

**IMPORTANT:** Since you get your app's hash, you should remove the call to this function. Also, remember this hash uses the certicate used to sign the APK so consider this information if you face issues to intercept SMS.

    // Remove this code after getting your app's hash
    // The hash might be different when building a release version
    if (device.platform === 'Android') {

        cordova.plugins.AndroidSmsRetriever
            .getAppHash(
                
                function successCallback(hash) {
                    alert(hash);
                },

                function errorCallback(e) {
                    console.error(e);
                }

            );
    }

If you are facing difficulties to generate your app's hash using this function refer to documentation provided by Google [here](https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string). 

<a name="module_androidSmsRetriever.onSmsReceived"></a>
### .onSmsReceived(successCallback, errorCallback, notifyWhenStarted)

**successCallback** The function to be called when a SMS arrives. 

**errorCallback** The error callback will be called if something goes wrong or if no SMS arrives within **5 minutes**.

**notifyWhenStarted** An optional boolean that determines if the successCallback should also be called when the SMS retriever starts. The value returned in the message parameter of the successCallback will be the constant 'SMS_RETRIEVER_SETUP'.

Useful when you want to make sure the native SMSretriever listener is up and running before requesting your server to send the text message.


    if (device.platform === 'Android') {

        cordova.plugins.AndroidSmsRetriever
            .onSmsReceived(
                
                function successCallback(message) {

                    if(message === 'SMS_RETRIEVER_SETUP') {
                        // Here you request server to send the SMS
                        return;
                    }

                    alert(message);
                },

                function errorCallback(e) {
                    console.error(e);
                },

                true //notifyWhenStarted
            );
    }
    
**IMPORTANT:** The body of your SMS needs to comply the Android SMS Retriever API requirements described [here](https://developers.google.com/identity/sms-retriever/verify#1_construct_a_verification_message). Basically you need to start with the "<#>" tag and finish with the 11 character code extracted from your app's hash, in the example below **FA+9qCX9VSu**.

##### SMS example

    <#> Your ExampleApp code is: 123ABC78
    FA+9qCX9VSu
