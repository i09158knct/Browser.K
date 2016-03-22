package net.i09158knct.android.browserk.browser

import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.R
import net.i09158knct.android.browserk.utils.Util

class ForegroundTabManager(var tab: Tab) {
    private val viewClient = ViewClient()
    private val chromeClient = ChromeClient()
    var listener: IEventListener? = null

    init {
        tab.wb.setWebViewClient(viewClient)
        tab.wb.setWebChromeClient(chromeClient)
    }

    fun changeTab(newTab: Tab): Unit {
        newTab.wb.setWebViewClient(viewClient)
        newTab.wb.setWebChromeClient(chromeClient)
        val oldTab = tab
        oldTab.wb.setWebViewClient(BackgroundViewClient)
        oldTab.wb.setWebChromeClient(BackGroundChromeClient)
        tab = newTab
        listener?.onForegroundTabChanged(oldTab, tab)
        listener?.onTitleChanged(tab.wb.title)
        listener?.onUrlChanged(tab.wb.url)
        listener?.onProgressChanged(tab.wb.progress)
        listener?.onBackForwardStateChanged(
                tab.wb.canGoBack(),
                tab.wb.canGoForward())
    }

    interface IEventListener {
        fun onForegroundTabChanged(oldTab: Tab, newTab: Tab)

        fun onTitleChanged(title: String)
        fun onUrlChanged(url: String)

        fun onPageStarted()
        fun onPageFinished()

        fun onProgressChanged(progress: Int)
        fun onBackForwardStateChanged(canGoBack: Boolean, canGoForward: Boolean)
        fun onReloadStopStateChanged(loading: Boolean)
    }

    object BackgroundViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.v(Util.tag, "url: ${url}")
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            App.toaster.show(R.string.loading)
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            App.toaster.show(R.string.loaded)
        }
    }
    object  BackGroundChromeClient : WebChromeClient() {
    }

    inner class ViewClient() : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.v(Util.tag, "url: ${url}")
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            Log.v(Util.tag, "url: $url")
            listener?.onPageStarted()
            listener?.onUrlChanged(url)
            listener?.onBackForwardStateChanged(
                    tab.wb.canGoBack(),
                    tab.wb.canGoForward())
            listener?.onReloadStopStateChanged(true)
        }

        override fun onPageFinished(view: WebView, url: String) {
            Log.v(Util.tag, "url: $url")
            listener?.onPageFinished()
            listener?.onReloadStopStateChanged(false)
            listener?.onBackForwardStateChanged(
                    tab.wb.canGoBack(),
                    tab.wb.canGoForward())
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            Util.debug(Util.tag, "error $errorCode $description $failingUrl")
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            Util.debug(Util.tag, "ssl error: $error")
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
            Util.debug(Util.tag, "auth ${host} ${realm}")
        }
    }

    inner class ChromeClient() : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            Log.v(Util.tag, "${title}")
            listener?.onTitleChanged(title ?: "(no title)")
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            Log.v(Util.tag, "${newProgress}")
            listener?.onProgressChanged(newProgress)
        }
    }
}