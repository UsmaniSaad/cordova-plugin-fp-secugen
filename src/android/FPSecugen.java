package sa.com.plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxConstant;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPosition;
import SecuGen.FDxSDKPro.SGImpressionType;
import SecuGen.FDxSDKPro.SGWSQLib;

public class FPSecugen extends CordovaPlugin {

    static final String TAG = "SecuGen USB";
    private static String templatePath = "/sdcard/Download/fprints/";
    private static String serverUrl = "";
    private static String serverUrlFilepath = "";
    private static String serverKey = "";
    private static String projectName = "";
    private static String templateFormat = "";

    private IntentFilter filter;

    private int QUALITY_VALUE = 0;

    // actions
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String COOLMETHOD = "coolMethod";
    private static final String REGISTER = "register";
    private static final String IDENTIFY = "identify";
    private static final String ACTION_CAPTURE = "capture";
    private static final String ACTION_CLOSE = "close";
    private static final String BLINK = "blink";
    private static final String VERIFY = "verify";
    private static final String SCAN = "scan";
    private static final String OPEN = "open";
    private static final String EXITAPP = "exitapp";
    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
    private int[] mMaxTemplateSize;
    private int mImageWidth;
    private int mImageHeight;
    private boolean mLed;

    private JSGFPLib sgfplib;

    private Context context;

    long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
    // UsbManager instance to deal with permission and opening
    private UsbManager manager;

    //    private AfisEngine afis;
    private ScanProperties props;
    private PendingIntent mPermissionIntent;
    private boolean usbPermissionRequested;
    private int mImageDPI;
    private boolean bSecuGenDeviceOpened;
    private String serialNumber;
    private UsbBroadcastReceiver usbReceiver;
    private int count = 0;

    public void initialize(CordovaInterface cordova, CordovaWebView view) {
        super.initialize(cordova, view);

        context = cordova.getActivity().getBaseContext();

        String path = "/sdcard/Download/fprints/";
        File templatePathFile = new File(templatePath);
        templatePathFile.mkdirs();
        FPSecugen.setTemplatePath(path);
        // LOG.d(TAG,"this.cordova.getActivity().getPackageName(): " + this.cordova.getActivity().getPackageName());
        // int id = context.getResources().getIdentifier("templatePath", "string", this.cordova.getActivity().getPackageName());
        // LOG.d(TAG,"templatePath id: " + id);
        // String translatedValue = context.getResources().getString(id);
        // LOG.d(TAG,"translatedValue: " + translatedValue);
        // File templatePathFile = new File(templatePath);
        // templatePathFile.mkdirs();
        // SecugenPlugin.setTemplatePath(translatedValue);
        // id = context.getResources().getIdentifier("serverUrl", "string", this.cordova.getActivity().getPackageName());
        // LOG.d(TAG,"serverUrl id: " + id);
        // String serverUrl = context.getResources().getString(id);
        // LOG.d(TAG,"serverUrl: " + serverUrl);
        // SecugenPlugin.setServerUrl(serverUrl);
        // id = context.getResources().getIdentifier("serverKey", "string", this.cordova.getActivity().getPackageName());
        // String serverKey = context.getResources().getString(id);
        // LOG.d(TAG,"serverKey: " + serverKey);
        // SecugenPlugin.setServerKey(serverKey);
        // id = context.getResources().getIdentifier("projectName", "string", this.cordova.getActivity().getPackageName());
        // String projectName = context.getResources().getString(id);
        // LOG.d(TAG,"projectName: " + projectName);
        // SecugenPlugin.setProjectName(projectName);
        // id = context.getResources().getIdentifier("templateFormat", "string", this.cordova.getActivity().getPackageName());
        // String templateFormat = context.getResources().getString(id);
        // LOG.d(TAG,"templateFormat: " + templateFormat);
        // SecugenPlugin.setTemplateFormat(templateFormat);
        // id = context.getResources().getIdentifier("serverUrlFilepath", "string", this.cordova.getActivity().getPackageName());
        // LOG.d(TAG,"serverUrlFilepath id: " + id);
        // String serverUrlFilepath = context.getResources().getString(id);
        // LOG.d(TAG,"serverUrlFilepath: " + serverUrlFilepath);
        // SecugenPlugin.setServerUrlFilepath(serverUrlFilepath);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        mMaxTemplateSize = new int[1];
        if (action.equals("greet")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else if (action.equals(ACTION_REQUEST_PERMISSION)) {
            requestPermission2(callbackContext);
            return true;
        } else if (action.equals(ACTION_CAPTURE)) {
            capture(callbackContext);
            return true;

        } else if (action.equals(OPEN)) {
            initDeviceSettings();
            return true;
        } else if (action.equals(ACTION_CLOSE)) {

            closeDevice();
            callbackContext.success("Closed");

            return true;

        } else if (action.equals(EXITAPP)) {
            exitApplication();
            callbackContext.success("Closed Application");

            return true;
        } else if (action.equals("close")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {
            return false;
        }
    }

    /*private void openDevice() {

//        Toast.makeText(context, "Permission", Toast.LENGTH_SHORT).show();
        debugMessage("Opening SecuGen Device\n");
        long error = sgfplib.OpenDevice(0);
        debugMessage("OpenDevice() ret: " + error + "\n");
        if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            bSecuGenDeviceOpened = true;
            SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
            error = sgfplib.GetDeviceInfo(deviceInfo);
            debugMessage("GetDeviceInfo() ret: " + error + "\n");
            mImageWidth = deviceInfo.imageWidth;
            mImageHeight = deviceInfo.imageHeight;
            mImageDPI = deviceInfo.imageDPI;
            serialNumber = new String(deviceInfo.deviceSN());
            debugMessage("Image width: " + mImageWidth + "\n");
            debugMessage("Image height: " + mImageHeight + "\n");
            debugMessage("Image resolution: " + mImageDPI + "\n");
            debugMessage("Serial Number: " + new String(deviceInfo.deviceSN()) + "\n");
            sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
            debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
//                        mRegisterTemplate = new byte[mMaxTemplateSize[0]];
//                        mVerifyTemplate = new byte[mMaxTemplateSize[0]];
//                        EnableControls();
//                        boolean smartCaptureEnabled = this.mToggleButtonSmartCapture.isChecked();
//                        if (smartCaptureEnabled)
//                            sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte) 1);
//                        else
            sgfplib.WriteData(SGFDxConstant.WRITEDATA_COMMAND_ENABLE_SMART_CAPTURE, (byte) 0);
//                        if (mAutoOnEnabled) {
//                            autoOn.start();
//                            DisableControls();
//                        }
        } else {
            debugMessage("Waiting for USB Permission\n");
        }
    }*/

    private void requestPermission2(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                manager = (UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE);
                sgfplib = new JSGFPLib((UsbManager) context.getSystemService(Context.USB_SERVICE));

                mLed = false;

                long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
                if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
                    String message = "Fingerprint device initialization failed!";
                    if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND) {
                        message = "Error: Either a fingerprint device is not attached or the attached fingerprint device is not supported.";
                    }
                    debugMessage(message);
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } else {
                    UsbDevice usbDevice = sgfplib.GetUsbDevice();
                    if (usbDevice == null) {
                        String message = "Error: Fingerprint sensor not found!";
                        debugMessage(message);
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    }

                    // create the intent that will be used to get the permission
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, new Intent(UsbBroadcastReceiver.USB_PERMISSION), 0);
                    // and a filter on the permission we ask
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(UsbBroadcastReceiver.USB_PERMISSION);
                    // this broadcast receiver will handle the permission results
                    usbReceiver = new UsbBroadcastReceiver(callbackContext, cordova.getActivity());
                    cordova.getActivity().registerReceiver(usbReceiver, filter);
                    // finally ask for the permission
                    manager.requestPermission(usbDevice, pendingIntent);
//                    initDeviceSettings();
                }
            }
        });
    }


    private void exitApplication() {
        cordova.getActivity().finish();
    }

    public void initDeviceSettings() {
        long error;
//        openDeviceRequestCount++;
        error = sgfplib.OpenDevice(0);
        debugMessage("OpenDevice() ret: " + error + "\n");
        boolean isOpened = error == SGFDxErrorCode.SGFDX_ERROR_NONE;

        if (!isOpened) {
            //init setting again...
            /*if(openDeviceRequestCount < 40) {
                initDeviceSettings();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

            return;
        } else {
            debugMessage("Device opened successfully");
//            openDeviceRequestCount = 0;
        }

//        boolean deviceInUse = sgfplib.DeviceInUse();
//        debugMessage("Device In Use  =" + deviceInUse+"");
        SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
        error = sgfplib.GetDeviceInfo(deviceInfo);
        debugMessage("GetDeviceInfo() ret: " + error + "\n");
        mImageWidth = deviceInfo.imageWidth;
        mImageHeight = deviceInfo.imageHeight;
        serialNumber = new String(deviceInfo.deviceSN());
        debugMessage("Setting props: mImageWidth: " + mImageWidth + " mImageHeight: " + mImageHeight);
        props = new ScanProperties(mImageWidth, mImageHeight);
//                  sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
//                  sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
        Field fieldName;
        try {
            fieldName = SGFDxTemplateFormat.class.getField(FPSecugen.getTemplateFormat());
            short templateValue = fieldName.getShort(null);
            debugMessage("templateValue: " + templateValue);
            sgfplib.SetTemplateFormat(templateValue);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sgfplib.GetMaxTemplateSize(mMaxTemplateSize);
        debugMessage("mMaxTemplateSize: " + mMaxTemplateSize[0] + "\n");
        mRegisterTemplate = new byte[mMaxTemplateSize[0]];
        sgfplib.writeData((byte) 5, (byte) 1);
    }

    private void closeDevice() {
        if (sgfplib != null) {
            sgfplib.CloseDevice();
            sgfplib.Close();
        }

        if (usbReceiver != null)
            cordova.getActivity().unregisterReceiver(usbReceiver);
    }

    public void capture(final CallbackContext callbackContext) {
        try {
            captureFingerPrint1(callbackContext);
        } catch (IOException ex) {
            ex.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Exception"));
        } catch (Exception ex) {
//            openDevice();
            ex.printStackTrace();
            Toast.makeText(context, "Capture Again! something went wrong", Toast.LENGTH_SHORT).show();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Exception"));
        }
    }

    public ImageData captureFingerPrint1(final CallbackContext callbackContext) throws IOException {
//        sgfplib.

        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
        byte[] buffer = new byte[mImageWidth * mImageHeight];

        dwTimeStart = System.currentTimeMillis();

        sgfplib.SetLedOn(true);
        long result = sgfplib.GetImageEx(buffer, 5000, 0);

        String NFIQString = "";

//        DumpFile("capture2016.raw", buffer);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd - dwTimeStart;
        debugMessage("getImageEx(10000,50) ret:" + result + " [" + dwTimeElapsed + "ms]" + NFIQString + "\n");

        int[] quality = new int[1];
        SGFingerInfo fingerInfo = new SGFingerInfo();
        int encodePixelDepth = 8;
        int encodePPI = 500;
        int[] wsqImageOutSize = new int[1];
        byte[] wsqImage = null;

//        Toast.makeText(cordova.getActivity(), "captured", Toast.LENGTH_SHORT).show();//(TAG, byteArrayToBase64(buffer));

        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            result = sgfplib.GetImageQuality(mImageWidth, mImageHeight, buffer, quality);

            if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_LI;
                fingerInfo.ImageQuality = quality[0];
                fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
                fingerInfo.ViewNumber = 1;
                Log.d("FP Image Quality", fingerInfo.ImageQuality + "");
                if (fingerInfo.ImageQuality >= QUALITY_VALUE) {

                    result = sgfplib.WSQGetEncodedImageSize(wsqImageOutSize,
                            SGWSQLib.BITRATE_5_TO_1, buffer, mImageWidth,
                            mImageHeight, encodePixelDepth, encodePPI);

                    if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                        wsqImage = new byte[wsqImageOutSize[0]];
                        result = sgfplib.WSQEncode(wsqImage,
                                SGWSQLib.BITRATE_5_TO_1, buffer,
                                mImageWidth, mImageHeight, encodePixelDepth,
                                encodePPI);

//                        sgfplib.WS

                        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                            //TODO send base64 image
                            Bitmap bitmap = this.toGrayscale(buffer);
//                            Utils.saveImageFile(context, callbackContext, bitmap, "fp");
//                            File file = new java.io.File(Environment
//                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                                    +"fp.png");
//
//                            Log.d("FP Path", file.getAbsolutePath());

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            String wsqEncoded = Base64.encodeToString(wsqImage, Base64.DEFAULT);
//                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                            bos.write(buffer);
//                            bos.flush();
//                            bos.close();
                            JSONObject json = new JSONObject();
                            try {
                                json.put("image", encoded);
                                json.put("wsqImage", wsqEncoded);
                                json.put("errorCode", 0);
                                json.put("quality", fingerInfo.ImageQuality);
                                json.put("serialNumber", serialNumber);
                            } catch (JSONException ex) {
                                Log.d("Exception", "JSON Exception");
                            }

                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                            callbackContext.sendPluginResult(pluginResult);
                            return new ImageData(0);
                        }
                    }
                } else {
                    result = 10001;

                    //send callback error
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Quality of the fingerprint is less than " + QUALITY_VALUE);
                    callbackContext.sendPluginResult(pluginResult);
                }

//                buffer


            } else {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Quality Error Code: " + result);
                callbackContext.sendPluginResult(pluginResult);
            }
        } else {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, result);
            callbackContext.sendPluginResult(pluginResult);
        }


        buffer = null;

        return new ImageData(result, buffer, wsqImage, fingerInfo.ImageQuality);
    }

    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer) {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        //Bitmap bm contains the fingerprint img
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }

    public static String getTemplateFormat() {
        return templateFormat;
    }

    public static String getTemplatePath() {
        return templatePath;
    }

    public static void setTemplatePath(String templatePath) {
        FPSecugen.templatePath = templatePath;
    }

    private void debugMessage(String message) {
        Log.d("Cordova FP", message);
    }

    public class UsbBroadcastReceiver extends BroadcastReceiver {
        // logging tag
        private final String TAG = "UsbBroadcastReceiver";
        // usb permission tag name
        public static final String USB_PERMISSION = "com.example.plugin.USB_PERMISSION";
        // cordova callback context to notify the success/error to the cordova app
        private CallbackContext callbackContext;
        // cordova activity to use it to unregister this broadcast receiver
        private Activity activity;

        /**
         * Custom broadcast receiver that will handle the cordova callback context
         *
         * @param callbackContext
         * @param activity
         */
        public UsbBroadcastReceiver(CallbackContext callbackContext, Activity activity) {
            this.callbackContext = callbackContext;
            this.activity = activity;
        }

        public UsbBroadcastReceiver(Activity activity) {
            this.activity = activity;
        }

//        private int count = 0;


        /**
         * Handle permission answer
         *
         * @param context
         * @param intent
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (USB_PERMISSION.equals(action)) {
                // deal with the user answer about the permission
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.d(TAG, "Permission to connect to the device was accepted!");
                    initDeviceSettings();
                    if (callbackContext != null)
                        callbackContext.success("Permission to connect to the device was accepted!");
                } else {
                    Log.d(TAG, "Permission to connect to the device was denied!");
                    if (callbackContext != null)
                        callbackContext.error("Permission to connect to the device was denied!");
                }
                // unregister the broadcast receiver since it's no longer needed
                activity.unregisterReceiver(this);
            }
        }
    }
}

