/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "FpSecugen", "greet", [name]);
    },

    requestPermission: function(successCallback, errorCallback){
        cordova.exec(successCallback, errorCallback, "FpSecugen", "requestPermission", []);
    },

    closeDevice: function(successCallback, errorCallback){
        cordova.exec(successCallback, errorCallback, "FpSecugen", "close", []);
    },

    open: function(successCallback, errorCallback){
        cordova.exec(successCallback, errorCallback, "FpSecugen", "open", []);
    },

    capture: function(successCallback, errorCallback){
        cordova.exec(successCallback, errorCallback, "FpSecugen", "capture", []);
    }

    // capture: function(successCallback, errorCallback){
    //  cordova.exec(successCallback, errorCallback, "FpSecugen", "capture", []);
    // }

    // close: function(successCallback, errorCallback){
    //  cordova.exec(successCallback, errorCallback, "FpSecugen", "close", []);
    // }
};
