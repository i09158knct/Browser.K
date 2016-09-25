package net.i09158knct.android.browserk.browser

import android.app.Activity
import android.webkit.WebSettings
import android.webkit.WebView
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.utils.Util

class Tab(val wb: WebView, js: Boolean, image: Boolean) {
    init {
        val ws = wb.settings
        wb.setVerticalScrollbarOverlay(true)
        //        ws.setAllowContentAccess(true)
        //        ws.setAllowFileAccess(true)
        ws.setAllowFileAccessFromFileURLs(false)
        ws.setAllowUniversalAccessFromFileURLs(false)
        //        ws.setAppCacheEnabled(false)
        //        ws.setAppCachePath()
        //        ws.setBlockNetworkImage(false)
        //        ws.setBlockNetworkLoads(false)
        ws.setBuiltInZoomControls(true) // default is false
        //        ws.setCacheMode(WebSettings.LOAD_DEFAULT)
        //        ws.setCursiveFontFamily("cursive")
        //        ws.setDatabaseEnabled(false)
        //        ws.setDatabasePath()
        //        ws.setDefaultFixedFontSize(16)
        //        ws.setDefaultFontSize(16)
        //        ws.setDefaultTextEncodingName("UTF-8")
        //        ws.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM)
        ws.setDisplayZoomControls(false) // default is true
        ws.setDomStorageEnabled(true) // default is false
        //        ws.setEnableSmoothTransition(false)
        //        ws.setFantasyFontFamily("fantasy")
        //        ws.setFixedFontFamily("monospace")
        //        ws.setGeolocationDatabasePath()
        //        ws.setGeolocationEnabled()
        //        ws.setJavaScriptCanOpenWindowsAutomatically(false)
        ws.setJavaScriptEnabled(js)
        //        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS)
        //        ws.setLightTouchEnabled()
        //        ws.setLoadWithOverviewMode(false)
        ws.setLoadsImagesAutomatically(image)
        //        ws.setMediaPlaybackRequiresUserGesture(true)
        //        ws.setMinimumFontSize(8)
        //        ws.setMinimumLogicalFontSize(8)
        //        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW)
        //        ws.setNeedInitialFocus(true)
        // Sets whether this WebView should raster tiles when it is offscreen but attached
        // to a window. Turning this on can avoid rendering artifacts when animating an
        // offscreen WebView on-screen. Offscreen WebViews in this mode use more memory.
        // The default value is false.
        // Please follow these guidelines to limit memory usage:
        // - WebView size should be not be larger than the device screen size.
        // - Limit use of this mode to a small number of WebViews.
        //   Use it for visible WebViews and WebViews about to be animated to visible.
        //        ws.setOffscreenPreRaster(false)
        //        ws.setPluginState(WebSettings.PluginState.OFF)
        //        ws.setRenderPriority(WebSettings.RenderPriority.NORMAL)
        //        ws.setSansSerifFontFamily("sans-serif")
        ws.setSaveFormData(false) // default is true
        //        ws.setSavePassword(false) // default is true
        //        ws.setSerifFontFamily("sans-serif") // !!
        //        ws.setStandardFontFamily("sans-serif")
        ws.setSupportMultipleWindows(false) // TODO implements onCreateWindow to set true
        //        ws.setSupportZoom(true)
        //        ws.setTextZoom(100)
        ws.setUseWideViewPort(true)
        //        ws.setUserAgentString()
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

    fun shareUrl(activity: Activity) {
        Util.shareUrl(activity, wb.url)
    }

    fun openInOtherBrowser(activity: Activity) {
        Util.openInOtherBrowser(activity, wb.url)
    }

    fun bookmark() {
        // TODO
        App.toaster.show("TODO")
    }
}