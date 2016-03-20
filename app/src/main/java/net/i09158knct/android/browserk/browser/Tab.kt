package net.i09158knct.android.browserk.browser

import android.app.Activity
import android.webkit.WebView
import net.i09158knct.android.browserk.App
import net.i09158knct.android.browserk.activities.MainActivity
import net.i09158knct.android.browserk.utils.Util

class Tab(val wb: WebView) {

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