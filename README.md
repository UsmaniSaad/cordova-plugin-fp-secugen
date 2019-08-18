# Cordova Secugen Finger Print Plugin Android

## Simple cordova plugin that takes care of Secugen Native APIs for Android.

### Steps to add this plugin in your project.

1.   Add the plugin in your project **cordova plugin add cordova-plugin-fp-secugen**
2.  You have to request for jar and .so files from Secugen Website. They ask a valid email id and send android sdk to this email.
3.  Copy complete **libs folder** from the provided zip file to your projects **libs** folder.  
4.   Also add **FDxSDKProAndroid.jar** file from zip sdk file to the your projects **libs** folder.
5.   Now add **FDxSDKProAndroid.jar** to dependencies in **build.gradle** file eg:

``` 
dependencies {
    compile fileTree(include: '*.jar', dir: 'libs')
    //OTHER DEPENDENCIES    
    compile files('libs/FDxSDKProAndroid.jar')
}
```

### How to use it?

Add the following code snippet anywhere after deviceready event.

``` 
fpindex.capture(
    function(resp) {
        //resp json object contains fp data...
        console.log(resp);
    },
    function(b) { //Error handler...
        fpindex.requestPermission(function(a) {
            console.log(a);
        }, function(b) {
            console.log(b);
        });
    }
);
```

