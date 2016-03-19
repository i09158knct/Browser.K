package net.i09158knct.android.browserk.browser

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import net.i09158knct.android.browserk.utils.Util


class CustomWebChromeClient(val listener: IEventListener) : WebChromeClient() {

    override fun onReceivedTitle(view: WebView?, title: String?) {
        Log.d(Util.tag, "title: ${title}")
        listener.onReceivedTitle(view!!, title!!)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        Log.d(Util.tag, "progress: ${newProgress}")
        listener.onProgressChanged(view, newProgress)
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        Log.d(Util.tag, "received icon")
        listener.onReceivedIcon(view, icon)
    }

    interface IEventListener {
        fun onReceivedTitle(view: WebView, title: String): Unit
        fun onProgressChanged(view: WebView, newProgress: Int): Unit
        fun onReceivedIcon(view: WebView, icon: Bitmap): Unit
    }
}