package signature;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;



import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * This class echoes a string called from JavaScript.
 */
public class Signature extends CordovaPlugin {

  private CallbackContext callbackContext1 = null;

  private static final String TAG = "SIGNATURE";
  //  当前存储的结果
  private JSONObject result = new JSONObject();
  //  签名页面是否显示
  private Boolean isPush = false;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("startH5Activity")) {
      JSONObject message = args.getJSONObject(0);
      callbackContext1 = callbackContext;
      this.startH5Activity(message, callbackContext);
      return true;
    }
    return false;
  }
  @Subscribe
  public void processResult() {
    //添加实名/意愿/签署完成之后的逻辑

  }
  @Override
  protected void pluginInitialize() {
    //    初始化组件
//    EventBus.getDefault().register(this);
    //    设置秘钥信息
//    EsignSdk.getInstance().init(this.preferences.getString("SIGNATURE_KEY",null), this.preferences.getString("SIGNATURE_LICENSE",null));

  }

  private void startH5Activity(JSONObject message, CallbackContext callbackContext) throws JSONException {
    result.put("key","cancel");
    String ulr = message.getString("url");
    Log.i("ulr===",ulr);
//    EsignSdk.getInstance().startH5Activity(cordova.getActivity(), ulr);
    if (TextUtils.isEmpty(ulr)) {
      return;
    }

    Intent intent = new Intent(cordova.getActivity(), H5Activity.class);
    intent.putExtra("url", ulr);
    cordova.getActivity().startActivity(intent);
    this.isPush=true;
    PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject("{\"key\":\"start\",\"res\":\"success\"}"));
    result.setKeepCallback(true);
    callbackContext1.sendPluginResult(result);

  }

  /**
   * event.result内容是json格式
   * {key:XXX,res:XXX}
   * Key代表业务类型对应的值为：sign,realName,will
   * sign:签署 realName:实名 will:意愿
   * res代表刷脸结果对应的值：fail,success
   * fail:失败 success:成功
   */


  private void setStatusBarBackgroundColor(final String colorPref) {
    if (Build.VERSION.SDK_INT >= 21) {
      if (colorPref != null && !colorPref.isEmpty()) {
        final Window window = cordova.getActivity().getWindow();
        // Method and constants not available on all SDKs but we want to be able to compile this code with any SDK
        window.clearFlags(0x04000000); // SDK 19: WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(0x80000000); // SDK 21: WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        try {
          // Using reflection makes sure any 5.0+ device will work without having to compile with SDK level 21
          window.getClass().getMethod("setStatusBarColor", int.class).invoke(window, Color.parseColor(colorPref));
        } catch (IllegalArgumentException ignore) {
          LOG.e(TAG, "Invalid hexString argument, use f.i. '#999999'");
        } catch (Exception ignore) {
          // this should not happen, only in case Android removes this method in a version > 21
          LOG.w(TAG, "Method window.setStatusBarColor not found for SDK level " + Build.VERSION.SDK_INT);
        }
      }
    }
  }
  @Override
  public void onPause(boolean multitasking) {
    LOG.i(TAG,"on Pause");
  }
  @Override
  public void onResume(boolean multitasking) {
    //    防止其他插件情况下 会有回调进来
    if(!this.isPush){
      return;
    }
    this.isPush=false;
    Log.i(TAG,"回调结果：" + this.result.toString());
    PluginResult result = new PluginResult(PluginResult.Status.OK,this.result);
    result.setKeepCallback(true);
    callbackContext1.sendPluginResult(result);
  }

  private void setStatusBarStyle(final String style) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (style != null && !style.isEmpty()) {
        View decorView = cordova.getActivity().getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();

        String[] darkContentStyles = {
          "default",
        };

        String[] lightContentStyles = {
          "lightcontent",
          "blacktranslucent",
          "blackopaque",
        };

        if (Arrays.asList(darkContentStyles).contains(style.toLowerCase())) {
          decorView.setSystemUiVisibility(uiOptions | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
          return;
        }

        if (Arrays.asList(lightContentStyles).contains(style.toLowerCase())) {
          decorView.setSystemUiVisibility(uiOptions & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
          return;
        }

        LOG.e(TAG, "Invalid style, must be either 'default', 'lightcontent' or the deprecated 'blacktranslucent' and 'blackopaque'");
      }
    }
  }
}
