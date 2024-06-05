package signature;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class WBH5FaceVerifySDK {
    //没有网络连接
    private static final String NETWORK_NONE = "NETWORK_NONE";
    //wifi连接
    private static final String NETWORK_WIFI = "NETWORK_WIFI";
    //手机网络数据连接类型
    private static final String NETWORK_2G = "NETWORK_2G";
    private static final String NETWORK_3G = "NETWORK_3G";
    private static final String NETWORK_4G = "NETWORK_4G";
    private static final String NETWORK_MOBILE = "NETWORK_MOBILE";
    private static final int VIDEO_REQUEST = 0x0001;
    public static final int REQUEST_PERMISSION_CAMERA = 0x01;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private static WBH5FaceVerifySDK instance;

    /**
     * 获取当前网络连接类型
     *
     * @param context
     */
    private static String getNetWorkState(Context context) {
        //获取系统的网络服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //如果当前没有网络
        if (null == connManager) {
            return NETWORK_NONE;
        }

        //获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }

        // 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_2G;
                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_3G;
                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_4G;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORK_3G;
                            } else {
                                return NETWORK_MOBILE;
                            }
                    }
                }
            }
        }
        return NETWORK_NONE;
    }

    public static synchronized WBH5FaceVerifySDK getInstance() {
        if (null == instance) {
            instance = new WBH5FaceVerifySDK();
        }
        return instance;
    }

    private WBH5FaceVerifySDK() {
    }

    public void setWebViewSettings(WebView mWebView, Context context) {
        if (null == mWebView) {
            return;
        }
        if (null == mWebView) {
            return;
        }

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setTextZoom(100);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
//        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
//        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
//        webSetting.setAppCachePath(context.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(context.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(context.getDir("geolocation", 0).getPath());
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSetting.setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }

        String ua = webSetting.getUserAgentString();
        try {
            webSetting.setUserAgentString(ua + ";webank/h5face;webank/1.0" + ";netType:" +
                    getNetWorkState(context) + ";appVersion:" +
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode +
                    ";packageName:" + context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            webSetting.setUserAgentString(ua + ";webank/h5face;webank/1.0");
            e.printStackTrace();
        }
    }

    public boolean receiveH5FaceVerifyResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_REQUEST) { //根据请求码判断返回的是否是h5刷脸结果
            if (null == mUploadMessage && null == mUploadCallbackAboveL) {
                return true;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            Uri[] uris = result == null ? null : new Uri[]{result};
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(uris);
                setmUploadCallbackAboveL(null);
            } else {
                mUploadMessage.onReceiveValue(result);
                setmUploadMessage(null);
            }
            return true;
        }
        return false;
    }

    public boolean recordVideoForApiBelow21(ValueCallback<Uri> uploadMsg, String acceptType, Activity activity) {
        if ("video/webank".equals(acceptType)) {
            setmUploadMessage(uploadMsg);
            recordVideo(activity);
            return true;
        }
        return false;
    }

    @TargetApi(21)
    public boolean recordVideoForApi21(WebView webView, ValueCallback<Uri[]> filePathCallback, Activity activity, WebChromeClient.FileChooserParams fileChooserParams) {
        if ("video/webank".equals(fileChooserParams.getAcceptTypes()[0])||webView.getUrl().startsWith("https://miniprogram-kyc.tencentcloudapi.com")||webView.getUrl().startsWith("https://ida.webank.com")) { //是h5刷脸
            setmUploadCallbackAboveL(filePathCallback);
            recordVideo(activity);
            return true;
        }
        return false;
    }

    /**
     * 调用系统前置摄像头进行视频录制
     */
    public void recordVideo(Activity activity) {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.CAMERA)) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
                activity.startActivityForResult(intent, VIDEO_REQUEST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EasyPermissions.requestPermissions(activity, "请同意使用相机功能", REQUEST_PERMISSION_CAMERA, Manifest.permission.CAMERA);
        }
    }

    private void setmUploadMessage(ValueCallback<Uri> uploadMessage) {
        mUploadMessage = uploadMessage;
    }

    private void setmUploadCallbackAboveL(ValueCallback<Uri[]> uploadCallbackAboveL) {
        mUploadCallbackAboveL = uploadCallbackAboveL;
    }
}
