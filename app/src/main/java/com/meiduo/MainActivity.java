package com.meiduo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // ==================== UI ====================
    private WebView webView;
    private ProgressBar loadingProgress;
    private FrameLayout animationContainer;
    private FrameLayout errorContainer;
    private ImageView errorSvg;
    private TextView errorMessage;

    private BottomNavigationView bottomNav;

    // ==================== 状态 ====================
    private static final int STATE_WEB = 1;
    private static final int STATE_ERROR = 2;
    private int currentState = STATE_WEB;

    // URL 历史（用于返回栈）
    private final java.util.List<String> urlHistory = new java.util.ArrayList<>();

    // ==================== Lifecycle ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupWebView();
        setupBottomNavigation();
        loadHome();
    }

    // ==================== Init ====================
    private void initViews() {
        webView = findViewById(R.id.webView);
        loadingProgress = findViewById(R.id.loadingProgress);
        animationContainer = findViewById(R.id.animationContainer);
        errorContainer = findViewById(R.id.errorContainer);
        errorSvg = findViewById(R.id.errorSvg);
        errorMessage = findViewById(R.id.errorMessage);

        bottomNav = findViewById(R.id.bottom_nav);
    }

    // ==================== WebView ====================
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadingProgress.setProgress(newProgress);
                if (newProgress < 100) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                urlHistory.add(request.getUrl().toString());
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showWebView();
            }

            /* ✅ 主框架错误（ERR_NAME_NOT_RESOLVED / ERR_CONNECTION_REFUSED 等） */
            @Override
            public void onReceivedError(@NonNull WebView view,
                                        @NonNull WebResourceRequest request,
                                        @NonNull WebResourceError error) {
                if (request.isForMainFrame()) {
                    int errorCode = error.getErrorCode();
                    String description = error.getDescription() != null
                            ? error.getDescription().toString()
                            : "";
                    String failingUrl = request.getUrl() != null
                            ? request.getUrl().toString()
                            : "";

                    showErrorView(errorCode, description, failingUrl);
                }
            }

            @Override
            public void onReceivedHttpError(@NonNull WebView view,
                                            @NonNull WebResourceRequest request,
                                            @NonNull WebResourceResponse response) {
                if (request.isForMainFrame()) {
                    showErrorView(
                            response.getStatusCode(),
                            response.getReasonPhrase(),
                            request.getUrl().toString()
                    );
                }
            }

            @Override
            public void onReceivedSslError(@NonNull WebView view,
                                           @NonNull android.webkit.SslErrorHandler handler,
                                           @NonNull android.webkit.SslError error) {
                showErrorView(
                        error.getPrimaryError(),
                        getString(R.string.error_ssl),
                        error.getUrl()
                );
                handler.cancel();
            }

        });

        webView.setBackgroundColor(Color.TRANSPARENT);
    }

    // ==================== Bottom Navigation ====================
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            String url;

            if (itemId == R.id.nav_home) {
                url = getString(R.string.url_home);
            } else if (itemId == R.id.nav_page2) {
                url = getString(R.string.url_page2);
            } else if (itemId == R.id.nav_page3) {
                url = getString(R.string.url_page3);
            } else if (itemId == R.id.nav_page4) {
                url = getString(R.string.url_page4);
            } else {
                return false;
            }

            loadUrl(url);
            return true;
        });
    }

    // ==================== Page Load ====================
    private void loadHome() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadUrl(getString(R.string.url_home));
    }

    private void loadUrl(String url) {
        showLoading();
        webView.loadUrl(url);
    }

    // ==================== State Control ====================
    private void showLoading() {
        currentState = STATE_WEB;
        loadingProgress.setVisibility(View.VISIBLE);
        animationContainer.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
    }

    private void showWebView() {
        currentState = STATE_WEB;
        loadingProgress.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
    }

    private void showErrorView(int errorCode, String description, String url) {
        if (currentState == STATE_ERROR) return;

        currentState = STATE_ERROR;

        loadingProgress.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        errorSvg.setVisibility(View.VISIBLE);

        errorMessage.setText(mapNetworkErrorToMessage(errorCode)
                + "\n\n错误码：" + errorCode);
    }

    // ==================== Error Mapping ====================
    private String mapNetworkErrorToMessage(int errorCode) {
        switch (errorCode) {
            case -105: // ERR_NAME_NOT_RESOLVED
                return getString(R.string.error_dns);
            case -106: // ERR_CONNECTION_REFUSED
            case -102: // ERR_CONNECTION_FAILED
                return getString(R.string.error_connection_failed);
            case -2:   // ERR_INTERNET_DISCONNECTED
                return getString(R.string.error_network);
            case -8:   // ERR_CONNECTION_TIMED_OUT
            case -118: // ERR_DNS_TIMED_OUT
                return getString(R.string.error_timeout);
            case -6:   // ERR_TOO_MANY_REDIRECTS
                return getString(R.string.error_redirect);
            default:
                return getString(R.string.error_unknown);
        }
    }

    // ==================== Back Navigation ====================
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            if (!urlHistory.isEmpty()) {
                urlHistory.remove(urlHistory.size() - 1);
            }
        } else {
            int currentTab = bottomNav.getSelectedItemId();
            if (currentTab != R.id.nav_home) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else {
                finish();
            }
        }
    }

    // ==================== Lifecycle ====================
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
