package tv.rhinobird.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;


public class MainActivity extends Activity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private XWalkView xWalkWebView;
    private WebView mWebRTCWebView;
    private WebView webLoader;
    private static final String wrapUrl = "https://beta.rhinobird.tv/";
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initWeb();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();

        }

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        if (mUrl == null) {
            mUrl = wrapUrl;
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String streamId = uri.getLastPathSegment();
            mUrl += "stream/" + streamId;
        }

        loadWeb();
    }

    @Override
    public void onPause() {
        super.onPause();
            if (xWalkWebView != null) {
                xWalkWebView.pauseTimers();
                xWalkWebView.onHide();
            }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (xWalkWebView != null) {
            xWalkWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
            xWalkWebView.stopLoading();
        }
    }

   @Override
    public void onResume() {
       super.onResume();
       if (xWalkWebView != null) {
           xWalkWebView.resumeTimers();
           xWalkWebView.onShow();
       }

    }
    @Override
    public void onRestart() {
        super.onRestart();
        if (xWalkWebView != null) {
            xWalkWebView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (xWalkWebView != null) {
            xWalkWebView.onDestroy();
        }
    }

    public void initWeb(){
        webLoader = (WebView) findViewById(R.id.fragment_loader_webview);
        webLoader.loadUrl("file:///android_asset/index.html");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initNativeWebview();
        }
        else{
            initxWalkWebview();
        }

    }

    private void initxWalkWebview() {
        xWalkWebView = (XWalkView) findViewById(R.id.fragment_main_webview);
        xWalkWebView.clearCache(true);
        xWalkWebView.addJavascriptInterface(new RbWebAppInterface(this), "RbWebApp");
        xWalkWebView.setResourceClient(new XWalkResourceClient(xWalkWebView) {
            @Override
            public void onLoadFinished(XWalkView view, String url) {
                super.onLoadFinished(xWalkWebView, url);
                xWalkWebView.setVisibility(View.VISIBLE);
                webLoader.setVisibility(View.GONE);
            }

        });
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, false);

    }

    private void initNativeWebview() {
        mWebRTCWebView = (WebView) findViewById(R.id.fragment_main_webview);
        WebSettings settings = mWebRTCWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        WebView.setWebContentsDebuggingEnabled(true);
        mWebRTCWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebRTCWebView.setVisibility(View.VISIBLE);
                webLoader.setVisibility(View.GONE);
            }
        });
        mWebRTCWebView.addJavascriptInterface(new RbWebAppInterface(this), "RbWebApp");
        mWebRTCWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Allow geo location permissions
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        if (request.getOrigin().toString().equals(mUrl)) {
                            request.grant(request.getResources());
                        } else {
                            request.deny();
                        }
                    }
                });
            }
        });
    }


    public void loadWeb(){
        if (webLoader.getVisibility() == View.GONE){
            webLoader.setVisibility(View.VISIBLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadNative();
        } else{
            loadxWalk();
        }
    }

    private void loadxWalk() {
        xWalkWebView.setVisibility(View.GONE);
        if (DetectConnection.checkInternetConnection(this)) {
            xWalkWebView.load(mUrl, null);
        }
        else {
            xWalkWebView.load("file:///android_asset/error_page.html", null);
        }
    }

    private void loadNative() {
        //mWebRTCWebView.setVisibility(View.GONE);
        if (DetectConnection.checkInternetConnection(this)) {
            mWebRTCWebView.loadUrl(mUrl);
        }
        else {
            mWebRTCWebView.loadUrl("file:///android_asset/error_page.html");
        }
    }

}
