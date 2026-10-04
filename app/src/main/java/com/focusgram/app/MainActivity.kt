package com.focusgram.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val instagramOrigin = "https://www.instagram.com/"
    private var reelMode = false
    private var downY = 0f

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        configureWebView()
        configureTouchGuard()

        if (savedInstanceState == null) {
            webView.loadUrl(instagramOrigin)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    private fun configureWebView() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = false
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            mediaPlaybackRequiresUserGesture = true
            userAgentString = userAgentString
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return !isAllowedInstagramUrl(request.url.toString())
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                updateMode(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                updateMode(url)
                FocusInjector.install(view, reelMode)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    Toast.makeText(
                        this@MainActivity,
                        "Instagram page could not be loaded.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun configureTouchGuard() {
        webView.setOnTouchListener { _, event ->
            if (!reelMode) return@setOnTouchListener false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - downY
                    if (abs(deltaY) > 12f) {
                        // Let the page's injected guard handle the DOM scroll.
                        // Returning false preserves normal taps and controls.
                        false
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun updateMode(url: String) {
        reelMode = isInstagramReel(url)
    }

    private fun isInstagramReel(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.host?.lowercase() == "www.instagram.com" &&
                uri.pathSegments.firstOrNull() == "reel"
        } catch (_: Exception) {
            false
        }
    }

    private fun isAllowedInstagramUrl(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            host == "www.instagram.com" || host == "instagram.com" ||
                host.endsWith(".instagram.com")
        } catch (_: Exception) {
            false
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webChromeClient = null
        webView.destroy()
        super.onDestroy()
    }

    }
