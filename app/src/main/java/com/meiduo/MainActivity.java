package com.meiduo;

import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 美朵 - 固定展示网页的 Android 应用
 * ✅ 支持 HTTP（明文流量）
 */
public class MainActivity extends Activity {

    // ✅ 允许 HTTP
    private static final String TARGET_URL = "http://www.chana.cc.cd";

    private static final int VIEW_ERROR = 0;
    private static final int VIEW_WEB = 1;

    private FrameLayout container;
    private WebView webView;
    private TextView errorView;
    private int currentState = VIEW_ERROR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        webView = findViewById(R.id.webView);
        errorView = findViewById(R.id.errorView);

        setupWebView();
        loadUrl();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        // ✅ 允许 HTTP / HTTPS 混合内容（API 21+）
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // ✅ WebView 远程调试（静态方法）
        WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            /**
             * ✅ 允许 WebView 自己处理 HTTP / HTTPS
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // false = WebView 加载该 URL
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                showErrorView(errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           android.webkit.SslErrorHandler handler,
                                           SslError error) {
                // ⚠️ HTTP 不会触发 SSL 错误
                showErrorView(
                        error.getPrimaryError(),
                        "SSL 证书错误",
                        error.getUrl()
                );
                handler.cancel();
            }

            @Override
            public void onReceivedHttpError(WebView view,
                                            WebResourceRequest request,
                                            WebResourceResponse response) {
                showErrorView(
                        response.getStatusCode(),
                        response.getReasonPhrase(),
                        request.getUrl().toString()
                );
            }
        });

        webView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadUrl() {
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.loadUrl(TARGET_URL);
    }

    private void showErrorView(int errorCode, String description, String url) {
        if (currentState != VIEW_ERROR) {
            currentState = VIEW_ERROR;
            webView.setVisibility(View.GONE);
            container.setVisibility(View.GONE);

            errorView.setText(formatErrorMessage(errorCode, description, url));
            errorView.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
        }
    }

    private String formatErrorMessage(int errorCode, String description, String url) {
        switch (errorCode) {
            case -2:
                return "🌐 网络连接错误\n\n无法连接到互联网，请检查网络后重试。";
            case -3:
                return "🔧 DNS 解析错误\n\n无法解析网址，请检查网址是否正确。";
            case -5:
                return "🔧 主机名验证失败\n\n网址验证失败。";
            case -6:
                return "🌐 重定向循环\n\n网页存在重定向循环。";
            case 18:
            case 21:
            case 26:
                return "📜 SSL 证书错误\n\n网页证书不受信任或已过期。";
            default:
                return "⚠️ 网页加载失败\n\n"
                        + description
                        + "\n错误码：" + errorCode
                        + "\nURL：" + url;
        }
    }
}
