package signature;

import static android.webkit.WebSettings.LOAD_NO_CACHE;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comingzones.fubaoapp.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author xingtian on 2019/3/25
 */
public class H5Activity extends Activity {

    public final static String SCHEMA_REAL = "esign://demo/realBack";

    public final static String SCHEMA_SIGN = "esign://demo/signBack";
    public static final int REQUEST_PERMISSION_CAMERA = 0x01;
    private WebView mWebView;
    private View closeBtn;
    String curUrl = null;
    boolean viewFile = false;

    long time = System.currentTimeMillis();

    ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_h5);


        mWebView = findViewById(R.id.webview);

        closeBtn = findViewById(R.id.closeBtn);

        WebSettings webSetting = mWebView.getSettings();

        webSetting.setJavaScriptEnabled(true);
        webSetting.setDomStorageEnabled(true);
//        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
//        webSetting.setAllowFileAccess(true);
//        webSetting.setAppCacheEnabled(true);
        String appCachePath = getApplication().getCacheDir().getAbsolutePath();
//        webSetting.setAppCachePath(appCachePath);
        webSetting.setDatabaseEnabled(true);
        webSetting.setCacheMode(LOAD_NO_CACHE);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }


        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new H5FaceWebChromeClient(this));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
       WBH5FaceVerifySDK.getInstance().setWebViewSettings(mWebView, getApplicationContext());

        processExtraData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWebView.stopLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mWebView != null) {
                mWebView.removeAllViews();
                mWebView.destroy();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData() {

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Log.e("test", "===" + uri);
        if (uri != null) {
            // 芝麻认证刷脸结束返回获取后续操作页面地址
//            String callbackUrl = uri.getQueryParameter("callback");

            String callbackUrl = uri.getQueryParameter("realnameUrl");
            if (!TextUtils.isEmpty(callbackUrl)) {
                try {
                    mWebView.loadUrl(URLDecoder.decode(callbackUrl, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        } else {
            String url = intent.getStringExtra("url");
            viewFile = intent.getBooleanExtra("view_file", false);
            if (url.startsWith("alipay")) {

                try {
                    Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent2);
                    return;
                } catch (Exception e) {
                }
            }
            if (curUrl == null) {
                curUrl = url;
            }
            mWebView.loadUrl(url);
        }

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url == null) {
                return false;
            }

            Uri uri = Uri.parse(url);
            Log.e("test", "要加载的地址:" + uri.getScheme() + " " + url + " ");
            Log.e("test", "要加载的域名:" + uri.getAuthority() );
            if(uri.getAuthority().contains("fubaobaoxian")){
                closeBtn.setVisibility(View.VISIBLE);
            }else{
                closeBtn.setVisibility(View.GONE);
            }
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                view.loadUrl(url);
                return true;
            } else if (uri.getScheme().equals("js") || uri.getScheme().equals("jsbridge")) {

                // js://signCallback?signResult=true  签署结果
                if (uri.getAuthority().equals("signCallback")) {
                    if (viewFile) {
                        view.loadUrl(curUrl);
                        return true;
                    } else {
                        if (url.contains("signResult")) {
                            boolean signResult = uri.getBooleanQueryParameter("signResult", false);
                            Toast.makeText(H5Activity.this, "签署结果： " + " signResult = " + signResult, Toast.LENGTH_LONG).show();
                        } else {
                            String tsignCode = uri.getQueryParameter("tsignCode");
                            if ("0".equals(tsignCode)) {
                                tsignCode = "签署成功";
                            } else {
                                tsignCode = "签署失败";
                            }
                            Toast.makeText(H5Activity.this, "签署结果： " + tsignCode, Toast.LENGTH_LONG).show();
                        }
                    }
                    finish();
                }

                //js://tsignRealBack?esignAppScheme=esign://app/callback&serviceId=854677892133554052&verifycode=4a52e2af0d0abfb7b285c4f05b5af133&status=true&passed=true
                //实名结果
                if (uri.getAuthority().equals("tsignRealBack")) {
                    //实名结果字段
                    if (uri.getQueryParameter("verifycode") != null) {
                        String realVerifyCode = uri.getQueryParameter("verifycode");
                    }
                    // 实名认证结束 返回按钮/倒计时返回/暂不认证
                    boolean status = uri.getBooleanQueryParameter("status", false);
                    if (status) {
                        //认证成功返回
                        Toast.makeText(H5Activity.this, "认证成功", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                return true;
            } else if (url.startsWith(SCHEMA_REAL)) {
                //esign://app/realBack&serviceId=854677892133554052&verifycode=4a52e2af0d0abfb7b285c4f05b5af133&status=true&passed=true

                //实名结果
                if (uri.getQueryParameter("verifycode") != null) {
                    String realVerifyCode = uri.getQueryParameter("verifycode");
                }
                // 实名认证结束 返回按钮/倒计时返回/暂不认证
                boolean status = uri.getBooleanQueryParameter("status", false);
                if (status) {
                    //认证成功返回
                    Toast.makeText(H5Activity.this, "认证成功", Toast.LENGTH_LONG).show();
                    finish();
                }

                return true;
            } else if (url.startsWith(SCHEMA_SIGN)) {
                // js://signCallback?signResult=true  签署结果
                if (url.contains("signResult")) {
                    boolean signResult = uri.getBooleanQueryParameter("signResult", false);
                    Toast.makeText(H5Activity.this, "签署结果： " + " signResult = " + signResult, Toast.LENGTH_LONG).show();
                } else {
                    String tsignCode = uri.getQueryParameter("tsignCode");
                    if ("0".equals(tsignCode)) {
                        tsignCode = "签署成功";
                    } else {
                        tsignCode = "签署失败";
                    }
                    Toast.makeText(H5Activity.this, "签署结果： " + tsignCode, Toast.LENGTH_LONG).show();
                }
                finish();
                return true;
            } else if (uri.getScheme().equals("alipays")) {
                // 跳转到支付宝刷脸
                // alipays://platformapi/startapp?appId=20000067&pd=NO&url=https%3A%2F%2Fzmcustprod.zmxy.com.cn%2Fcertify%2Fbegin.htm%3Ftoken%3DZM201811133000000050500431389414
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            time = System.currentTimeMillis();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }


    public class H5FaceWebChromeClient extends WebChromeClient {
        private Activity activity;

        public H5FaceWebChromeClient(Activity mActivity) {
            this.activity = mActivity;
        }


        @Override
        public void onReceivedTitle(WebView view, String title) {
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        @TargetApi(8)
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            if (WBH5FaceVerifySDK.getInstance().recordVideoForApiBelow21(uploadMsg, acceptType, activity)) {
                return;
            }
            uploadMessage = uploadMsg;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            if (WBH5FaceVerifySDK.getInstance().recordVideoForApiBelow21(uploadMsg, acceptType, activity)) {
                return;
            }
            uploadMessage = uploadMessage;
        }

        @TargetApi(21)
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (WBH5FaceVerifySDK.getInstance().recordVideoForApi21(webView, filePathCallback, activity, fileChooserParams)) {
                return true;
            }
            uploadMessageAboveL = filePathCallback;
            recordVideo(H5Activity.this);
            return true;
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            request.grant(request.getResources());
            request.getOrigin();
        }
    }

    public void recordVideo(Activity activity) {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.CAMERA)) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
                activity.startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EasyPermissions.requestPermissions(activity, "请同意使用相机功能", REQUEST_PERMISSION_CAMERA, Manifest.permission.CAMERA);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (WBH5FaceVerifySDK.getInstance().receiveH5FaceVerifyResult(requestCode, resultCode, data)) {
            return;
        }

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) {
                return;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                if (resultCode == RESULT_OK) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{result});
                    uploadMessageAboveL = null;
                } else {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{});
                    uploadMessageAboveL = null;
                }
            } else if (uploadMessage != null) {
                if (resultCode == RESULT_OK) {
                    uploadMessage.onReceiveValue(result);
                    uploadMessage = null;
                } else {
                    uploadMessage.onReceiveValue(Uri.EMPTY);
                    uploadMessage = null;
                }
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WBH5FaceVerifySDK.getInstance().recordVideo(this);
    }
}