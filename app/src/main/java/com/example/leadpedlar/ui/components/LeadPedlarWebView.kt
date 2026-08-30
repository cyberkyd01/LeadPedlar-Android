package com.example.leadpedlar.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.leadpedlar.calling.CallManager
import com.example.leadpedlar.theme.BgDark
import com.example.leadpedlar.theme.Emerald500

class LeadPedlarAndroidBridge(
    private val onOpenCallSelector: (phoneNumber: String, leadName: String) -> Unit,
    private val context: Context
) {
    @JavascriptInterface
    fun openCallSelector(phoneNumber: String, leadName: String) {
        onOpenCallSelector(phoneNumber, leadName)
    }

    @JavascriptInterface
    fun makeCall(phoneNumber: String) {
        onOpenCallSelector(phoneNumber, "Lead Contact")
    }

    @JavascriptInterface
    fun isAppInstalled(packageName: String): Boolean {
        return CallManager.isPackageInstalled(context, packageName)
    }

    @JavascriptInterface
    fun getPlatform(): String = "AndroidNative"

    @JavascriptInterface
    fun getAppVersion(): String = "1.0.0"
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeadPedlarWebView(
    url: String,
    onOpenCallSelector: (phoneNumber: String, leadName: String) -> Unit,
    modifier: Modifier = Modifier,
    onTitleChange: ((String) -> Unit)? = null
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    Box(modifier = modifier.fillMaxSize().background(BgDark)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0xFF0B0F17.toInt())

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = "$userAgentString LeadPedlarApp/1.0 (Android; Mobile)"
                    }

                    // Attach Javascript Interface
                    addJavascriptInterface(
                        LeadPedlarAndroidBridge(onOpenCallSelector, context),
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            canGoBack = view?.canGoBack() ?: false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            canGoBack = view?.canGoBack() ?: false
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val targetUrl = request?.url?.toString() ?: return false

                            // Intercept tel: links
                            if (targetUrl.startsWith("tel:")) {
                                val phone = targetUrl.removePrefix("tel:")
                                onOpenCallSelector(phone, "Lead")
                                return true
                            }

                            // Intercept WhatsApp links
                            if (targetUrl.startsWith("whatsapp:") || targetUrl.startsWith("https://wa.me/") || targetUrl.startsWith("https://api.whatsapp.com/")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    return true
                                } catch (e: Exception) {
                                    // Fallback to loading in webview
                                }
                            }

                            // Intercept Telegram links
                            if (targetUrl.startsWith("tg:") || targetUrl.startsWith("https://t.me/")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    return true
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }

                            return false
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            isLoading = false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progress = newProgress / 100f
                            if (newProgress == 100) {
                                isLoading = false
                            }
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            title?.let { onTitleChange?.invoke(it) }
                        }
                    }

                    loadUrl(url)
                    webViewInstance = this
                }
            },
            update = { webView ->
                if (webView.url != url && !url.isEmpty()) {
                    webView.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Progress bar indicator
        if (isLoading && progress < 1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter),
                color = Emerald500,
                trackColor = Color.Transparent
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
        }
    }
}
