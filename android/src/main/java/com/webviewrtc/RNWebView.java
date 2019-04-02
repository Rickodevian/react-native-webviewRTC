package com.webviewrtc;

import android.annotation.SuppressLint;

import android.annotation.TargetApi;
import android.net.Uri;
import android.net.http.SslError;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.PermissionRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceError;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.SystemClock;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.net.MalformedURLException;
import java.net.URL;

class RNWebView extends WebView implements LifecycleEventListener {

    private final EventDispatcher mEventDispatcher;
    private final RNWebViewManager mViewManager;

    private String charset = "UTF-8";
    private String baseUrl = "file:///";
    private String injectedJavaScript = null;
    private boolean allowUrlRedirect = false;

    private String currentUrl = "";
    private String shouldOverrideUrlLoadingUrl = "";

    // lucas
    private String siteURL = "";

    protected class EventWebClient extends WebViewClient {
        private boolean isError = false;

        public boolean shouldOverrideUrlLoading(WebView view, String url){
            int navigationType = 0;

            if (currentUrl.equals(url) || url.equals("about:blank")) { // for regular .reload() and html reload.
                navigationType = 3;
            }

            shouldOverrideUrlLoadingUrl = url;
            mEventDispatcher.dispatchEvent(new ShouldOverrideUrlLoadingEvent(getId(), SystemClock.nanoTime(), url, navigationType));

            return true;
        }

        public void onPageFinished(WebView view, String url) {
            if (this.isError == false) {
              mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), false, url, view.canGoBack(), view.canGoForward(), 200, "Success load page"));
            }
            currentUrl = url;

            if(RNWebView.this.getInjectedJavaScript() != null) {

                //lucas
                Log.d("RNWeb2", "onPageFinished: " + RNWebView.this.getInjectedJavaScript());

                view.loadUrl("javascript:(function() {\n" + RNWebView.this.getInjectedJavaScript() + ";\n})();");
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // lucas - load original url from RN-js, use this to validate url during permission request
            siteURL = url;
            Log.d("onPageStarted", "siteURL is :" + siteURL);
            
            this.isError = false;
            mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), true, url, view.canGoBack(), view.canGoForward(), 0, "No page loaded"));
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            int statusCode = errorResponse.getStatusCode();
            String responseMessage = errorResponse.getReasonPhrase();
            String url = request.getUrl().toString();

            this.isError = true;
            mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), false, url, false, false, statusCode, responseMessage));
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError (WebView view, WebResourceRequest request, WebResourceError error) {
          String url = request.getUrl().toString();
          int statusCode = error.getErrorCode();
          String errorDescription = error.getDescription().toString();

          this.isError = true;
          mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), false, url, false, false, statusCode, errorDescription));
        }

        @Override
        public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
          this.isError = true;
          mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), false, failingUrl, false, false, errorCode, description));
        }
    }

    protected class CustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            getModule().showAlert(url, message, result);
            return true;
        }

        // For Android 4.1+
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            getModule().startFileChooserIntent(uploadMsg, acceptType);
        }

        // For Android 5.0+
        @SuppressLint("NewApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return getModule().startFileChooserIntent(filePathCallback, fileChooserParams.createIntent());
        }


        //lucas - for webchromeview
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPermissionRequest(final PermissionRequest request) {

            //lucas
            String checkPermURL = "";

            Log.d("WebChromeView", "onPermissionRequest");
            Log.d("WebChromeView", "siteURL is :" + siteURL);

            Log.d("URL", "is " + request.getOrigin().toString());

            try {
                URL checkSiteURL = new URL(siteURL);
                checkPermURL = checkSiteURL.getHost();
                Log.d("siteURL", "is " + checkSiteURL.getHost());
                Log.d("checkPermURL", "is " + checkPermURL);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
                    // verify permission request is indeed from the original domain
                    if((request.getOrigin().toString().indexOf(checkPermURL)) > 0) {
                        Log.d("Webchromeview","grant");
                        request.grant(request.getResources());
                    } else {
                        Log.d("Webchromeview","deny");
                        Log.d("Webchromeview",request.getOrigin().toString());
                        request.deny();
                    }

        }

    }

    protected class GeoWebChromeClient extends CustomWebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    public RNWebView(RNWebViewManager viewManager, ThemedReactContext reactContext) {
        super(reactContext);

        mViewManager = viewManager;
        mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();

        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setGeolocationEnabled(false);
        this.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAllowFileAccessFromFileURLs(true);
        this.getSettings().setAllowUniversalAccessFromFileURLs(true);
        this.getSettings().setLoadsImagesAutomatically(true);
        this.getSettings().setBlockNetworkImage(false);
        this.getSettings().setBlockNetworkLoads(false);
        this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        this.setWebViewClient(new EventWebClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel(); // Ignore SSL certificate errors
            }
        });

        this.setWebChromeClient(getCustomClient());

        this.addJavascriptInterface(RNWebView.this, "webView");
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setAllowUrlRedirect(boolean a) {
        this.allowUrlRedirect = a;
    }

    public boolean getAllowUrlRedirect() {
        return this.allowUrlRedirect;
    }

    public void setInjectedJavaScript(String injectedJavaScript) {
        this.injectedJavaScript = injectedJavaScript;
    }

    public String getInjectedJavaScript() {
        return this.injectedJavaScript;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void shouldOverrideWithResult(RNWebView view, ReadableArray args) {
        if (!args.getBoolean(0)) {
            view.loadUrl(shouldOverrideUrlLoadingUrl);
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public CustomWebChromeClient getCustomClient() {
        return new CustomWebChromeClient();
    }

    public GeoWebChromeClient getGeoClient() {
        return new GeoWebChromeClient();
    }

    public RNWebViewModule getModule() {
        return mViewManager.getPackage().getModule();
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        destroy();
    }

    @Override
    public void onDetachedFromWindow() {
        this.loadDataWithBaseURL(this.getBaseUrl(), "<html></html>", "text/html", this.getCharset(), null);
        super.onDetachedFromWindow();
    }

    @JavascriptInterface
     public void postMessage(String jsParamaters) {
        mEventDispatcher.dispatchEvent(new MessageEvent(getId(), jsParamaters));
    }
}
