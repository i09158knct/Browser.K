package net.i09158knct.android.browserk.browser

import android.webkit.WebView

class Tab(val wb: WebView
          , val chromeClient: CustomWebChromeClient
          , val viewClient: CustomWebViewClient) {
    init {
        wb.setWebChromeClient(chromeClient)
        wb.setWebViewClient(viewClient)
    }

    fun reload(): Unit {
        wb.reload()
    }

    fun stopLoading(): Unit {
        wb.stopLoading()
    }

    fun loadUrl(url: String) {
        wb.loadUrl(url)
    }

    fun back() {
        wb.goBack()
    }

    fun forward() {
        wb.goForward()
    }
}