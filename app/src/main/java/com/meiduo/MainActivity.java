package com.meiduo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 美朵 - 固定展示网页的 Android 应用
 * 加载 www.chana.cc.cd，隐藏地址栏，网络错误时显示友好提示
 */
public class MainActivity extends Activity {
    
    private static final String TARGET_URL = "https://www.chana.cc.cd";
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

    /**
     * 配置 WebView
     */
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        
        // 启用 JavaScript
        settings.setJavaScriptEnabled(true);
        
        // 允许混合内容（如果网页有 HTTPS 资源但被标记为不安全）
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // 设置缓存大小
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        
        // 启用远程调试
        settings.setRemoteDebuggingEnabled(true);

        // 设置 WebViewClient 以捕获网络错误
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri parsed = Uri.parse(url);
                if (parsed.getScheme().equals("file") || 
                    parsed.getScheme().equals("content") ||
                    parsed.getScheme().equals("about")) {
                    view.loadUrl(url);
                    return true;
                }
                // 拒绝所有外部链接
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载完成后显示容器
                view.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showErrorView(errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 忽略 SSL 错误，显示错误提示
                showErrorView(error.getPrimaryError(), "SSL 证书错误", error.getUrl());
                handler.cancel();
            }

            @Override
            public void onReceivedHttpError(WebView view, Handler handler, HttpError error) {
                // 处理 HTTP 错误（如 404、500 等）
                showErrorView(error.getErrorCode(), error.getDescription(), error.getUrl());
            }
        });

        // 设置 WebView 的背景色，避免加载时的白屏
        webView.setBackgroundColor(getColor(android.R.color.transparent));

        // 隐藏进度条（如果需要）
        // webView.setProgressBar(null);
    }

    /**
     * 加载目标 URL
     */
    private void loadUrl() {
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.loadUrl(TARGET_URL);
    }

    /**
     * 显示错误提示
     */
    private void showErrorView(int errorCode, String description, String url) {
        if (currentState != VIEW_ERROR) {
            currentState = VIEW_ERROR;
            webView.setVisibility(View.GONE);
            container.setVisibility(View.GONE);

            // 设置友好的错误提示
            String message = formatErrorMessage(errorCode, description, url);
            errorView.setText(message);

            // 显示错误视图
            errorView.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 格式化错误消息
     */
    private String formatErrorMessage(int errorCode, String description, String url) {
        // 常见错误码的友好提示
        switch (errorCode) {
            case -2: // 无网络连接
                return "\uD83C\uDF10 网络连接错误 \n\n无法连接到互联网。请检查您的网络连接后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case -3: // DNS 解析失败
                return "\uD83D\uDD27 DNS 解析错误 \n\n无法解析网址。请检查网址是否正确后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case -5: // 主机名验证失败
                return "\uD83D\uDD27 主机名验证错误 \n\n网址验证失败。请检查网址是否正确后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case -6: // 重定向循环
                return "\uD83C\uDF10 重定向循环 \n\n网页存在重定向循环。请刷新页面后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case 18: // 证书过期
                return "\uD83D\uDCCE 证书错误 \n\n网页的 SSL 证书已过期。请刷新页面后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case 21: // 证书不受信任
                return "\uD83D\uDCCE 不受信任的证书 \n\n网页的 SSL 证书不受信任。请刷新页面后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            case 26: // 证书不受信任
                return "\uD83D\uDCCE 不受信任的证书 \n\n网页的 SSL 证书不受信任。请刷新页面后重试。\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "错误信息：" + description;
            default:
                // 通用错误提示
                return "\uD83D\uDD2E 网页加载失败 \n\n" +
                       description + "\n\n" +
                       "错误代码：" + errorCode + "\n" +
                       "尝试加载的网址：" + url;
        }
    }
}
