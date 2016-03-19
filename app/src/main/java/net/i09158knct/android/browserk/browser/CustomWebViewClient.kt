package net.i09158knct.android.browserk.browser

import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import net.i09158knct.android.browserk.utils.Util

class CustomWebViewClient(val listener: IEventListener) : android.webkit.WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        Log.d(Util.tag, "url: ${url}")
        return false
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        Log.d(Util.tag, "started bitmap: ${favicon != null} url: $url")
        listener.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        Log.d(Util.tag, "finished url: $url")
        listener.onPageFinished(view, url)
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        Util.debug(Util.tag, "error $errorCode $description $failingUrl")
        listener.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        Util.debug(Util.tag, "ssl error: $error")
        listener.onReceivedSslError(view, handler, error)
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
        Util.debug(Util.tag, "auth ${host} ${realm}")
        listener.onReceivedHttpAuthRequest(view, handler, host, realm)
    }


    interface IEventListener {
        fun onPageStarted(view: WebView, url: String, favicon: Bitmap?)
        fun onPageFinished(view: WebView, url: String)
        fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String)
        fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError)
        fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String)
    }
}