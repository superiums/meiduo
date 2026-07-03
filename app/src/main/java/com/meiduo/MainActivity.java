package com.meiduo;

import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 美朵 - 固定展示网页的 Android 应用
 * ✅ 底部导航栏（图标+文字）+ 滑动返回 + 加载动画
 * ✅ URL 和文案全部来自 strings.xml
 */
public class MainActivity extends Activity {

    // ==================== UI 组件 ====================
    private WebView webView;
    private ProgressBar loadingProgress;
    private TextView errorView;

    // 导航项（容器 + 图标 + 文字）
    private NavItem navHome;
    private NavItem navPage2;
    private NavItem navPage3;
    private NavItem navPage4;

    // ==================== 状态 ====================
    private static final int STATE_WEB = 1;
    private static final int STATE_ERROR = 2;
    private int currentState = STATE_WEB;

    // URL 历史（用于滑动返回）
    private final List<String> urlHistory = new ArrayList<>();
    private int currentNavIndex = 0;

    // ==================== 导航项数据类 ====================
    private static class NavItem {
        LinearLayout container;
        ImageView icon;
        TextView text;
        String url;
        int iconRes;       // 普通图标
        int iconActiveRes;  // 选中图标

        NavItem(LinearLayout container, ImageView icon, TextView text,
                String url, int iconRes, int iconActiveRes) {
            this.container = container;
            this.icon = icon;
            this.text = text;
            this.url = url;
            this.iconRes = iconRes;
            this.iconActiveRes = iconActiveRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupWebView();
        setupNavigation();
        loadHome();
    }

    // ==================== 初始化 UI ====================

    private void initViews() {
        webView = findViewById(R.id.webView);
        loadingProgress = findViewById(R.id.loadingProgress);
        errorView = findViewById(R.id.errorView);

        // 首页
        navHome = new NavItem(
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_home_icon),
                findViewById(R.id.nav_home_text),
                getString(R.string.url_home),
                R.drawable.ic_home,
                R.drawable.ic_home_active
        );

        // 发现
        navPage2 = new NavItem(
                findViewById(R.id.nav_page2),
                findViewById(R.id.nav_page2_icon),
                findViewById(R.id.nav_page2_text),
                getString(R.string.url_page2),
                R.drawable.ic_2,
                R.drawable.ic_2_active
        );

        // 消息
        navPage3 = new NavItem(
                findViewById(R.id.nav_page3),
                findViewById(R.id.nav_page3_icon),
                findViewById(R.id.nav_page3_text),
                getString(R.string.url_page3),
                R.drawable.ic_3,
                R.drawable.ic_3_active
        );

        // 我的
        navPage4 = new NavItem(
                findViewById(R.id.nav_page4),
                findViewById(R.id.nav_page4_icon),
                findViewById(R.id.nav_page4_text),
                getString(R.string.url_page4),
                R.drawable.ic_4,
                R.drawable.ic_4_active
        );
    }

    // ==================== WebView 配置 ====================

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        WebView.setWebContentsDebuggingEnabled(true);

        // 进度条
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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                urlHistory.add(url);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showWebView();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                showErrorView(errorCode, description, failingUrl);
            }
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
            public void onReceivedSslError(WebView view,
                                           android.webkit.SslErrorHandler handler,
                                           SslError error) {
                showErrorView(
                        error.getPrimaryError(),
                        getString(R.string.error_ssl),
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

        // webView.setBackgroundColor(Color.parseColor("#403F3F"));
        webView.setBackgroundColor(getColor(android.R.color.transparent));
    }

    // ==================== 底部导航 ====================

    private void setupNavigation() {
        navHome.container.setOnClickListener(v -> selectNav(navHome, 0));
        navPage2.container.setOnClickListener(v -> selectNav(navPage2, 1));
        navPage3.container.setOnClickListener(v -> selectNav(navPage3, 2));
        navPage4.container.setOnClickListener(v -> selectNav(navPage4, 3));
    }

    /**
     * 选中指定导航项，加载对应 URL
     */
    private void selectNav(NavItem item, int index) {
        if ( currentNavIndex != index ){
            currentNavIndex = index;
            loadUrl(item.url);
            highlightNav(item);
        }
    }

    /**
     * 高亮当前导航项，切换图标和文字颜色
     */
    private void highlightNav(NavItem active) {
        // 全部重置为普通态
        resetNav(navHome);
        resetNav(navPage2);
        resetNav(navPage3);
        resetNav(navPage4);

        // 选中项：切换图标 + 高亮文字
        active.icon.setImageResource(active.iconActiveRes);
        active.text.setTextColor(Color.parseColor("#3DDC84"));
    }

    /**
     * 重置导航项为普通态
     */
    private void resetNav(NavItem item) {
        item.icon.setImageResource(item.iconRes);
        item.text.setTextColor(Color.parseColor("#999999"));
    }

    // ==================== 页面加载 ====================

    private void loadHome() {
        currentNavIndex = 0;
        loadUrl(navHome.url);
        highlightNav(navHome);
    }

    private void loadUrl(String url) {
        showLoading();
        webView.loadUrl(url);
    }

    // ==================== 滑动返回 ====================

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            if (!urlHistory.isEmpty()) {
                urlHistory.remove(urlHistory.size() - 1);
            }
        } else {
            // 已在最底层，回到首页或退出
            if (currentNavIndex != 0) {
                navHome.container.performClick();
            } else {
                finish();
            }
        }
    }

    // ==================== 状态切换 ====================

    private void showLoading() {
        currentState = STATE_WEB;
        loadingProgress.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
    }

    private void showWebView() {
        currentState = STATE_WEB;
        loadingProgress.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void showErrorView(int errorCode, String description, String url) {
        if (currentState != STATE_ERROR) {
            currentState = STATE_ERROR;
            loadingProgress.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
            errorView.setText(formatErrorMessage(errorCode, description, url));
        }
    }

    // ==================== 错误消息 ====================

    private String formatErrorMessage(int errorCode, String description, String url) {
        String message;
        switch (errorCode) {
            case -2:
                message = getString(R.string.error_network);
                break;
            case -3:
                message = getString(R.string.error_dns);
                break;
            case -6:
                message = getString(R.string.error_redirect);
                break;
            default:
                message = getString(R.string.error_unknown) + "\n\n" + description;
        }
        return message + "\n\n错误码：" + errorCode;
    }

    // ==================== 生命周期 ====================

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
